package com.tribe.app.presentation.view.tutorial;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.FloatValueAnimatorBuilder;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;

public class FrameLayoutWithHole extends FrameLayout {

    private int CIRCLE_RADIUS = 0;

    private int initCircleRadius;
    private int initPulseCircleRadius;
    private float circleRadius;
    private float pulseCircleRadius;
    private int pulseCircleRadiusAlpha;

    private ScreenUtils screenUtils;
    private TextPaint textPaint;
    private Activity activity;
    private @Tutorial.MotionType int motionType;
    private Paint eraser;

    private Bitmap eraserBitmap;
    private Canvas eraserCanvas;
    private Paint paint;
    private Paint transparentPaint;
    private View viewHole;
    private int radius;
    private int[] pos;
    private Overlay overlay;
    private boolean cleanUpLock = false;
    private Paint circlePaint;
    private Paint pulseCirclePaint;

    private ArrayList<AnimatorSet> animatorSetArrayList;

    public void setViewHole(View viewHole) {
        this.viewHole = viewHole;
        enforceMotionType();
    }

    public void addAnimatorSet(AnimatorSet animatorSet) {
        if (animatorSetArrayList == null) {
            animatorSetArrayList = new ArrayList<>();
        }

        animatorSetArrayList.add(animatorSet);
    }

    private void enforceMotionType() {
        Log.d("tourguide", "enforceMotionType 1");

        if (viewHole != null) {
            Log.d("tourguide", "enforceMotionType 2");

            if (motionType == Tutorial.CLICK_ONLY) {
                Log.d("tourguide", "enforceMotionType 3");
                Log.d("tourguide", "only Clicking");
                viewHole.setOnTouchListener((view, motionEvent) -> {
                    viewHole.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });
            } else if (motionType == Tutorial.SWIPE_ONLY) {
                Log.d("tourguide", "enforceMotionType 4");
                Log.d("tourguide", "only Swiping");
                viewHole.setClickable(false);
            }
        }
    }

    public FrameLayoutWithHole(Activity context, View view) {
        this(context, view, Tutorial.ALLOW_ALL);
    }

    public FrameLayoutWithHole(Activity context, View view, @Tutorial.MotionType int motionType) {
        this(context, view, motionType, new Overlay(context));
    }

    public FrameLayoutWithHole(Activity context, View view, @Tutorial.MotionType int motionType, Overlay overlay) {
        super(context);

        screenUtils = ((AndroidApplication) context.getApplication()).getApplicationComponent().screenUtils();

        activity = context;
        viewHole = view;
        this.overlay = overlay;
        int[] pos = new int[2];
        viewHole.getLocationOnScreen(pos);
        this.pos = pos;

        init(null, 0);
        enforceMotionType();

        int padding = screenUtils.dpToPx(20);

        if (viewHole.getHeight() > viewHole.getWidth()) {
            radius = (viewHole.getHeight() >> 1) + padding;
        } else {
            radius = (viewHole.getWidth() >> 1) + padding;
        }

        CIRCLE_RADIUS = overlay.holeRadius != Overlay.NOT_SET ? overlay.holeRadius + overlay.holeRadiusPulsePadding : radius;

        initCircleRadius = CIRCLE_RADIUS;
        initPulseCircleRadius = (int) (initCircleRadius * 0.2f);
        circleRadius = 0;
        pulseCircleRadius = 0;

        this.motionType = motionType;
    }

    private void init(AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);

        Point size = new Point();
        size.x = screenUtils.getWidthPx();
        size.y = screenUtils.getHeightPx();

        eraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        eraserCanvas = new Canvas(eraserBitmap);

