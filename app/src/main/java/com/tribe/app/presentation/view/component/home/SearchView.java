package com.tribe.app.presentation.view.component.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class SearchView extends FrameLayout implements SearchMVPView {

  private final static int DURATION = 300;
  private final static int DURATION_FAST = 100;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject SearchPresenter searchPresenter;

  @Inject ContactAdapter contactAdapter;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject TagManager tagManager;

  @BindView(R.id.recyclerViewContacts) RecyclerView recyclerViewContacts;

  @BindView(R.id.layoutContent) ViewGroup layoutContent;

  @BindView(R.id.layoutBottom) ViewGroup layoutBottom;

  @BindView(R.id.layoutTop) ViewGroup layoutTop;

  private LoadFriendsView viewFriendsFBLoad;
  private LoadFriendsView viewFriendsAddressBookLoad;
  private View viewSeparatorAddressBook;
  private View viewSeparatorFBTop;
  private View viewSeparatorFBBottom;

  // RESOURCES
  private int margin;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private GradientDrawable background;
  private ContactsLayoutManager layoutManager;
  private List<Object> filteredContactList;
  private List<Object> originalContactList;
  private SearchResult searchResult;
  private String username;
  private boolean isSearchMode = false;
  private String search;
  private RxPermissions rxPermissions;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onGone = PublishSubject.create();
  private PublishSubject<Void> onShow = PublishSubject.create();
  private PublishSubject<Void> onNavigateToSmsForInvites = PublishSubject.create();
  private PublishSubject<Recipient> onHangLive = PublishSubject.create();
  private PublishSubject<ContactAB> onInvite = PublishSubject.create();
  private PublishSubject<Recipient> onUnblock = PublishSubject.create();

  public SearchView(Context context) {
    super(context);
    init(context, null);
  }

  public SearchView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    searchPresenter.onViewAttached(this);
  }

  @Override protected void onDetachedFromWindow() {
    searchPresenter.onViewDetached();
    recyclerViewContacts.setAdapter(null);
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    super.onDetachedFromWindow();
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_search, this, true);

    unbinder = ButterKnife.bind(this);

    rxPermissions = new RxPermissions((Activity) getContext());

    initUI();
    initRecyclerView();
  }

  private void initUI() {
    filteredContactList = new ArrayList<>();
    originalContactList = new ArrayList<>();

    refactorActions();

    int radiusTopLeft = screenUtils.dpToPx(5);
    int radiusTopRight = screenUtils.dpToPx(5);
    float[] radiusMatrix =
        new float[] { radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, 0, 0, 0, 0 };

    background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setCornerRadii(radiusMatrix);
    background.setColor(Color.TRANSPARENT);
    setBackground(background);
  }

  private void initRecyclerView() {
    this.layoutManager = new ContactsLayoutManager(context());
    this.recyclerViewContacts.setLayoutManager(layoutManager);
    this.recyclerViewContacts.setItemAnimator(null);
    this.recyclerViewContacts.addItemDecoration(
        new DividerHeadersItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10)));
    this.recyclerViewContacts.setAdapter(contactAdapter);

    contactAdapter.setItems(new ArrayList<>());

    subscriptions.add(contactAdapter.onClickAdd()
        .map(view -> contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof SearchResult) {
            SearchResult searchResult = (SearchResult) o;
            if (searchResult.isInvisible()) {
              DialogFactory.dialog(getContext(), searchResult.getDisplayName(),
                  EmojiParser.demojizedText(
                      getContext().getString(R.string.add_friend_error_invisible)),
                  context().getString(R.string.add_friend_error_invisible_invite_ios),
                  context().getString(R.string.add_friend_error_invisible_cancel))
                  .filter(x -> x == true)
                  .subscribe(a -> onNavigateToSmsForInvites.onNext(null));
            } else if (searchResult.getFriendship() == null) {
              if (searchResult.getUsername() != null && !searchResult.getUsername()
                  .equals(user.getUsername())) {
                searchPresenter.createFriendship(searchResult.getId());
              }
            } else {
              searchResult.getFriendship().setStatus(FriendshipRealm.DEFAULT);
              onUnblock.onNext(searchResult.getFriendship());
            }
          } else if (o instanceof Contact) {
            Contact contact = (Contact) o;
            searchPresenter.createFriendship(contact.getUserList().get(0).getId());
          } else if (o instanceof User) {
            User user = (User) o;
            searchPresenter.createFriendship(user.getId());
          }
        }));

    subscriptions.add(contactAdapter.onClickInvite()
        .map(view -> contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof ContactAB) {
            ContactAB contact = (ContactAB) o;
            onInvite.onNext(contact);
          }
        }));

    subscriptions.add(contactAdapter.onHangLive()
        .map(view -> contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof Recipient) {
            onHangLive.onNext((Recipient) o);
          } else if (o instanceof SearchResult) {
            onHangLive.onNext(((SearchResult) o).getFriendship());
          }
        }));

    subscriptions.add(contactAdapter.onUnblock()
        .map(view -> {
          int position = recyclerViewContacts.getChildLayoutPosition(view);
          Recipient recipient = (Recipient) contactAdapter.getItemAtPosition(position);
          return new Pair<>(position, recipient);
        })
        .doOnError(throwable -> throwable.printStackTrace())
        .flatMap(pairPositionRecipient -> DialogFactory.dialog(getContext(),
            pairPositionRecipient.second.getDisplayName(),
            context().getString(R.string.search_unblock_alert_message),
            context().getString(R.string.search_unblock_alert_unblock,
                pairPositionRecipient.second.getDisplayName()),
            context().getString(R.string.search_unblock_alert_cancel)),
            (pairPositionRecipient, aBoolean) -> new Pair<>(pairPositionRecipient, aBoolean))
        .filter(pair -> pair.second == true)
        .subscribe(pair -> {
          Friendship friendship = (Friendship) pair.first.second;
          onUnblock.onNext(pair.first.second);
          friendship.setStatus(FriendshipRealm.DEFAULT);
          friendship.setAnimateAdd(true);
          contactAdapter.notifyItemChanged(pair.first.first);
        }));
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void refactorContacts(List<Object> contactList) {
    this.filteredContactList.clear();

    boolean hasDoneSuggested = false, hasDoneContacts = false;
    boolean hasNewFriends = false;

    for (Object obj : contactList) {
      boolean shouldAdd = false;
      if (obj instanceof Contact) {
        Contact contact = (Contact) obj;
        if (!StringUtils.isEmpty(contact.getName()) && contact.getName()
            .toLowerCase()
            .startsWith(search)) {
          shouldAdd = true;
        }
      } else if (obj instanceof Recipient) {
        Recipient recipient = (Recipient) obj;
        if (!StringUtils.isEmpty(search) && ((!StringUtils.isEmpty(recipient.getDisplayName())
            && recipient.getDisplayName().toLowerCase().startsWith(search))
            || (recipient.getUsername() != null && recipient.getUsername()
            .toLowerCase()
            .startsWith(search)))) {
          shouldAdd = true;
        }
      }

      if (shouldAdd) {
        if (!hasDoneSuggested && obj instanceof Recipient) {
          hasDoneSuggested = true;
          this.filteredContactList.add(
              EmojiParser.demojizedText(getContext().getString(R.string.search_already_friends)));
        } else if (!hasDoneContacts && obj instanceof Contact) {
          Contact contact = (Contact) obj;
          if (contact.getUserList() != null && contact.getUserList().size() > 0) {
            User user = contact.getUserList().get(0);
            user.setNew(contact.isNew());

            if (contact.isNew()) {
              hasNewFriends = true;
            }
            if (hasNewFriends) {
              String titleNewfriend =
                  EmojiParser.demojizedText(getContext().getString(R.string.search_new_friends));
              if (!filteredContactList.contains(titleNewfriend)) {
                this.filteredContactList.add(0, titleNewfriend);
              }
              this.filteredContactList.add(1, user);
            }

            if (!hasDoneSuggested) {
              hasDoneSuggested = true;
              String titleAlreadyFriend = EmojiParser.demojizedText(
                  getContext().getString(R.string.search_already_friends));
              if (!filteredContactList.contains(titleAlreadyFriend)) {
                this.filteredContactList.add(titleAlreadyFriend);
              }
            }

            if (!filteredContactList.contains(user)) {
              this.filteredContactList.add(user);
            }

            shouldAdd = false;
          } else {
            this.filteredContactList.add(
                EmojiParser.demojizedText(getContext().getString(R.string.search_invite_contacts)));
            hasDoneContacts = true;
          }
        }

        if (shouldAdd) this.filteredContactList.add(obj);
      }
    }
  }

  private void filter() {
    refactorContacts(originalContactList);
  }

  private void showContactList() {
    if (!isSearchMode) contactAdapter.setItems(this.filteredContactList);
  }

  private void updateSearch() {
    filter();
    this.contactAdapter.updateSearch(searchResult, filteredContactList);
  }

  private void refactorActions() {
    boolean permissionsFB = FacebookUtils.isLoggedIn();
    boolean permissionsContact =
        PermissionUtils.hasPermissionsContact(rxPermissions) && addressBook.get();

    layoutBottom.removeAllViews();
    layoutTop.removeAllViews();

    if (permissionsContact && permissionsFB) {
      recyclerViewContacts.setPadding(0, 0, 0, 0);
      searchPresenter.loadContacts(search);
      return;
    }

    if (!permissionsContact && !permissionsFB) {
      layoutContent.setVisibility(View.GONE);
      layoutTop.setVisibility(View.VISIBLE);
      initLoadView(inflater.inflate(R.layout.view_load_ab_fb_friends, layoutTop));
    } else if (!permissionsContact || !permissionsFB) {
      layoutContent.setVisibility(View.VISIBLE);
      layoutBottom.setVisibility(View.VISIBLE);
      recyclerViewContacts.setPadding(0, 0, 0,
          getResources().getDimensionPixelSize(R.dimen.load_friends_height));
      initLoadView(inflater.inflate(R.layout.view_load_ab_fb_friends, layoutBottom));
    }

    if (permissionsContact || permissionsFB) {
      searchPresenter.loadContacts(search);
    }

    if (!permissionsContact) {
      viewFriendsAddressBookLoad.setOnClickListener(v -> {
        lookupContacts();
        viewFriendsAddressBookLoad.showLoading();
      });
    } else if (!permissionsFB) {
      viewFriendsAddressBookLoad.setVisibility(View.GONE);
      viewSeparatorAddressBook.setVisibility(View.GONE);
    }

    if (!permissionsFB) {
      if (permissionsContact) viewSeparatorFBBottom.setVisibility(View.GONE);
      viewFriendsFBLoad.setOnClickListener(v -> {
        searchPresenter.loginFacebook();
        viewFriendsFBLoad.showLoading();
      });
    } else if (!permissionsContact) {
      viewFriendsFBLoad.setVisibility(View.GONE);
      viewSeparatorFBTop.setVisibility(View.GONE);
      viewSeparatorFBBottom.setVisibility(View.GONE);
    }
  }

  private void initLoadView(View v) {
    viewFriendsFBLoad = ButterKnife.findById(v, R.id.viewFriendsFBLoad);
    viewFriendsAddressBookLoad = ButterKnife.findById(v, R.id.viewFriendsAddressBookLoad);
    viewSeparatorAddressBook = ButterKnife.findById(v, R.id.viewSeparatorAddressBook);
    viewSeparatorFBTop = ButterKnife.findById(v, R.id.viewSeparatorFBTop);
    viewSeparatorFBBottom = ButterKnife.findById(v, R.id.viewSeparatorFBBottom);
  }

  private void lookupContacts() {
    rxPermissions.request(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(hasPermission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, hasPermission);
      tagManager.setProperty(bundle);

      if (hasPermission) {
        addressBook.set(true);
        sync();
      } else {
        viewFriendsAddressBookLoad.hideLoading();
      }
    });
  }

  private void sync() {
    searchPresenter.lookupContacts();
  }

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void initSearchTextSubscription(Observable<String> obs) {
    subscriptions.add(obs.map(CharSequence::toString).doOnNext(s -> {
      search = s;
      if (StringUtils.isEmpty(s)) {
        isSearchMode = false;
        filter();
        showContactList();
      }
    }).filter(s -> !StringUtils.isEmpty(s)).doOnNext(s -> {
      isSearchMode = true;
      searchResult = new SearchResult();
      searchResult.setUsername(s);
      updateSearch();
    }).debounce(500, TimeUnit.MILLISECONDS).subscribe(s -> searchPresenter.findByUsername(s)));
  }

  public void show() {
    if (recyclerViewContacts.getVisibility() == View.VISIBLE) return;

    onShow.onNext(null);
    ValueAnimator colorAnimation =
        ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, Color.WHITE);
    colorAnimation.addUpdateListener(
        animator -> background.setColor((Integer) animator.getAnimatedValue()));
    colorAnimation.setDuration(DURATION_FAST);
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    colorAnimation.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
      }

      @Override public void onAnimationEnd(Animator animation) {
        recyclerViewContacts.setVisibility(View.VISIBLE);
      }
    });
    colorAnimation.start();
  }

  public void hide() {
    search = "";

    recyclerViewContacts.setVisibility(View.GONE);

    ValueAnimator colorAnimation =
        ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, Color.TRANSPARENT);
    colorAnimation.addUpdateListener(
        animator -> background.setColor((Integer) animator.getAnimatedValue()));
    colorAnimation.setDuration(DURATION_FAST);
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    colorAnimation.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        onGone.onNext(null);
      }
    });
    colorAnimation.start();
  }

  ///////////////
  //   TOUCH   //
  ///////////////

  /////////////////////
  //   OBSERVABLES   //
  /////////////////////
  public Observable<Void> onNavigateToSmsForInvites() {
    return onNavigateToSmsForInvites;
  }

  public Observable<Void> onShow() {
    return onShow;
  }

  public Observable<Void> onGone() {
    return onGone;
  }

  public Observable<Recipient> onHangLive() {
    return onHangLive;
  }

  public Observable<ContactAB> onInvite() {
    return onInvite;
  }

  public Observable<Recipient> onUnblock() {
    return onUnblock;
  }

  @Override public void onAddSuccess(Friendship friendship) {
    contactAdapter.updateAdd(friendship.getFriend());
  }

  @Override public void onAddError() {
    updateSearch();
  }

  @Override public void successFacebookLogin() {
    sync();
  }

  @Override public void errorFacebookLogin() {
    viewFriendsFBLoad.hideLoading();
  }

  @Override public void syncDone() {
    refactorActions();
    viewFriendsFBLoad.hideLoading();
    viewFriendsAddressBookLoad.hideLoading();
  }

  @Override public void renderSearchResult(SearchResult searchResult) {
    if (isSearchMode) {
      searchResult.setAnimateAdd(this.searchResult.isAnimateAdd());
      this.searchResult = searchResult;
      this.searchResult.setMyself(searchResult.getUsername() != null && searchResult.getUsername()
          .equals(user.getUsername()));
      updateSearch();
    }
  }

  @Override public void renderContactList(List<Object> contactList) {
    this.originalContactList.clear();
    this.originalContactList.addAll(contactList);
    refactorContacts(contactList);
    showContactList();
  }

  @Override public void showLoading() {

  }

  @Override public void hideLoading() {

  }

  @Override public void showError(String message) {

  }

  @Override public Context context() {
    return getContext();
  }
}
