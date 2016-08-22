package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.StringDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.camera.shader.fx.GlLutShader;
import com.tribe.app.presentation.view.camera.view.CameraView;
import com.tribe.app.presentation.view.camera.view.GlPreview;
import com.tribe.app.presentation.view.camera.view.PictoVisualizerView;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * A layout which handles the preview aspect ratio.
 */
public class CameraWrapper extends FrameLayout {

    @StringDef({VIDEO, AUDIO, PHOTO})
    public @interface TribeMode {}

    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String PHOTO = "photo";

    public static final String TAG = "CameraWrapper";
    public static final int DURATION = 200;
    public static final int DURATION_ICONS = 360;
    public static final int DELAY = 500;
    public static final int DIFF_TOUCH = 20;
    public static final int RATIO = 3;

    @Inject ScreenUtils screenUtils;

    @BindView(R.id.cameraView)
    CameraView cameraView;

    @BindView(R.id.imgSound)
    View imgSound;

    @BindView(R.id.imgFlash)
    View imgFlash;

    @BindView(R.id.imgVideo)
    View imgVideo;

    @BindView(R.id.layoutCameraPermissions)
    ViewGroup layoutCameraPermissions;

    @BindView(R.id.visualizerView)
    PictoVisualizerView visualizerView;

    // VARIABLES
    private GlPreview preview;
    private double aspectRatio;
    private boolean isAudioMode;
    private PathView pathView;
    private boolean canMove = true;

    // RESOURCES
    private int marginTopInit, marginLeftInit, marginBottomInit, marginVerticalIcons, marginHorizontalIcons, diffTouch;

    // DRAG CAMERA
    private int downX, downY, xDelta, yDelta;

    // OBSERVABLES
    private PublishSubject<String> tribeModePublishSubject = PublishSubject.create();
    private PublishSubject<Void> permissionsPublishSubject = PublishSubject.create();

