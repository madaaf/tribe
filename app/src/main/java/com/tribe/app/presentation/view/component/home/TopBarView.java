package com.tribe.app.presentation.view.component.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.EmojiGameView;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/15/2016.
 */
public class TopBarView extends FrameLayout {

  private static final int MIN_NUMBER_CALL = 3;
  private static final int DURATION_FADE = 100;
  private static final float OVERSHOOT_LIGHT = 0.5f;
  private static final int DURATION = 200;
  private static final int DURATION_MEDIUM = 450;
  private static final int CLICK_ACTION_THRESHOLD = 5;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @Inject StateManager stateManager;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;

  @BindView(R.id.txtEmojiGame) EmojiGameView txtEmojiGame;

  @BindView(R.id.btnSearch) ViewGroup btnSearch;

  @BindView(R.id.imgSearch) ImageView imgSearch;

  @BindView(R.id.txtSearch) TextViewFont txtSearch;

  @BindView(R.id.editTextSearch) EditTextFont editTextSearch;

  @BindView(R.id.txtNewContacts) TextViewFont txtNewContacts;

  @BindView(R.id.imgClose) View imgClose;

  @BindView(R.id.viewTopBarContainer) FrameLayout viewTopBarContainer;

  @BindView(R.id.btnSyncContacts) FrameLayout btnSyncContacts;

  @BindView(R.id.imgSyncContacts) ImageView imgSyncContacts;

  @BindView(R.id.imgBack) ImageView imgBack;

  // VARIABLES
  private float startX, startY = 0;
  private boolean searchMode = false;
  private GradientDrawable drawableBGNewContacts;
  private int nbContacts = 0;
  private boolean hasNewContacts = false;
  private boolean open = false;
  private boolean shouldForceRed = false;

  // RESOURCES
  private int avatarSize;
  private int clickActionThreshold;
  private int marginSmall;
  private RxPermissions rxPermissions;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onSearch = PublishSubject.create();
  private PublishSubject<Void> clickProfile = PublishSubject.create();
  private PublishSubject<Void> clickBack = PublishSubject.create();
  private PublishSubject<Boolean> onOpenCloseSearch = PublishSubject.create();
  private PublishSubject<Void> onSyncContacts = PublishSubject.create();

  public TopBarView(Context context) {
    super(context);
    init(context, null);
  }

  public TopBarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    unbinder.unbind();

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_top_bar, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();

    reloadUserUI();

