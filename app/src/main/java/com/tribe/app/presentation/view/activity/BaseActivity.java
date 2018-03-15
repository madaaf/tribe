package com.tribe.app.presentation.view.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.preferences.ChallengeNotifications;
import com.tribe.app.presentation.utils.preferences.HasSoftKeys;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Base {@link android.app.Activity} class for every Activity in this application.
 */
public abstract class BaseActivity extends AppCompatActivity {

  protected static boolean isFirstLeaveRoom = false;

  private Context context;

  @Inject Navigator navigator;

  @Inject TagManager tagManager;

  @Inject User user;

  @Inject RxFacebook rxFacebook;

  @Inject @HasSoftKeys Preference<Boolean> hasSoftKeys;

  @Inject @ChallengeNotifications Preference<String> challengeNotificationsPref;

  @Inject StateManager stateManager;

  @Inject UserPresenter userPresenter;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onStop() {
    super.onStop();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getApplicationComponent().inject(this);
    if (!hasSoftKeys.isSet()) hasSoftKeys.set(hasSoftKeys());
    this.context = this;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    connectToFacebook();
  }

  @Override protected void onResume() {
    super.onResume();
    if (getResources().getBoolean(R.bool.isTablet)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    displayChallengerNotifications();
  }

  private void displayChallengerNotifications() {
    if (challengeNotificationsPref != null &&
        challengeNotificationsPref.get() != null &&
        !challengeNotificationsPref.get().isEmpty()) {
      ArrayList usersIds =
          new ArrayList<>(Arrays.asList(challengeNotificationsPref.get().split(",")));
      userPresenter.getUsersInfoListById(usersIds);
    } else if (isFirstLeaveRoom) {
      isFirstLeaveRoom = false;
      List<NotificationModel> list = new ArrayList<>();
      NotifView view = new NotifView(getBaseContext());
      NotificationModel a = NotificationUtils.getFbNotificationModel(this, null);
      list.add(a);
      view.show(this, list);
    }
  }

  private void connectToFacebook() {
    if (isFirstLeaveRoom) {
      displayPopups();
      isFirstLeaveRoom = false;
    }
  }

  private void displayPopups() {
    List<NotificationModel> list = new ArrayList<>();
    NotifView view = new NotifView(getBaseContext());

    NotificationModel a = NotificationUtils.getFbNotificationModel(this, null);
    list.add(a);
    Date now = new Date();
    if ((user.getProfilePicture() == null ||
        user.getProfilePicture().isEmpty() ||
        user.getProfilePicture().equals("http://no")) &&
        (user.getCreatedAt() != null && (now.getTime() - user.getCreatedAt().getTime()) > 24 * 60 * 60 * 1000)) {
      NotificationModel b = NotificationUtils.getAvatarNotificationModel(this);
      list.add(b);
    }
    if (!list.isEmpty()) view.show(this, list);
  }

  public boolean hasSoftKeys() {
    boolean hasSoftwareKeys;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      Display d = getWindowManager().getDefaultDisplay();

      DisplayMetrics realDisplayMetrics = new DisplayMetrics();
      d.getRealMetrics(realDisplayMetrics);

      int realHeight = realDisplayMetrics.heightPixels;
      int realWidth = realDisplayMetrics.widthPixels;

      DisplayMetrics displayMetrics = new DisplayMetrics();
      d.getMetrics(displayMetrics);

      int displayHeight = displayMetrics.heightPixels;
      int displayWidth = displayMetrics.widthPixels;

      hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    } else {
      boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
      boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
      hasSoftwareKeys = !hasMenuKey && !hasBackKey;
    }

    return hasSoftwareKeys;
  }

  /**
   * Adds a {@link Fragment} to this activity's layout.
   *
   * @param containerViewId The container view to where add the fragment.
   * @param fragment The fragment to be added.
   */
  protected void addFragment(int containerViewId, Fragment fragment) {
    FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
    fragmentTransaction.add(containerViewId, fragment);
    fragmentTransaction.commit();
  }

  /**
   * Get the Main Application component for dependency injection.
   *
   * @return {@link ApplicationComponent}
   */
  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) getApplication()).getApplicationComponent();
  }

  /**
   * Get an Activity module for dependency injection.
   *
   * @return {@link com.tribe.app.presentation.internal.di.modules.ActivityModule}
   */
  protected ActivityModule getActivityModule() {
    return new ActivityModule(this);
  }

  public User getCurrentUser() {
    return ((AndroidApplication) getApplication()).getApplicationComponent().currentUser();
  }

  public TagManager getTagManager() {
    return tagManager;
  }

  /**
   * Shows a {@link android.widget.Toast} message.
   *
   * @param message An string representing a message to be shown.
   */
  protected void showToastMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  protected int getStatusBarHeight() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

  protected void onResumeLockPhone() {
    Window wind = this.getWindow();
    wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
  }
}