    public CameraWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        initUI();
    }

    public void initDimens(int marginTopInit, int marginLeftInit, int marginBottomInit, boolean canMove) {
        this.marginTopInit = marginTopInit;
        this.marginLeftInit = marginLeftInit;
        this.marginBottomInit = marginBottomInit;
        marginHorizontalIcons = getContext().getResources().getDimensionPixelOffset(R.dimen.horizontal_margin);
        marginVerticalIcons = getContext().getResources().getDimensionPixelOffset(R.dimen.vertical_margin);
        diffTouch = screenUtils.dpToPx(DIFF_TOUCH);
        this.canMove = canMove;
    }

    public void initUI() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_camera, this, true);

        ButterKnife.bind(this);

        setAspectRatio(3.0 / 2.0);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMargins();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        imgVideo.setTranslationX(screenUtils.getWidth() / RATIO);

        setBackgroundResource(R.color.black_opacity_20);
    }

    public void onPause() {
        pauseCamera();
    }

    public void onResume(boolean animate) {
        if (animate) {
            int cx = getWidth();
            int cy = getHeight();

            // get the final radius for the clipping circle
            int dx = Math.max(cx, getWidth() - cx);
            int dy = Math.max(cy, getHeight() - cy);
            float finalRadius = (float) Math.hypot(dx, dy);

            Animator animator =
                    ViewAnimationUtils.createCircularReveal(layoutCameraPermissions, cx, cy, finalRadius, 0);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(DURATION);
            animator.start();
        } else {
            layoutCameraPermissions.setVisibility(View.GONE);
        }

        if (preview == null)
            resumeCamera();
    }

    public void showPermissions() {
        layoutCameraPermissions.setVisibility(View.VISIBLE);
    }

    public void onStartRecord(String fileId) {
        if (isAudioMode) {
            hideIcons();
            preview.startRecording(fileId, visualizerView);
            visualizerView.startRecording();
        } else {
            preview.startRecording(fileId, null);
        }

        addPathView();
    }

    public void onEndRecord() {
        if (isAudioMode) {
            showIcons();
            visualizerView.stopRecording();
        }

        preview.stopRecording();
        removePathView();
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
        layoutParams.topMargin = marginTopInit;
        setLayoutParams(layoutParams);
    }

    public int getHeightFromRatio() {
        return (int) (screenUtils.getWidth() / RATIO * aspectRatio);
    }

    public int getWidthFromRatio() {
        return (int) (screenUtils.getWidth() / RATIO);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Scale the preview while keeping the aspect ratio
        int fullWidth = screenUtils.getWidth() / RATIO;
        int fullHeight = (int) (screenUtils.getWidth() / RATIO * aspectRatio);

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
                if (Math.abs(touchY - downY) < diffTouch && Math.abs(touchX - downX) < diffTouch) {
                    cameraView.switchCamera();
                }

                if (canMove)
                    snapCamera();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                if (canMove) {
                    layoutParams.leftMargin = touchX - xDelta;
                    layoutParams.topMargin = touchY - yDelta;
                    setLayoutParams(layoutParams);
                    invalidate();
                }

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
            topMargin = marginTopInit;
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

    @OnClick(R.id.imgSound)
    public void activateSound() {
        isAudioMode = true;
        tribeModePublishSubject.onNext(AUDIO);

        if (cameraView != null) {
            cameraView.animate().alpha(0).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
        }

        imgSound.setEnabled(false);
        imgSound.animate()
                .translationX((getWidth() >> 1) - marginHorizontalIcons - (imgSound.getWidth() >> 1))
                .translationY(-((getHeight() >> 1) - marginVerticalIcons - (imgSound.getHeight() >> 1)))
                .setDuration(DURATION_ICONS)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imgSound.setVisibility(View.GONE);
                        visualizerView.activate();
                    }
                })
                .start();

        imgFlash.animate()
                .translationX(getWidth() >> 1)
                .alpha(0)
                .setDuration(DURATION_ICONS)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        imgVideo.animate()
                .translationX(0)
                .setDuration(DURATION_ICONS)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(DELAY)
                .setListener(null)
                .start();
    }

    @OnClick(R.id.imgVideo)
    public void activateVideo() {
        isAudioMode = false;
        tribeModePublishSubject.onNext(VIDEO);
        visualizerView.deactivate();

        imgSound.setVisibility(View.VISIBLE);
        imgSound.setEnabled(true);
        imgSound.animate()
                .translationX(0)
                .translationY(0)
                .setDuration(DURATION_ICONS)
                .setStartDelay(DELAY)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        visualizerView.deactivate();
                    }
                })
                .start();

        imgFlash.animate()
                .translationX(0)
                .translationY(0)
                .alpha(1)
                .setDuration(DURATION_ICONS)
                .setStartDelay(DELAY)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        imgVideo.animate()
                .translationX(screenUtils.getWidth() / RATIO)
                .setDuration(DURATION_ICONS)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        if (cameraView != null) {
                            cameraView.animate().alpha(1).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
                        }
                    }
                })
                .start();
    }

    @OnClick(R.id.layoutCameraPermissions)
    public void askForPermissions() {
        permissionsPublishSubject.onNext(null);
    }

    public Observable<String> tribeMode() {
        return tribeModePublishSubject;
    }

    public Observable<Void> cameraPermissions() {
        return permissionsPublishSubject;
    }

    private void pauseCamera() {
        if (cameraView != null && preview != null) {
            cameraView.stopPreview();
            cameraView.removeAllViews();
            preview.stopRecording();
            preview = null;
        }
    }

    private void resumeCamera() {
        if (cameraView != null) {
            preview = new GlPreview(getContext());
            preview.setShader(new GlLutShader(getContext().getResources(), R.drawable.video_filter_blue));
            preview.setZOrderOnTop(false);
            preview.setZOrderMediaOverlay(false);
            cameraView.setPreview(preview);

            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    tribeModePublishSubject.onNext(VIDEO);
                    cameraView.startPreview();
                });
        }

        if (preview != null)
            preview.onResume();
    }

    private void hideIcons() {
        imgVideo.animate().alpha(0).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
        imgSound.animate().alpha(0).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
        imgFlash.animate().alpha(0).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void showIcons() {
        imgVideo.animate().alpha(1).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
        imgSound.animate().alpha(1).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
        imgFlash.animate().alpha(1).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void addPathView() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getWidth(), getHeight());
        pathView = new PathView(getContext());
        pathView.setLayoutParams(params);
        addView(pathView);
        pathView.start(getWidth(), getHeight());
    }

    private void removePathView() {
        removeView(pathView);
    }
}