    super.onFinishInflate();
  }

  private void init(Context context, AttributeSet attrs) {
    rxPermissions = new RxPermissions((Activity) context);
  }

  private void initUI() {
    drawableBGNewContacts = new GradientDrawable();
    drawableBGNewContacts.setShape(GradientDrawable.RECTANGLE);
    drawableBGNewContacts.setCornerRadius(screenUtils.dpToPx(5));
    drawableBGNewContacts.setColor(Color.RED);
    txtNewContacts.setBackgroundDrawable(drawableBGNewContacts);

    imgClose.setTranslationX(screenUtils.getWidthPx() >> 1);
    imgClose.setAlpha(1);

    editTextSearch.setEnabled(false);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        MarginLayoutParams params = (MarginLayoutParams) btnSearch.getLayoutParams();
        params.rightMargin = getMarginRightSearch();
        params.leftMargin = getMarginLeftSearch();
        btnSearch.setLayoutParams(params);

        params = (MarginLayoutParams) imgBack.getLayoutParams();
        params.rightMargin = getMarginRightBtnClose();
        imgBack.setLayoutParams(params);

        params = (MarginLayoutParams) viewAvatar.getLayoutParams();
        params.leftMargin = getMarginLeftAvatar();
        viewAvatar.setLayoutParams(params);
      }
    });

    subscriptions.add(RxTextView.textChanges(editTextSearch)
        .map(CharSequence::toString)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSearch));
    if (!PermissionUtils.hasPermissionsContact(rxPermissions)) {
      displaySyncBtn();
    }
  }

  public void reloadUserUI() {
    viewAvatar.load(user.getProfilePicture());
    txtEmojiGame.setEmojiList(user.getEmojiLeaderGameList());
  }

  private void displaySyncBtn() {
    btnSyncContacts.setClickable(true);
    btnSyncContacts.setVisibility(VISIBLE);
    txtNewContacts.setVisibility(INVISIBLE);
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
            //viewAvatar.performClick();
          } else if (isAClickInView(imgBack, (int) startX, (int) startY)) {
            imgBack.onTouchEvent(event);
            imgBack.performClick();
          } else if (isAClickInView(btnSyncContacts, (int) startX, (int) startY)) {
            if (btnSyncContacts.isClickable()) {
              btnSyncContacts.onTouchEvent(event);
              btnSyncContacts.performClick();
            }
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
        break;
      }

      default:
        if (isAClickInView(viewAvatar, (int) event.getRawX(), (int) event.getRawY())) {
          viewAvatar.onTouchEvent(event);
        } else if (isAClickInView(imgBack, (int) event.getRawX(), (int) event.getRawY())) {
          imgBack.onTouchEvent(event);
        } else if (isAClickInView(btnSyncContacts, (int) event.getRawX(), (int) event.getRawY())) {
          btnSyncContacts.onTouchEvent(event);
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

  @OnClick(R.id.imgBack) void clickBack() {
    clickBack.onNext(null);
  }

  @OnClick(R.id.btnSyncContacts) void btnSyncContactsClick() {
    onSyncContacts.onNext(null);
  }

  public void onSyncStart() {
    txtNewContacts.setVisibility(INVISIBLE);
    btnSyncContacts.setBackground(
        ContextCompat.getDrawable(getContext(), R.drawable.shape_rect_white40_sync_corner));
    imgSyncContacts.setImageResource(R.drawable.picto_synchronizing_contacts);
    Animation anim =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    imgSyncContacts.startAnimation(anim);
  }

  public void onSyncDone() {
    btnSyncContacts.clearAnimation();
    btnSyncContacts.setVisibility(GONE);
    btnSyncContacts.setClickable(false);
    txtNewContacts.setVisibility(VISIBLE);
  }

  public void onSyncError() {
    displaySyncBtn();
  }

  @OnClick(R.id.btnSearch) void animateSearch() {
    if (searchMode) return;

    screenUtils.showKeyboard(editTextSearch, 0);

    onOpenCloseSearch.onNext(true);

    if (nbContacts > 0) {
      showNewContacts(false);
    }

    searchMode = true;
    btnSearch.setClickable(false);

    showView(imgClose, new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        editTextSearch.setEnabled(true);
        imgClose.animate().setListener(null).start();
      }
    });

    hideView(imgBack, false);
    hideView(viewAvatar, true);
    hideView(txtEmojiGame, true);

    AnimationUtils.animateLeftMargin(btnSearch, marginSmall, DURATION, null);
    AnimationUtils.animateRightMargin(btnSearch, imgClose.getWidth(), DURATION);

    txtSearch.setVisibility(View.INVISIBLE);
  }

  @OnClick(R.id.imgClose) public void closeSearch() {
    shouldForceRed = false;

    onOpenCloseSearch.onNext(false);
    onSearch.onNext("");

    searchMode = false;
    screenUtils.hideKeyboard(editTextSearch);
    editTextSearch.getText().clear();
    editTextSearch.setEnabled(false);
    btnSearch.setClickable(true);

    showView(imgBack, null);
    hideView(imgClose, false);
    showView(viewAvatar, null);
    showView(txtEmojiGame, null);

    if (nbContacts > 0) {
      showNewContacts(true);
    }

    AnimationUtils.animateLeftMargin(btnSearch, getMarginLeftSearch(), DURATION, null);
    AnimationUtils.animateRightMargin(btnSearch, getMarginRightSearch(), DURATION);

    subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> txtSearch.setVisibility(View.VISIBLE)));
  }

  private int getMarginRightSearch() {
    return imgBack.getMeasuredWidth();
  }

  private int getMarginLeftSearch() {
    return viewAvatar.getWidth() + marginSmall + getMarginLeftAvatar();
  }

  private int getMarginLeftAvatar() {
    return marginSmall;
  }

  private int getMarginRightBtnClose() {
    return 0;
  }

  private void hideView(View view, boolean left) {
    int translateX = screenUtils.getWidthPx() >> 1;

    view.animate()
        .alpha(0)
        .translationX(left ? -translateX : translateX)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .setDuration(DURATION)
        .start();
  }

  private void showView(View view, Animator.AnimatorListener listener) {
    view.animate()
        .alpha(1)
        .translationX(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .setListener(listener)
        .setDuration(DURATION)
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

  private void showNewContacts(boolean show) {
    if (searchMode || txtNewContacts == null) return;

    txtNewContacts.animate()
        .alpha(show ? 1 : 0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  public void initNewContactsObs(Observable<Pair<Integer, Boolean>> obsContactList) {
    subscriptions.add(
        obsContactList.observeOn(AndroidSchedulers.mainThread()).subscribe(integerBooleanPair -> {
          nbContacts = integerBooleanPair.first;
          hasNewContacts = integerBooleanPair.second;

          if (nbContacts > 0) {
            showNewContacts(true);
            txtNewContacts.setText("" + nbContacts);
            if (hasNewContacts || shouldForceRed) {
              drawableBGNewContacts.setColor(
                  ContextCompat.getColor(getContext(), R.color.red_new_contacts));
            } else {
              drawableBGNewContacts.setColor(
                  ContextCompat.getColor(getContext(), R.color.grey_new_contacts));
            }
          } else {
            showNewContacts(false);
          }
        }));
  }

  public boolean isSearchMode() {
    return searchMode;
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<String> onSearch() {
    return onSearch;
  }

  public Observable<Void> onSyncContacts() {
    return onSyncContacts;
  }

  public Observable<Void> onClickProfile() {
    return clickProfile;
  }

  public Observable<Void> onBack() {
    return clickBack;
  }

  public Observable<Boolean> onOpenCloseSearch() {
    return onOpenCloseSearch;
  }
}

