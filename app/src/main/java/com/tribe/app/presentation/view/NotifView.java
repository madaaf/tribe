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
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactFB;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
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
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.utils.facebook.RxFacebook.MAX_FRIEND_INVITE;
import static com.tribe.app.presentation.utils.facebook.RxFacebook.MAX_SIZE_PAGINATION;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotifView extends FrameLayout {

  private final static String DOTS_TAG_MARKER = "DOTS_TAG_MARKER_";

  private final static int NOTIF_ANIM_DURATION_ENTER = 500;

  private static ViewGroup decorView;
  private static View v;
  private static boolean disposeView = true;

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
  @Inject MessagePresenter messagePresenter;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onDismiss = PublishSubject.create();

  public NotifView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public NotifView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public static boolean isDisplayed() {
    return !disposeView;
  }

  public void overrideBackground(Drawable background) {
    bgView.setBackground(background);
  }

  public void show(Activity activity, List<NotificationModel> list) {
    disposeView = false;

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
              newChatPresenter.getContactFbList(MAX_FRIEND_INVITE);
              hideNextNotif();
            }));
          } else {
            newChatPresenter.getContactFbList(MAX_FRIEND_INVITE);
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
        case NotificationModel.POPUP_POKE:
          Score score = notificationModel.getScore();
          String[] userIds = new String[1];
          userIds[0] = score.getUser().getId();
          String intent = (score.isAbove()) ? MessagePoke.INTENT_FUN : MessagePoke.INTENT_JEALOUS;
          messagePresenter.createPoke(userIds, score.getEmoticon(), score.getGame().getId(),
              intent);
          Toast.makeText(context,
              context.getString(R.string.poke_sent_confirmation, score.getUser().getDisplayName()),
              Toast.LENGTH_SHORT).show();
          hideNextNotif();
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
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    v = inflater.inflate(R.layout.activity_test, this, true);
    unbinder = ButterKnife.bind(this);

    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());

    pager.setOnTouchListener((v, event) -> {
      gestureScanner.onTouchEvent(event);
      return super.onTouchEvent(event);
    });

    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    if (hasSoftKeys.get()) {
      UIUtils.changeBottomMarginOfView(textDismiss, screenUtils.dpToPx(50));
    }

    newChatMVPViewAdapter = new NewChatMVPViewAdapter() {
      @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
        Timber.d("onShortcutCreatedSuccess " + shortcut.getId());
      }

      @Override public void onLoadFBContactsFbInvite(List<ContactFB> contactList) {
        super.onLoadFBContactsFbInvite(contactList);
        Timber.d("onLoadFBContactsFbInvite " + contactList.size());
        tagNotifyFbFriend();
        ArrayList<String> array = new ArrayList<>();
        for (Contact c : contactList) {
          array.add(c.getId());
        }
        int rest = array.size() % MAX_SIZE_PAGINATION;
        int nbrOfArray = array.size() / MAX_SIZE_PAGINATION;
        if (rest == 0) {
          for (int i = 0; i < nbrOfArray; i++) {
            ArrayList<String> splitArray = splitList((i * MAX_SIZE_PAGINATION),
                (i * MAX_SIZE_PAGINATION) + MAX_SIZE_PAGINATION, array);
            rxFacebook.notifyFriends(context, splitArray);
          }
        } else {
          for (int i = 0; i < nbrOfArray; i++) {
            ArrayList<String> splitArray = splitList((i * MAX_SIZE_PAGINATION),
                (i * MAX_SIZE_PAGINATION) + MAX_SIZE_PAGINATION, array);
            rxFacebook.notifyFriends(context, splitArray);
          }
          ArrayList<String> splitArray = splitList(nbrOfArray, nbrOfArray + rest, array);
          rxFacebook.notifyFriends(context, splitArray);
        }
      }
    };
  }

  private ArrayList<String> splitList(int startIndex, int endIndex, ArrayList<String> list) {
    ArrayList<String> array = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      if (i >= startIndex && i < endIndex) {
        array.add(list.get(i));
      }
    }
    return array;
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
    decorView.removeView(v);
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

    private void next() {
      if (pager.getCurrentItem() == data.size() - 1) {
        hideView();
        tagCancelAction();
        onDismiss.onNext(null);
      } else {
        pager.setCurrentItem(pageListener.getPositionViewPage() + 1);
      }
    }

    @Override public boolean onDown(MotionEvent e) {
      //  next();
      return false;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      next();
      return true;
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

  /**
   * OBSERVABLES
   */

  public Observable<Void> onDismiss() {
    return onDismiss;
  }
}
