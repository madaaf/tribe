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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.ContactFB;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.adapter.SearchAdapter;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.SearchSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.SearchViewDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.CustomFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class SearchView extends CustomFrameLayout implements SearchMVPView, ShortcutMVPView {

  private final static int DURATION = 300;
  private final static int DURATION_FAST = 100;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject SearchPresenter searchPresenter;

  @Inject SearchAdapter searchAdapter;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject StateManager stateManager;

  @Inject Navigator navigator;

  @Inject TagManager tagManager;

  @Inject RxFacebook rxFacebook;

  @BindView(R.id.recyclerViewContacts) RecyclerView recyclerViewContacts;

  @BindView(R.id.layoutContent) ViewGroup layoutContent;

  @BindView(R.id.layoutBottom) ViewGroup layoutBottom;

  private LoadFriendsView viewFriendsFBLoad;
  private LoadFriendsView viewFriendsAddressBookLoad;
  private TextViewFont txtTitle;
  private ImageView imgWarning;
  private ImageView imgToggle;
  private ViewGroup viewOpenClose;

  // RESOURCES
  private int margin;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private GradientDrawable background;
  private ContactsLayoutManager layoutManager;
  private List<Object> filteredContactList;
  private List<Object> originalContactList;
  private List<Object> contactList;
  private SearchResult searchResult;
  private String username;
  private boolean isSearchMode = false;
  private String search;
  private RxPermissions rxPermissions;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SearchResult> onSearchResult = PublishSubject.create();
  private PublishSubject<List<Contact>> onContactsInApp = PublishSubject.create();
  private PublishSubject<List<Contact>> onContactsInvite = PublishSubject.create();
  private PublishSubject<List<Contact>> onFBContactsInvite = PublishSubject.create();
  private PublishSubject<List<Shortcut>> onShortcuts = PublishSubject.create();
  private PublishSubject<Void> onGone = PublishSubject.create();
  private PublishSubject<Void> onShow = PublishSubject.create();
  private PublishSubject<Void> onNavigateToSmsForInvites = PublishSubject.create();
  private PublishSubject<Recipient> onHangLive = PublishSubject.create();
  private PublishSubject<ContactAB> onInvite = PublishSubject.create();
  private PublishSubject<Recipient> onUnblock = PublishSubject.create();
  private PublishSubject<Void> onSyncContacts = PublishSubject.create();

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
    initSubscriptions();
  }

  private void initUI() {
    filteredContactList = new ArrayList<>();
    originalContactList = new ArrayList<>();
    contactList = new ArrayList<>();

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
    layoutManager = new ContactsLayoutManager(context());
    recyclerViewContacts.setLayoutManager(layoutManager);
    recyclerViewContacts.setItemAnimator(null);
    recyclerViewContacts.addItemDecoration(
        new DividerHeadersItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10)));
    recyclerViewContacts.setAdapter(searchAdapter);
    recyclerViewContacts.addItemDecoration(new SearchViewDividerDecoration(context(),
        ContextCompat.getColor(context(), R.color.grey_divider), screenUtils.dpToPx(0.5f),
        getSectionCallback(searchAdapter.getItems())));

    SearchSectionItemDecoration sectionItemDecoration = new SearchSectionItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.list_search_header_height), false,
        getSectionCallback(searchAdapter.getItems()), screenUtils);
    recyclerViewContacts.addItemDecoration(sectionItemDecoration);

    searchAdapter.setItems(new ArrayList<>());

    subscriptions.add(searchAdapter.onClick()
        .map(view -> searchAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof SearchResult) {
            SearchResult searchResult = (SearchResult) o;
            Shortcut shortcut = searchResult.getShortcut();

            if (searchResult.isInvisible()) {
              DialogFactory.dialog(getContext(), searchResult.getDisplayName(),
                  EmojiParser.demojizedText(
                      getContext().getString(R.string.add_friend_error_invisible)),
                  context().getString(R.string.add_friend_error_invisible_invite_android),
                  context().getString(R.string.add_friend_error_invisible_cancel))
                  .filter(x -> x == true)
                  .subscribe(a -> onNavigateToSmsForInvites.onNext(null));
            } else if (shortcut == null) {
              if (searchResult.getUsername() != null &&
                  !searchResult.getUsername().equals(user.getUsername())) {
                searchPresenter.createShortcut(searchResult.getId());
              }
            } else if (!shortcut.getStatus().equals(ShortcutRealm.DEFAULT)) {
              shortcut.setStatus(ShortcutRealm.DEFAULT);
              searchPresenter.updateShortcutStatus(shortcut.getId(), ShortcutRealm.DEFAULT);
            } else {
              searchPresenter.removeShortcut(shortcut.getId());
            }
          } else if (o instanceof Contact) {
            Contact contact = (Contact) o;
            searchPresenter.createShortcut(contact.getUserList().get(0).getId());
          } else if (o instanceof User) {
            User user = (User) o;
            searchPresenter.createShortcut(user.getId());
          }
        }));

    subscriptions.add(searchAdapter.onInvite()
        .map(view -> searchAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof ContactAB) {
            ContactAB contact = (ContactAB) o;
            onInvite.onNext(contact);
          } else if (o instanceof ContactFB) {
            ContactFB contactFB = (ContactFB) o;
            subscriptions.add(rxFacebook.requestGameInvite(contactFB.getId()).subscribe());
          }
        }));

    //subscriptions.add(contactAdapter.onHangLive()
    //    .map(view -> contactAdapter.getItemAtPosition(
    //        recyclerViewContacts.getChildLayoutPosition(view)))
    //    .doOnError(throwable -> throwable.printStackTrace())
    //    .subscribe(o -> {
    //      if (o instanceof Recipient) {
    //        onHangLive.onNext((Recipient) o);
    //      } else if (o instanceof SearchResult) {
    //        onHangLive.onNext(((SearchResult) o).getShortcut());
    //      }
    //    }));
    //
  }

  private void initSubscriptions() {
    subscriptions.add(
        Observable.combineLatest(onSearchResult.startWith(new SearchResult()), onShortcuts,
            onContactsInApp, onContactsInvite, onFBContactsInvite,
            (searchResult, shortcutList, contactInAppList, contactInviteList, fbContactInviteList) -> {
              Set<String> setUserAdded = new HashSet<>();

              if (isSearchMode) {
                searchResult.setAnimateAdd(this.searchResult.isAnimateAdd());
                this.searchResult = searchResult;
                this.searchResult.setMyself(searchResult.getUsername() != null &&
                    searchResult.getUsername().equals(user.getUsername()));
                updateSearch();
              }

              originalContactList.clear();
              for (Shortcut shortcut : shortcutList) {
                setUserAdded.add(shortcut.getSingleFriend().getId());
                originalContactList.add(shortcut);
              }

              if (contactList.size() == 0) {
                for (Contact contact : contactInAppList) {
                  if (contact.getUserList() != null) {
                    for (User userInList : contact.getUserList()) {
                      if (!setUserAdded.contains(userInList.getId())) {
                        contactList.add(contact);
                        setUserAdded.add(userInList.getId());
                      }
                    }
                  }
                }
              }

              originalContactList.addAll(contactList);
              originalContactList.addAll(contactInviteList);
              originalContactList.addAll(fbContactInviteList);

              refactorContacts(contactList);

              return null;
            })
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(o -> showContactList()));
  }

  private SectionCallback getSectionCallback(final List<Object> itemList) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        if (position == 0) return true;

        Object item = itemList.get(position);
        Object itemPrevious = itemList.get(position - 1);

        return position > 0 && !item.getClass().equals(itemPrevious.getClass());
      }

      @Override public int getSectionType(int position) {
        Object item = itemList.get(position);
        if (item instanceof SearchResult || item instanceof Shortcut) {
          if (item instanceof Shortcut) {
            Shortcut shortcut = (Shortcut) item;
            if (shortcut.getId().equals(Shortcut.ID_EMPTY)) {
              return BaseSectionItemDecoration.SEARCH_EMPTY;
            }

            return BaseSectionItemDecoration.SEARCH_RECENT;
          }

          return BaseSectionItemDecoration.SEARCH_RESULTS;
        } else if (item instanceof User) {
          return BaseSectionItemDecoration.SEARCH_SUGGESTED_CONTACTS;
        } else {
          return BaseSectionItemDecoration.SEARCH_INVITES_TO_SEND;
        }
      }
    };
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

    Set<String> userIdsDone = new HashSet<>();

    for (Object obj : contactList) {
      boolean shouldAdd = false;
      if (obj instanceof Contact) {
        Contact contact = (Contact) obj;
        if (!StringUtils.isEmpty(contact.getName()) &&
            contact.getName().toLowerCase().startsWith(search)) {
          shouldAdd = true;
        }
      } else if (obj instanceof Shortcut) {
        Shortcut shortcut = (Shortcut) obj;
        if (!StringUtils.isEmpty(search) &&
            ((!StringUtils.isEmpty(shortcut.getDisplayName()) &&
                shortcut.getDisplayName().toLowerCase().startsWith(search)) ||
                (shortcut.getUsername() != null &&
                    shortcut.getUsername().toLowerCase().startsWith(search)))) {
          shouldAdd = true;
        }
      }

      if (shouldAdd) {
        if (obj instanceof Contact) {
          Contact contact = (Contact) obj;
          if (contact.getUserList() != null && contact.getUserList().size() > 0) {
            User user = contact.getUserList().get(0);
            user.setNew(contact.isNew());

            if (!filteredContactList.contains(user) && !userIdsDone.contains(user.getId())) {
              this.filteredContactList.add(user);
              userIdsDone.add(user.getId());
            }

            shouldAdd = false;
          }
        }

        if (shouldAdd) filteredContactList.add(obj);
      }
    }
  }

  private void filter() {
    refactorContacts(originalContactList);
  }

  private void showContactList() {
    if (!isSearchMode) {
      if (filteredContactList.size() == 0) {
        ListUtils.addEmptyItemsSearch(filteredContactList);
      }

      searchAdapter.setItems(filteredContactList);
    }
  }

  private void updateSearch() {
    filter();
    this.searchAdapter.updateSearch(searchResult, filteredContactList);
  }

  public void refactorWarning(boolean open) {
    boolean permissionsFB = FacebookUtils.isLoggedIn();
    boolean permissionsContact =
        PermissionUtils.hasPermissionsContact(rxPermissions) && addressBook.get();

    if ((!permissionsContact || !permissionsFB) && !open) {
      imgWarning.setVisibility(View.VISIBLE);
    } else {
      imgWarning.setVisibility(View.GONE);
    }
  }

  public void refactorActions() {
    boolean permissionsFB = FacebookUtils.isLoggedIn();
    boolean permissionsContact = PermissionUtils.hasPermissionsContact(rxPermissions) &&
        addressBook.get() &&
        !StringUtils.isEmpty(user.getPhone());

    layoutBottom.removeAllViews();

    initLoadView(inflater.inflate(R.layout.view_load_ab_fb_friends, layoutBottom));

    layoutContent.setVisibility(View.VISIBLE);
    layoutBottom.setVisibility(View.VISIBLE);
    recyclerViewContacts.setPadding(0, 0, 0,
        getResources().getDimensionPixelSize(R.dimen.load_friends_height));

    if (permissionsContact || permissionsFB) {
      searchPresenter.searchLocally(search);
    }

    refactorWarning(isContactsViewOpen());

    viewFriendsAddressBookLoad.setChecked(permissionsContact);
    viewFriendsFBLoad.setChecked(permissionsFB);

    subscriptions.add(viewFriendsFBLoad.onChecked().subscribe(checked -> {

      if (checked) {
        searchPresenter.loginFacebook();
      } else {
        disableLookupFacebook();
      }
    }));

    subscriptions.add(viewFriendsAddressBookLoad.onChecked().subscribe(checked -> {
      if (checked) {
        //changeMyPhoneNumber();
      } else {
        disableLookupContacts();
      }
    }));

    viewFriendsAddressBookLoad.setOnClickListener(v -> {
      // Nothing (capture the event)
    });

    viewFriendsFBLoad.setOnClickListener(v -> {
      // Nothing (capture the event)
    });

    viewOpenClose.setOnClickListener(v -> {
      openCloseContactsView(!isContactsViewOpen(), true);
    });
  }

  private boolean isContactsViewOpen() {
    return layoutBottom.getTranslationY() == 0;
  }

  private void openCloseContactsView(boolean open, boolean animate) {
    int rotation = open ? 180 : 0;
    int translation = open ? 0 : (viewFriendsFBLoad.getHeight() +
        viewFriendsAddressBookLoad.getHeight() +
        screenUtils.dpToPx(1));
    // + 1 because of the dividers 2 * 0.5dp

    if (animate) {
      imgToggle.animate().rotation(rotation).start();
      layoutBottom.animate().translationY(translation).start();
    } else {
      imgToggle.setRotation(rotation);
      layoutBottom.setTranslationY(translation);
    }

    refactorWarning(open);
  }

  @Override protected void onFirstLayout() {
    super.onFirstLayout();

    openCloseContactsView(false, false);
  }

  private void initLoadView(View v) {
    viewFriendsFBLoad = ButterKnife.findById(v, R.id.viewFriendsFBLoad);
    viewFriendsAddressBookLoad = ButterKnife.findById(v, R.id.viewFriendsAddressBookLoad);
    viewOpenClose = ButterKnife.findById(v, R.id.viewOpenClose);
    imgWarning = ButterKnife.findById(v, R.id.imgWarning);
    imgToggle = ButterKnife.findById(v, R.id.imgToggle);
    txtTitle = ButterKnife.findById(v, R.id.txtTitle);

    txtTitle.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.linked_friends_title)));
  }

  protected void showToastMessage(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  private void disableLookupFacebook() {
    if (StringUtils.isEmpty(user.getPhone())) {
      showToastMessage(
          getContext().getString(R.string.linked_friends_unlink_error_unable_to_unlink));
      viewFriendsFBLoad.setChecked(true);
    } else {
      subscriptions.add(DialogFactory.dialog(getContext(), EmojiParser.demojizedText(
          getContext().getString(R.string.linked_friends_notifications_disable_fb_alert_title)),
          getContext().getString(R.string.linked_friends_notifications_disable_fb_alert_msg),
          getContext().getString(R.string.action_cancel),
          getContext().getString(R.string.linked_friends_notifications_disable_fb_alert_disable))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(shouldCancel -> {

            if (shouldCancel) {
              viewFriendsFBLoad.setChecked(true);
            } else {
              FacebookUtils.logout();
              refactorActions();
              searchPresenter.disconnectFromFacebook(user.getId());
            }
          }));
    }
  }

  private void disableLookupContacts() {
    if (!FacebookUtils.isLoggedIn()) {
      showToastMessage(
          getContext().getString(R.string.linked_friends_unlink_error_unable_to_unlink));
      viewFriendsAddressBookLoad.setChecked(true);
    } else {
      subscriptions.add(DialogFactory.dialog(getContext(), EmojiParser.demojizedText(
          getContext().getString(
              R.string.linked_friends_notifications_disable_phone_alert_disable)),
          getContext().getString(R.string.linked_friends_notifications_disable_phone_alert_msg),
          getContext().getString(R.string.action_cancel),
          getContext().getString(R.string.linked_friends_notifications_disable_phone_alert_disable))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(shouldCancel -> {

            if (shouldCancel) {
              viewFriendsAddressBookLoad.setChecked(true);
            } else {
              addressBook.set(false);
              refactorActions();
              searchPresenter.updatePhoneNumber(user.getId(), null, null);
            }
          }));
    }
  }

  private void lookupContacts() {
    rxPermissions.requestEach(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(permission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, permission.granted);
      tagManager.setProperty(bundle);

      if (permission.granted) {
        addressBook.set(true);
        sync();
      } else if (permission.shouldShowRequestPermissionRationale) {
        Timber.d("Denied contact permission without ask never again");
        viewFriendsAddressBookLoad.hideLoading();
        viewFriendsAddressBookLoad.setChecked(false);
      } else {
        Timber.d("Denied contact permission and ask never again");
        viewFriendsAddressBookLoad.hideLoading();
        viewFriendsAddressBookLoad.setChecked(false);
        if (!stateManager.shouldDisplay(StateManager.NEVER_ASK_AGAIN_CONTACT_PERMISSION)) {
          navigator.navigateToSettingApp(getContext());
        }
        stateManager.addTutorialKey(StateManager.NEVER_ASK_AGAIN_CONTACT_PERMISSION);
      }
    });
  }

  private void sync() {
    searchPresenter.lookupContacts();
    onSyncContacts.onNext(null);
  }

  @Override public void successFacebookLogin() {
    sync();
    viewFriendsFBLoad.showLoading();
    searchPresenter.connectToFacebook(user.getId(), FacebookUtils.accessToken().getToken());
  }

  @Override public void errorFacebookLogin() {
    viewFriendsFBLoad.setChecked(false);
  }

  @Override public void syncDone() {
    refactorActions();
    viewFriendsFBLoad.hideLoading();
    viewFriendsAddressBookLoad.hideLoading();
  }

  @Override public void renderSearchResult(SearchResult searchResult) {
    onSearchResult.onNext(searchResult);
  }

  @Override public void renderContactList(List<Shortcut> shortcutList) {
    onShortcuts.onNext(shortcutList);
  }

  @Override public void renderContactListOnApp(List<Contact> contactListOnApp) {
    onContactsInApp.onNext(contactListOnApp);
  }

  @Override public void renderContactListInvite(List<Contact> contactListInvite) {
    onContactsInvite.onNext(contactListInvite);
  }

  @Override public void renderContactListInviteFB(List<Contact> contactListInviteFB) {
    onFBContactsInvite.onNext(contactListInviteFB);
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {

  }

  @Override public void successUpdateFacebook(User user) {
    if (FacebookUtils.isLoggedIn()) {
      showToastMessage(getContext().getString(R.string.linked_friends_link_success_fb));
    } else {
      showToastMessage(getContext().getString(R.string.linked_friends_unlink_success_fb));
    }
  }

  @Override public void errorUpdatePhoneNumber() {
    refactorActions();
    showToastMessage(getContext().getString(R.string.linked_friends_link_error_phone));
  }

  @Override public void successUpdatePhoneNumber(User user) {
    if (!StringUtils.isEmpty(user.getPhone())) {
      showToastMessage(getContext().getString(R.string.linked_friends_link_success_phone));
      lookupContacts();
    } else {
      showToastMessage(getContext().getString(R.string.linked_friends_unlink_success_phone));
    }
  }

  @Override public void usernameResult(Boolean available) {

  }

  @Override public void successUpdateUser(User user) {

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

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.SEARCH);
    tagManager.trackEvent(TagManagerUtils.AddFriend, bundle);
    searchAdapter.updateAdd(shortcut.getSingleFriend());
  }

  @Override public void onShortcutCreatedError() {
    Toast.makeText(context(),
        EmojiParser.demojizedText(context().getString(R.string.add_friend_error_invisible)),
        Toast.LENGTH_SHORT).show();
    updateSearch();
  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void onShortcut(Shortcut shortcut) {

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
    contactList.clear();

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

  public Observable<Void> onSyncContacts() {
    return onSyncContacts;
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

  public Observable<Recipient> onClickLive() {
    return searchAdapter.onClickLive()
        .map(view -> (Recipient) searchAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)));
  }

  public Observable<Recipient> onClickChat() {
    return searchAdapter.onClickChat()
        .map(view -> (Recipient) searchAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)));
  }

  public Observable<Recipient> onMainClick() {
    return searchAdapter.onMainClick()
        .map(view -> (Recipient) searchAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view)));
  }
}
