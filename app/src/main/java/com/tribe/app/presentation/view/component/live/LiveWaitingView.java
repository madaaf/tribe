package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

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
    private Recipient recipient;
    private Boolean isMeasuring = false;
    private @LiveRoomView.TribeRoomViewType int type = LiveRoomView.GRID;
    private TextViewFont txtDropInTheLive;

    private CompositeSubscription subscriptions = new CompositeSubscription();

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
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
        circlePaint.setAntiAlias(true);

        circleViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        circleViewParams.gravity = Gravity.CENTER;

        int avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_medium);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin_smaller);
        FrameLayout.LayoutParams imageAvatarParams = new FrameLayout.LayoutParams(avatarSize, avatarSize);
        imageAvatarParams.leftMargin = imageAvatarParams.topMargin
                = imageAvatarParams.bottomMargin = imageAvatarParams.rightMargin = margin;
        imageAvatarParams.gravity = Gravity.CENTER;

        circleView = new CircleView(getContext());
        viewAvatar = new AvatarView(getContext());

        addView(circleView, circleViewParams);
        addView(viewAvatar, imageAvatarParams);

        viewAvatar.setVisibility(View.GONE);

        setBackground(null);

        txtDropInTheLive = new TextViewFont(getContext());
        FrameLayout.LayoutParams txtLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        txtLayoutParams.gravity = Gravity.CENTER;
        TextViewCompat.setTextAppearance(txtDropInTheLive, R.style.Caption_Two_Black40);
        txtDropInTheLive.setCustomFont(getContext(), "Roboto-Bold.ttf");
        txtDropInTheLive.setText(R.string.live_drop_friend);
        addView(txtDropInTheLive, txtLayoutParams);
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

//    private int getAvatarSize() {
//        if (type == LiveRoomView.LINEAR) {
//            Timber.d("getAvatarSize : " + getMeasuredHeight() / 3);
//            Timber.d("getMeasureHeight : " + getMeasuredHeight());
//            if (getMeasuredHeight() != 0) return (getMeasuredHeight() / 3);
//            else return 0;
//        } else {
//            Timber.d("getAvatarSize : " + getMeasuredWidth() / 3);
//            Timber.d("getMeasureWidth : " + getMeasuredWidth());
//            if (getMeasuredHeight() != 0) return (getMeasuredHeight() / 3);
//            else return 0;
//        }
//    }

    public void startPulse() {
        txtDropInTheLive.setVisibility(View.GONE);
        viewAvatar.setVisibility(View.VISIBLE);

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

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
        viewAvatar.load(recipient.getProfilePicture());
    }

    public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
        this.type = type;
    }

    public void release() {
        if (circleView != null) circleView.clearAnimation();

        if (circleAnimator != null) {
            circleAnimator.removeAllUpdateListeners();
            circleAnimator.cancel();
        }
    }
}
