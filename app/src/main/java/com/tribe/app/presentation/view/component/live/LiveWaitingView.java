package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;

import javax.inject.Inject;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveWaitingView extends FrameLayout {

    private Rect rect = new Rect();
    private Paint circlePaint = new Paint();
    private CircleView circleView;
    private FrameLayout.LayoutParams circleViewParams;
    private AvatarView viewAvatar;
    private ValueAnimator circleAnimator;

    @Inject
    ScreenUtils screenUtils;

    public LiveWaitingView(Context context) {
        super(context);
        init();
    }

    public LiveWaitingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveWaitingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int avatarSize = getAvatarSize();
        UIUtils.changeSizeOfView(viewAvatar, avatarSize);
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
        circlePaint.setAntiAlias(true);

        circleViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        circleViewParams.gravity = Gravity.CENTER;

        int avatarSize = getAvatarSize();
        LayoutParams imageAvatarParams = new LayoutParams(avatarSize, avatarSize);
        imageAvatarParams.gravity = Gravity.CENTER;

        circleView = new CircleView(getContext());
        viewAvatar = new AvatarView(getContext());

        addView(circleView, circleViewParams);
        addView(viewAvatar, imageAvatarParams);

        circleView.setVisibility(View.GONE);
        viewAvatar.setVisibility(View.GONE);

        setBackground(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (circleAnimator == null) {
            rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

            circleView.setRect(rect);
            circleView.setPaint(circlePaint);
        }
    }

    private int getAvatarSize() {
        return Math.max(getMeasuredWidth() / 3, screenUtils.getWidthPx() / 3);
    }

    public void startPulse() {
        int finalHeight = getMeasuredHeight();

        circleAnimator = ValueAnimator.ofInt(0, finalHeight);
        circleAnimator.setDuration(1500);
        circleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        circleAnimator.setRepeatMode(ValueAnimator.RESTART);
        circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        circleAnimator.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            circleView.setRadius(value);
        });

        circleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                setColor(circlePaint.getColor());
            }
        });

        circleAnimator.start();
    }

    public void stopPulse() {
        circleAnimator.cancel();
        circleAnimator.removeAllListeners();
    }

    public void setColor(int color) {
        circleView.setBackgroundColor(color);
        circlePaint.setColor(PaletteGrid.getRandomColorExcluding(color));
    }

    public void setAvatarPicture(String url) {
        viewAvatar.load(url);
    }

    public void release() {
        if (circleView != null) circleView.clearAnimation();

        if (circleAnimator != null) {
            circleAnimator.removeAllUpdateListeners();
            circleAnimator.cancel();
        }
    }
}
