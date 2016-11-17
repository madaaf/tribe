package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.rebound.SpringUtil;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/15/2016.
 */
public class TopBarView extends FrameLayout {

    private static final int DURATION_FADE = 100;
    private static final int DURATION = 250;
    private static final int CLICK_ACTION_THRESHOLD = 5;
    private static final int SPINNER_THRESHOLD = 20;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    User user;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.imgSearch)
    ImageView imgSearch;

    @BindView(R.id.syncLayout)
    FrameLayout syncLayout;

    @BindView(R.id.progressRefresh)
    CircularProgressView progressRefresh;

    @BindView(R.id.imgTick)
    View imgTick;

    @BindView(R.id.btnGroup)
    View btnGroup;

    @BindView(R.id.btnInvites)
    View btnInvites;

    // VARIABLES
    float startX, startY = 0;

    // RESOURCES
    private int avatarSize;
    private int marginLeftSyncLayout;
    private int clickActionThreshold;
    private int spinnerThreshold;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public TopBarView(Context context) {
        super(context);
        init(context, null);
    }

    public TopBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_top_bar, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();

        super.onFinishInflate();
    }

    private void init(Context context, AttributeSet attrs) {

    }

    private void initUI() {
        Glide.with(getContext()).load(user.getProfilePicture())
                .override(avatarSize, avatarSize)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgAvatar);

        syncLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                syncLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                marginLeftSyncLayout = ((MarginLayoutParams) syncLayout.getLayoutParams()).leftMargin;
            }
        });
    }

    private void initResources() {
        avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
        clickActionThreshold = screenUtils.dpToPx(CLICK_ACTION_THRESHOLD);
        spinnerThreshold = screenUtils.dpToPx(SPINNER_THRESHOLD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP: {
                System.out.println("UP : start x : " + startX + " / start Y : " + startY + " / x : " + event.getX() + " / y : " + event.getY());

                if (isAClick(startX, event.getX(), startY, event.getY())) {
                    if (isAClickInView(imgAvatar, (int) startX, (int) startY)) imgAvatar.performClick();
                    else if (isAClickInView(imgSearch, (int) startX, (int) startY)) imgSearch.performClick();
                    else if (isAClickInView(syncLayout, (int) startX, (int) startY)) syncLayout.performClick();
                }

                break;
            }

            case MotionEvent.ACTION_DOWN: {
                startX = event.getX();
                startY = event.getY();
            }

            default:
                if (isAClickInView(imgAvatar, (int) event.getX(), (int) event.getY())) imgAvatar.onTouchEvent(event);
                else if (isAClickInView(imgSearch, (int) event.getX(), (int) event.getY())) imgSearch.onTouchEvent(event);
                else if (isAClickInView(syncLayout, (int) event.getX(), (int) event.getY())) syncLayout.onTouchEvent(event);
                break;
        }

        return false;
    }

    @OnClick(R.id.imgSearch)
    void launchSearch() {
        System.out.println("LAUNCH SEARCH");
    }

    @OnClick(R.id.imgAvatar)
    void launchAvatar() {
        System.out.println("LAUNCH AVATAR");
    }

    @OnClick(R.id.syncLayout)
    void launchSyncLayout() {
        System.out.println("LAUNCH SYNC");
    }

    public boolean animatePull(float value, int min, float max) {
        int maxLeftMargin = (getWidth() >> 1) - (syncLayout.getWidth() >> 1);
        int leftMargin = ((int) SpringUtil.mapValueFromRangeToRange(
                value,
                min,
                max,
                marginLeftSyncLayout,
                maxLeftMargin
        ));

        leftMargin = (int) SpringUtil.clamp(leftMargin, 0, maxLeftMargin);

        MarginLayoutParams params = (MarginLayoutParams) syncLayout.getLayoutParams();
        params.leftMargin = leftMargin;
        syncLayout.setLayoutParams(params);

        float alpha = (float) SpringUtil.mapValueFromRangeToRange(
                value,
                min,
                max,
                1,
                0
        );

        btnGroup.setAlpha(alpha);
        btnInvites.setAlpha(alpha);
        imgAvatar.setAlpha(alpha);
        imgSearch.setAlpha(alpha);

        float alphaSpinner = (float) SpringUtil.mapValueFromRangeToRange(
                value,
                min,
                max,
                0,
                10
        );

        System.out.println("Alpha Spinner : " + alphaSpinner);
        imgTick.setAlpha(1 - alphaSpinner);
        progressRefresh.setAlpha(alphaSpinner);

        return leftMargin == maxLeftMargin;
    }

    public void showSpinner() {
        progressRefresh.clearAnimation();
        imgTick.clearAnimation();
        AnimationUtils.fadeIn(progressRefresh, DURATION_FADE);
        AnimationUtils.fadeOut(imgTick, DURATION_FADE);
    }

    public void hideSpinner() {
        progressRefresh.clearAnimation();
        imgTick.clearAnimation();
        AnimationUtils.fadeOut(progressRefresh, DURATION_FADE);
        AnimationUtils.fadeIn(imgTick, DURATION_FADE);
    }

    public void reset() {
        AnimationUtils.animateLeftMargin(syncLayout, marginLeftSyncLayout, DURATION);
        hideSpinner();
        AnimationUtils.fadeIn(btnGroup, DURATION);
        AnimationUtils.fadeIn(btnInvites, DURATION);
        AnimationUtils.fadeIn(imgAvatar, DURATION);
        AnimationUtils.fadeIn(imgSearch, DURATION);
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);

        System.out.println("DIFFERENCE X : " + differenceX + " / DIFFERENCE Y : " + differenceY + " / THRESHOLD : " + clickActionThreshold);

        if (differenceX > clickActionThreshold || differenceY > clickActionThreshold) {
            return false;
        }

        return true;
    }

    private boolean isAClickInView(View v, int x, int y) {
        final int location[] = {0, 0};
        v.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());

        if (!rect.contains(x, y)) {
            return false;
        }

        return true;
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////
}
