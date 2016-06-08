package com.tribe.app.presentation.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A layout which handles the preview aspect ratio.
 */
public class CameraView extends FrameLayout {

    public static final String TAG = "CameraView";
    public static final int DURATION = 200;

    @Inject ScreenUtils screenUtils;

    @BindView(R.id.cameraManager)
    CameraManager cameraManager;

    // Variables
    private double aspectRatio;

    // Resources
    private int marginLeftInit;
    private int marginBottomInit;

    // Drag camera
    private int downX, downY, xDelta, yDelta;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_camera, this, true);

        ButterKnife.bind(this);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
        marginLeftInit = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        marginBottomInit = context.getResources().getDimensionPixelOffset(R.dimen.nav_layout_height);

        setAspectRatio(3.0 / 2.0);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMargins();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void onPause() {
        cameraManager.pause();
    }

    public void onResume() {
        cameraManager.resume();
    }

    public void setAspectRatio(double ratio) {
        if (ratio <= 0.0) {
            throw new IllegalArgumentException();
        }

        if (aspectRatio != ratio) {
            aspectRatio = ratio;
            requestLayout();
        }
    }

    public void setMargins() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = marginLeftInit;
        layoutParams.topMargin = ((View) getParent()).getHeight() - getMeasuredHeight() - marginBottomInit;
        setLayoutParams(layoutParams);

    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Scale the preview while keeping the aspect ratio
        int fullWidth = screenUtils.getWidth() / 3;
        int fullHeight =  (int) (screenUtils.getWidth() / 3 * aspectRatio);

        setMeasuredDimension(fullWidth, fullHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec((int) fullWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) fullHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int touchX = (int) event.getRawX();
        final int touchY = (int) event.getRawY();

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = touchX - layoutParams.leftMargin;
                yDelta = touchY - layoutParams.topMargin;
                downX = touchX;
                downY = touchY;
                break;

            case MotionEvent.ACTION_UP:
                snapCamera();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                layoutParams.leftMargin = touchX - xDelta;
                layoutParams.topMargin = touchY - yDelta;
                setLayoutParams(layoutParams);

                break;
        }

        invalidate();
        return true;
    }

    private void snapCamera() {
        int [] locationLayoutCamera = new int[2];
        getLocationOnScreen(locationLayoutCamera);

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
        int leftMargin = 0;
        int topMargin = 0;

        if (lp.leftMargin > screenUtils.getWidth() / 4) {
            leftMargin = screenUtils.getWidth() - getWidth() - marginLeftInit;
        } else {
            leftMargin = marginLeftInit;
        }

        if (lp.topMargin > screenUtils.getHeight() / 4) {
            topMargin = ((View) getParent()).getHeight() - getMeasuredHeight() - marginBottomInit;
        } else {
            topMargin = marginLeftInit;
        }

        ValueAnimator animator = ValueAnimator.ofInt(lp.leftMargin, leftMargin);
        animator.setDuration(DURATION);
        animator.addUpdateListener(animation -> {
            lp.leftMargin = (Integer) animation.getAnimatedValue();
            setLayoutParams(lp);
        });
        animator.start();

        ValueAnimator animator2 = ValueAnimator.ofInt(lp.topMargin, topMargin);
        animator2.setDuration(DURATION);
        animator2.addUpdateListener(animation -> {
            lp.topMargin = (Integer) animation.getAnimatedValue();
            setLayoutParams(lp);
        });
        animator2.start();
    }
}
