package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.FriendsPresenter;
import com.tribe.app.presentation.mvp.view.FriendsMVPView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.adapter.UserListAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.UserListLayoutManager;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.common.PickAllView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

public class PickYourFriendsActivity extends BaseActivity implements FriendsMVPView {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, PickYourFriendsActivity.class);
    return intent;
  }

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject UserListAdapter adapter;

  @Inject FriendsPresenter friendsPresenter;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @BindView(R.id.layoutFocus) ViewGroup layoutFocus;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.layoutContent) ViewGroup layoutContent;

  //@BindView(R.id.layoutFriendsLoad)
  //View layoutFriendsLoad;

  @BindView(R.id.viewPickAll) PickAllView viewPickAll;

  @BindView(R.id.viewSeparatorLarge) View viewSeparatorLarge;

  @BindView(R.id.editTextSearch) EditTextFont editTextSearch;

  @BindView(R.id.layoutSearch) ViewGroup layoutSearch;

  @BindView(R.id.viewSeparatorSearch) View viewSeparatorSearch;

  @BindView(R.id.layoutBottom) ViewGroup layoutBottom;

  @BindView(R.id.layoutTop) ViewGroup layoutTop;

  LoadFriendsView viewFriendsFBLoad;
  LoadFriendsView viewFriendsAddressBookLoad;
  View viewSeparatorAddressBook;
  View viewSeparatorFBTop;
  View viewSeparatorFBBottom;

  // VARIABLES
  private Unbinder unbinder;
  private Uri deepLink;
  private UserListLayoutManager layoutManager;
  private List<User> newFriends;
  private int countFriends;
  private List<User> contactList;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pick_friends);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init();
    initResources();
    manageDeepLink(getIntent());
    initRecyclerView();

    tagManager.trackEvent(TagManagerConstants.ONBOARDING_SHOW_ADD_FRIENDS);
  }

  @Override protected void onStart() {
    super.onStart();
    friendsPresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    friendsPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void init() {
    newFriends = new ArrayList<>();
    contactList = new ArrayList<>();
    renderContactList(new ArrayList<>());
    refactorActions();

    subscriptions.add(
        RxTextView.textChanges(editTextSearch).map(CharSequence::toString).subscribe(s -> {
          filter(s);
        }));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initResources() {

  }

  private void manageDeepLink(Intent intent) {
    if (intent != null && intent.getData() != null) {
      deepLink = intent.getData();
    }
  }

  private void initRecyclerView() {
    layoutManager = new UserListLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(15), screenUtils.dpToPx(10), 1));

    subscriptions.add(adapter.clickAdd()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition((View) view)))
        .subscribe(o -> {
          if (o instanceof User) {
            User user = (User) o;
            if (newFriends.contains(user)) {
              newFriends.remove(user);
            } else {
              newFriends.add(user);
            }
            adapter.updateUser(user);
            refactorDone();
          }
        }));
  }

  private void initLoadView(View v) {
    viewFriendsFBLoad = ButterKnife.findById(v, R.id.viewFriendsFBLoad);
    viewFriendsAddressBookLoad = ButterKnife.findById(v, R.id.viewFriendsAddressBookLoad);
    viewSeparatorAddressBook = ButterKnife.findById(v, R.id.viewSeparatorAddressBook);
    viewSeparatorFBTop = ButterKnife.findById(v, R.id.viewSeparatorFBTop);
    viewSeparatorFBBottom = ButterKnife.findById(v, R.id.viewSeparatorFBBottom);
  }

  private void filter(String text) {
    adapter.filterList(text);
  }

  private void refactorActions() {
    boolean permissionsFB = FacebookUtils.isLoggedIn();
    boolean permissionsContact = PermissionUtils.hasPermissionsContact(this);

    layoutBottom.removeAllViews();
    layoutTop.removeAllViews();

    if (permissionsContact && permissionsFB) {
      recyclerView.setPadding(0, 0, 0, 0);
      friendsPresenter.loadContacts();
      return;
    }

    if (!permissionsContact && !permissionsFB) {
      layoutContent.setVisibility(View.GONE);
      layoutTop.setVisibility(View.VISIBLE);
      initLoadView(getLayoutInflater().inflate(R.layout.view_load_ab_fb_friends, layoutTop));
    } else if (!permissionsContact || !permissionsFB) {
      layoutContent.setVisibility(View.VISIBLE);
      layoutBottom.setVisibility(View.VISIBLE);
      recyclerView.setPadding(0, 0, 0,
          getResources().getDimensionPixelSize(R.dimen.load_friends_height));
      initLoadView(getLayoutInflater().inflate(R.layout.view_load_ab_fb_friends, layoutBottom));
    }

    if (permissionsContact || permissionsFB) {
      friendsPresenter.loadContacts();
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
        friendsPresenter.loginFacebook();
        viewFriendsFBLoad.showLoading();
      });
    } else if (!permissionsContact) {
      viewFriendsFBLoad.setVisibility(View.GONE);
      viewSeparatorFBTop.setVisibility(View.GONE);
      viewSeparatorFBBottom.setVisibility(View.GONE);
    }
  }

  private void refactorDone() {
    if (newFriends.size() > 0 || countFriends > 0 || contactList.size() == 0) {
      TextViewCompat.setTextAppearance(txtAction, R.style.Title_2_BlueNew);
    } else {
      TextViewCompat.setTextAppearance(txtAction, R.style.Title_2_Grey);
    }
    txtAction.setCustomFont(this, "Roboto-Bold.ttf");
  }

  private void lookupContacts() {
    RxPermissions.getInstance(this)
        .request(PermissionUtils.PERMISSIONS_CONTACTS)
        .subscribe(hasPermission -> {
          Bundle bundle = new Bundle();
          bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED, hasPermission);
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
    tagManager.trackEvent(TagManagerConstants.ONBOARDING_CONTACTS_SYNC);
    friendsPresenter.lookupContacts();
  }

  private void navigateToHome() {
    tagManager.trackEvent(TagManagerConstants.ONBOARDING_COMPLETED);
    navigator.navigateToHome(this, false, deepLink);
    finish();
  }

  private void addUsers(List<User> userList) {
    Bundle bundle = new Bundle();
    bundle.putInt(TagManagerConstants.TOTAL, userList.size());
    tagManager.trackEvent(TagManagerConstants.ONBOARDING_DID_INVITE_FRIENDS, bundle);
    friendsPresenter.createFriendships(userList);
  }

  @Override public boolean dispatchTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (editTextSearch.hasFocus()) {
        Rect outRect = new Rect();
        editTextSearch.getGlobalVisibleRect(outRect);

        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
          editTextSearch.clearFocus();
          screenUtils.hideKeyboard(editTextSearch);
          layoutFocus.requestFocus();
        }
      }
    }

    return super.dispatchTouchEvent(event);
  }

  @OnClick(R.id.txtAction) void onClickAction() {
    if (newFriends.size() == 0) {
      if (countFriends > 0 || contactList.size() == 0) {
        navigateToHome();
      } else {
        subscriptions.add(
            DialogFactory.dialog(this, getString(R.string.onboarding_friends_to_add_popup_title),
                getString(R.string.onboarding_friends_to_add_popup_message),
                getString(R.string.onboarding_friends_to_add_popup_confirm),
                getString(R.string.onboarding_friends_to_add_popup_cancel))
                .filter(x -> x == false)
                .subscribe(aVoid -> {
                  navigator.navigateToHome(this, false, deepLink);
                }));
      }
    } else {
      addUsers(newFriends);
    }
  }

  @OnClick(R.id.viewPickAll) void onPickAll() {
    addUsers(contactList);
  }

  @Override public void renderContactList(List<User> contactList) {
    countFriends = user.computeUserFriends(contactList);
    adapter.setItems(new ArrayList<>(contactList));

    if (contactList != null && contactList.size() > 0) {
      this.contactList.clear();
      this.contactList.addAll(contactList);

      if (countFriends == contactList.size() || contactList.size() == 0) {
        viewPickAll.setVisibility(View.GONE);
      } else {
        viewPickAll.setVisibility(View.VISIBLE);
      }

      viewSeparatorLarge.setVisibility(View.VISIBLE);
      viewSeparatorSearch.setVisibility(View.VISIBLE);
      layoutSearch.setVisibility(View.VISIBLE);

      if (contactList.size() > 1) {
        viewPickAll.setAvatars(contactList.get(0).getProfilePicture(),
            contactList.get(1).getProfilePicture());
        viewPickAll.setBody(
            getString(R.string.onboarding_friends_to_add_shortcut_subtitle, contactList.size()));
      } else {
        viewPickAll.setAvatars(contactList.get(0).getProfilePicture(), null);
        viewPickAll.setBody(getString(R.string.onboarding_friends_to_add_shortcut_subtitle_one,
            contactList.size()));
      }
    } else {
      contactList.clear();
      viewPickAll.setVisibility(View.GONE);
      viewSeparatorLarge.setVisibility(View.GONE);
      viewSeparatorSearch.setVisibility(View.GONE);
      layoutSearch.setVisibility(View.GONE);
    }

    refactorDone();
  }

  @Override public void showLoading() {
    txtAction.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    txtAction.setVisibility(View.VISIBLE);
    progressView.setVisibility(View.GONE);
  }

  @Override public void showError(String message) {

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

  @Override public void successCreateFriendships() {
    navigateToHome();
  }

  @Override public void errorCreateFriendships() {

  }

  @Override public Context context() {
    return this;
  }
}