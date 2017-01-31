package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/15/2016.
 */
public class TopBarView extends FrameLayout {

  private static final float OVERSHOOT_LIGHT = 0.5f;
  private static final int DURATION = 300;
  private static final int DURATION_MEDIUM = 450;
  private static final int CLICK_ACTION_THRESHOLD = 5;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.viewAvatar) AvatarLiveView viewAvatar;

  @BindView(R.id.btnNew) View btnNew;

  @BindView(R.id.btnSearch) ViewGroup btnSearch;

  @BindView(R.id.editTextSearch) EditTextFont editTextSearch;

  @BindView(R.id.imgClose) View imgClose;

  // VARIABLES
  private float startX, startY = 0;
  private boolean searchMode = false;

  // RESOURCES
  private int avatarSize;
  private int clickActionThreshold;
  private int marginSmall;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> clickNew = PublishSubject.create();
  private PublishSubject<String> onSearch = PublishSubject.create();
  private PublishSubject<Void> clickProfile = PublishSubject.create();

  public TopBarView(Context context) {
    super(context);
    init(context, null);
  }

  public TopBarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_top_bar, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();

    super.onFinishInflate();
  }

  private void init(Context context, AttributeSet attrs) {
  }

  private void initUI() {
    imgClose.setTranslationX(screenUtils.getWidthPx() >> 1);
    imgClose.setAlpha(1);

    editTextSearch.setEnabled(false);

    btnSearch.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

          @Override public void onGlobalLayout() {
            btnSearch.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            MarginLayoutParams params = (MarginLayoutParams) btnSearch.getLayoutParams();
            params.rightMargin = btnNew.getWidth() + 2 * marginSmall;
            params.leftMargin = viewAvatar.getWidth() + 2 * marginSmall;
            btnSearch.setLayoutParams(params);
          }
        });

    viewAvatar.load(user.getProfilePicture());

    subscriptions.add(RxTextView.textChanges(editTextSearch)
        .map(CharSequence::toString)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSearch));
  }

  private void initResources() {
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
    clickActionThreshold = screenUtils.dpToPx(CLICK_ACTION_THRESHOLD);
    marginSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_UP: {
        if (isAClick(startX, event.getRawX(), startY, event.getRawY())) {
          if (isAClickInView(viewAvatar, (int) startX, (int) startY)) {
            viewAvatar.onTouchEvent(event);
            viewAvatar.performClick();
          } else if (isAClickInView(btnNew, (int) startX, (int) startY)) {
            btnNew.onTouchEvent(event);
            btnNew.performClick();
          } else if (isAClickInView(btnSearch, (int) startX, (int) startY)) {
            btnSearch.onTouchEvent(event);
            btnSearch.performClick();
          } else if (isAClickInView(imgClose, (int) startX, (int) startY)) {
            imgClose.onTouchEvent(event);
            imgClose.performClick();
          }
        }

        break;
      }

      case MotionEvent.ACTION_DOWN: {
        startX = event.getRawX();
        startY = event.getRawY();
      }

      default:
        if (isAClickInView(viewAvatar, (int) event.getRawX(), (int) event.getRawY())) {
          viewAvatar.onTouchEvent(event);
        } else if (isAClickInView(btnNew, (int) event.getRawX(), (int) event.getRawY())) {
          btnNew.onTouchEvent(event);
        } else if (isAClickInView(btnSearch, (int) event.getRawX(), (int) event.getRawY())) {
          btnSearch.onTouchEvent(event);
        } else if (isAClickInView(imgClose, (int) event.getRawX(), (int) event.getRawY())) {
          imgClose.onTouchEvent(event);
        }

        break;
    }

    return false;
  }

  @OnClick(R.id.viewAvatar) void launchProfileSettings() {
    clickProfile.onNext(null);
  }

  @OnClick(R.id.btnNew) void launchInvites() {
    clickNew.onNext(null);
  }

  @OnClick(R.id.btnSearch) void animateSearch() {
    searchMode = true;
    btnSearch.setClickable(false);

    showView(imgClose, new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        editTextSearch.setEnabled(true);
        screenUtils.showKeyboard(editTextSearch, 0);
        imgClose.animate().setListener(null).start();
      }
    });
    hideView(btnNew, false);
    hideView(viewAvatar, true);

    AnimationUtils.animateLeftMargin(btnSearch, marginSmall, DURATION, null);
    AnimationUtils.animateRightMargin(btnSearch, imgClose.getWidth() + 2 * marginSmall, DURATION);
  }

  @OnClick(R.id.imgClose) public void closeSearch() {
    onSearch.onNext(null);

    searchMode = false;
    screenUtils.hideKeyboard(editTextSearch);
    editTextSearch.getText().clear();
    editTextSearch.setEnabled(false);
    btnSearch.setClickable(true);

    showView(btnNew, null);
    hideView(imgClose, false);
    showView(viewAvatar, null);

    AnimationUtils.animateLeftMargin(btnSearch, viewAvatar.getWidth() + 2 * marginSmall, DURATION,
        null);
    AnimationUtils.animateRightMargin(btnSearch, btnNew.getWidth() + 2 * marginSmall, DURATION);
  }

  private void hideView(View view, boolean left) {
    int translateX = screenUtils.getWidthPx() >> 1;

    view.animate()
        .alpha(0)
        .translationX(left ? -translateX : translateX)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .setDuration(DURATION_MEDIUM)
        .start();
  }

  private void showView(View view, Animator.AnimatorListener listener) {
    view.animate()
        .alpha(1)
        .translationX(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .setListener(listener)
        .setDuration(DURATION_MEDIUM)
        .start();
  }

  private boolean isAClick(float startX, float endX, float startY, float endY) {
    float differenceX = Math.abs(startX - endX);
    float differenceY = Math.abs(startY - endY);

    if (differenceX > clickActionThreshold || differenceY > clickActionThreshold) {
      return false;
    }

    return true;
  }

  private boolean isAClickInView(View v, int x, int y) {
    final int location[] = { 0, 0 };
    v.getLocationOnScreen(location);
    Rect rect =
        new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());

    if (!rect.contains(x, y)) {
      return false;
    }

    return true;
  }

  public boolean isSearchMode() {
    return searchMode;
  }

  @Override public boolean dispatchKeyEventPreIme(KeyEvent event) {
    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

    if (imm.isActive() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && searchMode) {
      closeSearch();
      return true;
    }

    return super.dispatchKeyEventPreIme(event);
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onClickNew() {
    return clickNew;
  }

  public Observable<String> onSearch() {
    return onSearch;
  }

  public Observable<Void> onClickProfile() {
    return clickProfile;
  }
}

