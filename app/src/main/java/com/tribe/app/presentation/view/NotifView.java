package com.tribe.app.presentation.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.mvp.presenter.NewChatPresenter;
import com.tribe.app.presentation.mvp.view.adapter.NewChatMVPViewAdapter;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.utils.preferences.HasSoftKeys;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotifView extends FrameLayout {

  private final static String DOTS_TAG_MARKER = "DOTS_TAG_MARKER_";

  private final static int NOTIF_ANIM_DURATION_ENTER = 500;

  private static ViewGroup decorView;
  private static View v;
  private static boolean disposeView = false;

  private Unbinder unbinder;
  private Context context;
  private LayoutInflater inflater;
  private NotificationViewPagerAdapter adapter;
  private GestureDetectorCompat gestureScanner;
  private List<NotificationModel> data;
  private NewChatMVPViewAdapter newChatMVPViewAdapter;
  private PageListener pageListener;
  private OnFinishEventListener listener;

  @BindView(R.id.pager) ViewPager pager;
  @BindView(R.id.txtDismiss) TextViewFont textDismiss;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.bgView) View bgView;
  @BindView(R.id.dotsContainer) LinearLayout dotsContainer;

  @Inject NewChatPresenter newChatPresenter;
  @Inject ScreenUtils screenUtils;
  @Inject TagManager tagManager;
  @Inject RxFacebook rxFacebook;
  @Inject RxImagePicker rxImagePicker;
  @Inject User currentUser;
  @Inject @HasSoftKeys Preference<Boolean> hasSoftKeys;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();

  public NotifView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public NotifView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public void overrideBackground(Drawable background) {
    bgView.setBackground(background);
  }

  public void show(Activity activity, List<NotificationModel> list) {
    this.data = list;
    if (list.size() < 2) {
      dotsContainer.setAlpha(0f);
    }
    decorView = (ViewGroup) activity.getWindow().getDecorView();
    adapter = new NotificationViewPagerAdapter(context, list);
    initDots(list.size());
    pageListener = new PageListener(dotsContainer);
    pager.addOnPageChangeListener(pageListener);
    pager.setAdapter(adapter);
    pager.setVisibility(GONE);
    decorView.addView(v);

    animateView();

    subscriptions.add(adapter.onClickBtn1().subscribe(notificationModel -> {
      switch (notificationModel.getType()) {
        case NotificationModel.POPUP_CHALLENGER:
          newChatPresenter.createShortcut(notificationModel.getUserId());
          tagAddedFriend();
          hideNextNotif();
          break;
        case NotificationModel.POPUP_FACEBOOK:
          if (!FacebookUtils.isLoggedIn()) {
            subscriptions.add(rxFacebook.requestLogin().subscribe(loginResult -> {
              newChatPresenter.loadFBContactsInvite();
              hideNextNotif();
            }));
          } else {
            newChatPresenter.loadFBContactsInvite();
            hideNextNotif();
          }
          break;
        case NotificationModel.POPUP_UPLOAD_PICTURE:
          subscriptions.add(
              DialogFactory.showBottomSheetForCamera(activity).subscribe(labelType -> {
                hideView();
                if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
                  subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
                    newChatPresenter.updateUser(currentUser.getId(), currentUser.getUsername(),
                        currentUser.getDisplayName(), uri.toString());
                  }));
                } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
                  subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
                    newChatPresenter.updateUser(currentUser.getId(), currentUser.getUsername(),
                        currentUser.getDisplayName(), uri.toString());
                  }));
                }
              }));
          break;
      }
    }));
  }

  private void hideNextNotif() {
    if (pageListener.getPositionViewPage() < data.size()) {
      if (pageListener.getPositionViewPage() == data.size() - 1) {
        subscriptions.add(Observable.timer((300), TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> hideView()));
      }

      pager.setCurrentItem(pageListener.getPositionViewPage() + 1);
    }
  }

  ////////////////////
  // PRIVATE METHOD //
  ////////////////////

  private void initView(Context context) {
    this.context = context;
    disposeView = false;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    v = inflater.inflate(R.layout.activity_test, this, true);
    unbinder = ButterKnife.bind(this);

    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());

    textDismiss.setOnTouchListener((v, event) -> gestureScanner.onTouchEvent(event));

    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    if (hasSoftKeys.get()) {
      UIUtils.changeBottomMarginOfView(textDismiss, screenUtils.dpToPx(50));
    }

    newChatMVPViewAdapter = new NewChatMVPViewAdapter() {
      @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
        Timber.d("onShortcutCreatedSuccess" + shortcut.getId());
      }

      @Override public void onLoadFBContactsInvite(List<Contact> contactList) {
        Timber.d("onShortcutCreatedSuccess" + contactList.size());
        tagNotifyFbFriend();
        ArrayList<String> array = new ArrayList<>();
        for (Contact c : contactList) {
          array.add(c.getId());
        }
        rxFacebook.notifyFriends(context, array);
      }
    };
  }

  private void animateView() {
    textDismiss.setVisibility(INVISIBLE);
    dotsContainer.setVisibility(INVISIBLE);
    pager.setTranslationY(-screenUtils.getHeightPx());
    pager.setVisibility(VISIBLE);
    bgView.setAlpha(0f);

    bgView.animate().setDuration(NOTIF_ANIM_DURATION_ENTER).alpha(1f).withEndAction(new Runnable() {
      @Override public void run() {
        pager.animate()
            .translationY(0f)
            .setDuration(NOTIF_ANIM_DURATION_ENTER)
            .setInterpolator(new OvershootInterpolator(1.15f))
            .setListener(new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                textDismiss.setVisibility(VISIBLE);
                dotsContainer.setVisibility(VISIBLE);
              }
            })
            .start();
      }
    }).start();
  }

  Animation slideOutAnimation;

  public void hideView() {
    if (listener != null) listener.onFinishView();
    disposeView = true;
    pager.setOnTouchListener(null);
    slideOutAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.notif_container_exit_animation);
    setAnimation(slideOutAnimation);
    slideOutAnimation.setFillAfter(true);
    slideOutAnimation.setDuration(NOTIF_ANIM_DURATION_ENTER);
    slideOutAnimation.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        dispose();
      }
    });
    pager.startAnimation(slideOutAnimation);
    bgView.animate().setDuration(NOTIF_ANIM_DURATION_ENTER).alpha(0f).start();
    textDismiss.setVisibility(INVISIBLE);
  }

  public void dispose() {
    setVisibility(GONE);
    post(() -> decorView.removeView(v));
    clearAnimation();
    subscriptions.unsubscribe();
  }

  private void initDots(int dotsNbr) {
    int sizeDot = context.getResources().getDimensionPixelSize(R.dimen.waiting_view_dot_size);
    for (int i = 0; i < dotsNbr; i++) {
      View v = new View(context);
      v.setTag(DOTS_TAG_MARKER + i);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.setMargins(0, 0, 15, 0);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      dotsContainer.addView(v);
      if (i == 0) {
        v.setBackgroundResource(R.drawable.shape_oval_white);
        v.setScaleX(1.2f);
        v.setScaleY(1.2f);
      } else {
        v.setBackgroundResource(R.drawable.shape_oval_white50);
        v.setScaleX(1f);
        v.setScaleY(1f);
      }
    }
  }

  private void tagAddedFriend() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.ADDFRIEND, TagManagerUtils.CHALLENGER_ACTION_ADDED);
    tagManager.trackEvent(TagManagerUtils.POPUP, properties);
  }

  private void tagNotifyFbFriend() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.FACEBOOK_INVITE, TagManagerUtils.ACTION_INVITED);
    tagManager.trackEvent(TagManagerUtils.POPUP, properties);
  }

  private void tagCancelAction() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.ADDFRIEND, TagManagerUtils.CHALLENGER_ACTION_CANCELLED);
    tagManager.trackEvent(TagManagerUtils.POPUP, properties);
  }

  ///////////////////
  //  GESTURE IMP  //
  ///////////////////

  public void setNotifEventListener(OnFinishEventListener eventListener) {
    listener = eventListener;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    newChatPresenter.onViewAttached(newChatMVPViewAdapter);
  }

  public static class PageListener extends ViewPager.SimpleOnPageChangeListener {
    private LinearLayout dotsContainer;
    private int positionViewPager;

    public PageListener(LinearLayout dotsContainer) {
      this.dotsContainer = dotsContainer;
    }

    public int getPositionViewPage() {
      return positionViewPager;
    }

    public void onPageSelected(int position) {
      this.positionViewPager = position;
      positionViewPager = position;
      for (int i = 0; i < dotsContainer.getChildCount(); i++) {
        View v = dotsContainer.getChildAt(i);
        if (v.getTag().toString().startsWith(DOTS_TAG_MARKER + position)) {
          v.setBackgroundResource(R.drawable.shape_oval_white);
          v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
        } else {
          v.setBackgroundResource(R.drawable.shape_oval_white50);
          v.setScaleX(1f);
          v.setScaleY(1f);
        }
      }
    }
  }

  private class TapGestureListener implements GestureDetector.OnGestureListener {

    @Override public boolean onDown(MotionEvent e) {
      if (pager.getCurrentItem() == data.size() - 1) {
        hideView();
        tagCancelAction();
      } else {
        pager.setCurrentItem(pageListener.getPositionViewPage() + 1);
      }

      return true;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }

  public interface OnFinishEventListener {
    void onFinishView();
  }
}
