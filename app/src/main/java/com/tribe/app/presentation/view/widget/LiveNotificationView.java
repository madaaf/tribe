package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LiveNotificationView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.avatar)
    AvatarLiveView avatarLiveView;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springHeight;
    //private HeightSpringListener springHeightListener;

    // RESOURCES
    private int minHeight = 0, maxHeight = 0;
    private int thresholdOpen;

    // VARIABLES
    private Unbinder unbinder;
    private ViewGroup.LayoutParams params;
    private int startHeight, startX, startY;
    private int activePointerId;
    private VelocityTracker velocityTracker;
    private int touchSlop;

    public LiveNotificationView(Context context) {
        super(context);
        init(context, null);
    }

    public LiveNotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResources();
        init(context, attrs);
    }

    private void initResources() {
        minHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_min_height);
        maxHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_max_height);
        thresholdOpen = getResources().getDimensionPixelSize(R.dimen.threshold_dismiss);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_live_notification, this, true);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        unbinder = ButterKnife.bind(this);


        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LiveNotificationView, 0, 0);

        try {
            setTitle(attr.getString(R.styleable.LiveNotificationView_notificationTitle));
        } finally {
            attr.recycle();
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                params = getLayoutParams();
            }
        });

        setBackgroundResource(R.drawable.bg_live_double_background);
        setClickable(false);
        setMinimumHeight(minHeight);
    }

    public void setTitle(String title) {
        txtTitle.setText(title);
    }

    public void loadAvatar(String url) {
        avatarLiveView.load(url);
    }

    ///////////////////////
    //    ANIMATIONS     //
    ///////////////////////

//    private class HeightSpringListener extends SimpleSpringListener {
//        @Override
//        public void onSpringUpdate(Spring spring) {
//            if (ViewCompat.isAttachedToWindow(LiveNotificationView.this)) {
//                int value = (int) spring.getCurrentValue();
//                changeHeight(value);
//            }
//        }
//    }
//
//    private void changeHeight(int value) {
//        params.height = value;
//    }
//
//    private boolean applyHeightWithTension(float offsetY) {
//        float totalDragDistance = getTotalDragDistance();
//        final float scrollTop = offsetY * DRAG_RATE;
//        currentDragPercent = scrollTop / totalDragDistance;
//
//        if (currentDragPercent < 0) {
//            return false;
//        }
//
//        currentOffsetTop = computeHeightWithTension(scrollTop, totalDragDistance);
//        changeHeight(currentOffsetTop);
//
//        return true;
//    }
//
//    private float getTotalDragDistance() {
//        return getHeight() / 8;
//    }
//
//    private int computeHeightWithTension(float scrollDist, float totalDragDistance) {
//        float boundedDragPercent = Math.min(1f, Math.abs(currentDragPercent));
//        float extraOS = Math.abs(scrollDist) - totalDragDistance;
//        float slingshotDist = totalDragDistance;
//        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
//        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
//        float extraMove = (slingshotDist) * tensionPercent / 2;
//        return (int) ((slingshotDist * boundedDragPercent) + extraMove);
//    }

    ///////////////////////
    //    OBSERVABLES    //
    ///////////////////////


}