        paint = new Paint();
        paint.setColor(0xcc000000);
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);

        Log.d("tourguide", "getHeight: " + size.y);
        Log.d("tourguide", "getWidth: " + size.x);

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);

        pulseCirclePaint = new Paint();
        pulseCirclePaint.setStrokeWidth(screenUtils.dpToPx(1f));
        pulseCirclePaint.setAntiAlias(true);
        pulseCirclePaint.setColor(Color.WHITE);

        if (overlay.imgOverlay != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(overlay.imgOverlay);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(overlay.imgOverlaySize, overlay.imgOverlaySize);
            params.leftMargin = pos[0] + overlay.imgOverlayOffsetX;
            params.topMargin = pos[1] + overlay.imgOverlayOffsetY;
            addView(imageView, params);
        }

        expandAnimation.start();
    }

    protected void cleanUp() {
        expandAnimation.cancel();
        pulseAnimation.cancel();
        dismissAnimation.start();

        if (getParent() != null) {
            if (overlay != null && overlay.exitAnimation != null) {
                performOverlayExitAnimation();
            } else {
                ((ViewGroup) this.getParent()).removeView(this);
            }
        }
    }

    private void performOverlayExitAnimation() {
        if (!cleanUpLock) {
            final FrameLayout _pointerToFrameLayout = this;
            cleanUpLock = true;
            Log.d("tourguide", "Overlay exit animation listener is overwritten...");
            overlay.exitAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ViewGroup) _pointerToFrameLayout.getParent()).removeView(_pointerToFrameLayout);
                }
            });
            this.startAnimation(overlay.exitAnimation);
        }
    }

    /* comment this whole method to cause a memory leak */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /* cleanup reference to prevent memory leak */
        eraserCanvas.setBitmap(null);
        eraserBitmap = null;

        if (animatorSetArrayList != null && !animatorSetArrayList.isEmpty()) {
            for (int i = 0; i < animatorSetArrayList.size(); i++) {
                animatorSetArrayList.get(i).end();
                animatorSetArrayList.get(i).removeAllListeners();
            }
        }
    }

    /**
     * Show an event in the LogCat view, for debugging
     */
    private void dumpEvent(MotionEvent event) {
        String[] names = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d("tourguide", sb.toString());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (viewHole != null) {
            if (isWithinButton(ev) && overlay != null && overlay.disableClickThroughHole) {
                Log.d("tourguide", "block user clicking through hole");
                // block it
                return true;
            } else if (isWithinButton(ev)) {
                // let it pass through
                return false;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isWithinButton(MotionEvent ev) {
        int[] pos = new int[2];
        viewHole.getLocationOnScreen(pos);
        return ev.getRawY() >= pos[1] &&
                ev.getRawY() <= (pos[1] + viewHole.getHeight()) &&
                ev.getRawX() >= pos[0] &&
                ev.getRawX() <= (pos[0] + viewHole.getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        eraserBitmap.eraseColor(Color.TRANSPARENT);

        if (overlay != null) {
            eraserCanvas.drawColor(overlay.backgroundColor);

            if (overlay.style == Overlay.RECTANGLE) {
                RectF rect = new RectF(pos[0] - overlay.holePadding + overlay.holeOffsetLeft,
                        pos[1] - overlay.holePadding + overlay.holeOffsetTop,
                        pos[0] + viewHole.getWidth() + overlay.holePadding + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() + overlay.holePadding + overlay.holeOffsetTop);

                if (pulseCircleRadiusAlpha > 0) {
                    pulseCirclePaint.setAlpha(pulseCircleRadiusAlpha);
                }

                eraserCanvas.drawCircle(rect.centerX(), rect.centerY(), pulseCircleRadius, pulseCirclePaint);
                eraserCanvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius, circlePaint);
                eraserCanvas.drawRoundRect(rect, overlay.holeCornerRadius, overlay.holeCornerRadius, eraser);
            } else {
                int holeRadius = overlay.holeRadius != Overlay.NOT_SET ? overlay.holeRadius : radius;
                int cx = pos[0] + viewHole.getWidth() / 2 + overlay.holeOffsetLeft;
                int cy = pos[1] + viewHole.getHeight() / 2 + overlay.holeOffsetTop;

                if (pulseCircleRadiusAlpha > 0) {
                    pulseCirclePaint.setAlpha(pulseCircleRadiusAlpha);
                }

                eraserCanvas.drawCircle(cx, cy, pulseCircleRadius, pulseCirclePaint);
                eraserCanvas.drawCircle(cx, cy, circleRadius, circlePaint);

                eraserCanvas.drawCircle(
                        cx,
                        cy,
                        holeRadius, eraser);
            }
        }

        canvas.drawBitmap(eraserBitmap, 0, 0, null);
    }

    final FloatValueAnimatorBuilder.UpdateListener expandContractUpdateListener = value -> {
        circleRadius = CIRCLE_RADIUS * value;
        pulseCircleRadius *= value;
        invalidate();
    };

    final ValueAnimator pulseAnimation = new FloatValueAnimatorBuilder()
            .duration(1250)
            .repeat(ValueAnimator.INFINITE)
            .interpolator(new AccelerateDecelerateInterpolator())
            .delayBy(300)
            .onUpdate(value -> {
                final float pulseValue = delayedValue(value, 0.5f);
                pulseCircleRadius = (1.0f + pulseValue) * CIRCLE_RADIUS;
                pulseCircleRadiusAlpha = (int) ((1.0f - pulseValue) * 255);
                circleRadius = CIRCLE_RADIUS + halfwayValue(pulseValue) * initPulseCircleRadius;
                invalidate();
            })
            .build();

    final ValueAnimator expandAnimation = new FloatValueAnimatorBuilder()
            .duration(250)
            .delayBy(250)
            .interpolator(new AccelerateDecelerateInterpolator())
            .onUpdate(value -> expandContractUpdateListener.onUpdate(value))
            .onEnd(() -> pulseAnimation.start())
            .build();

    final ValueAnimator dismissAnimation = new FloatValueAnimatorBuilder(true)
            .duration(250)
            .interpolator(new AccelerateDecelerateInterpolator())
            .onUpdate(lerpTime -> expandContractUpdateListener.onUpdate(lerpTime))
            .build();

    float delayedValue(float linearTime, float threshold) {
        if (linearTime < threshold) {
            return 0.0f;
        }

        return (linearTime - threshold) / (1.0f - threshold);
    }

    float halfwayValue(float value) {
        if (value < 0.5f) {
            return value / 0.5f;
        }

        return (1.0f - value) / 0.5f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (overlay != null && overlay.enterAnimation != null) {
            this.startAnimation(overlay.enterAnimation);
        }
    }
}
