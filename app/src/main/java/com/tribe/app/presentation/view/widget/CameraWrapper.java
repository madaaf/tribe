package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.StringDef;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.Filter;
import com.tribe.app.presentation.view.camera.shader.GlPixellateShader;
import com.tribe.app.presentation.view.camera.shader.fx.GlLutShader;
import com.tribe.app.presentation.view.camera.view.CameraView;
import com.tribe.app.presentation.view.camera.view.GlPreview;
import com.tribe.app.presentation.view.camera.view.PictoVisualizerView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
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
public class CameraWrapper extends CardView {

    public static final int RECORDING = 0;
    public static final int SETTING = 1;

    @StringDef({VIDEO, AUDIO, PHOTO})
    public @interface TribeMode {}

    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String PHOTO = "photo";

    public static final String TAG = "CameraWrapper";
    private static final float OVERSHOOT = 0.55f;
    private static final int DURATION = 300;
    private static final int DURATION_ICONS = 360;
    private static final int DELAY = 500;
    private static final int DIFF_TOUCH = 20;
    private static final int RATIO = 3;

    @Inject
    @Filter
    Preference<Integer> filter;

    @Inject ScreenUtils screenUtils;

    @Inject @AudioDefault
    Preference<Boolean> audioDefault;

    @BindView(R.id.cameraView)
    CameraView cameraView;

    @BindView(R.id.imgSound)
    View imgSound;

    @BindView(R.id.imgFlash)
    View imgFlash;

    @BindView(R.id.imgVideo)
    View imgVideo;

    @BindView(R.id.viewCameraForeground)
    View viewCameraForeground;

    @BindView(R.id.layoutCameraPermissions)
    ViewGroup layoutCameraPermissions;

    @BindView(R.id.visualizerView)
    PictoVisualizerView visualizerView;

    // VARIABLES
    private @TribeMode String tribeMode;
    private GlPreview preview;
    private double aspectRatio;
    private boolean isAudioMode;
    private PathView pathView;
    private boolean canMove = true;
    private AudioManager audioManager;
    private int cameraType;
    private int cameraHeight = 0;
    private int cameraWidth = 0;

    // RESOURCES
    private int marginTopInit, marginLeftInit, marginBottomInit, marginVerticalIcons, marginHorizontalIcons, diffTouch,
            marginTopBounds, marginLeftBounds, marginBottomBounds;

    // DRAG CAMERA
    private int downX, downY, xDelta, yDelta;

    // OBSERVABLES
    private PublishSubject<String> tribeModePublishSubject = PublishSubject.create();
    private PublishSubject<Void> permissionsPublishSubject = PublishSubject.create();

    public CameraWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraWrapper);
        cameraType = a.getInteger(R.styleable.CameraWrapper_cameraType, RECORDING);
        cameraHeight = a.getDimensionPixelSize(R.styleable.CameraWrapper_cameraHeight, ViewGroup.LayoutParams.MATCH_PARENT);
        cameraWidth = a.getDimensionPixelSize(R.styleable.CameraWrapper_cameraWidth, ViewGroup.LayoutParams.MATCH_PARENT);

        a.recycle();

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        initUI();
    }

    public void initDimens(int marginTopInit, int marginLeftInit, int marginBottomInit,
                           int marginTopBounds, int marginLeftBounds, int marginBottomBounds, boolean canMove) {
        this.marginTopInit = marginTopInit;
        this.marginLeftInit = marginLeftInit;
        this.marginBottomInit = marginBottomInit;
        this.marginTopBounds = marginTopBounds;
        this.marginLeftBounds = marginLeftBounds;
        this.marginBottomBounds = marginBottomBounds;
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

        if (cameraType == SETTING) {
            imgVideo.setVisibility(View.GONE);
            imgSound.setVisibility(View.GONE);
            imgFlash.setVisibility(View.GONE);
            viewCameraForeground.setVisibility(View.GONE);
        } else {
            imgVideo.setTranslationX(screenUtils.getWidthPx() / RATIO);
        }

        // Corners & Shadows

        if (cameraType == RECORDING) {
            setCardElevation(screenUtils.dpToPx(10));
            setRadius(screenUtils.dpToPx(5));
        } else {
            setCardElevation(0);
            setRadius(0);
        }
    }

    public void onPause() {
        pauseCamera();
    }

    public void onResume(boolean animate) {
        if (audioManager == null)
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        if (animate && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int cx = getWidth();
            int cy = getHeight();

            // get the final radius for the clipping circle
            int dx = Math.max(cx, getWidth() - cx);
            int dy = Math.max(cy, getHeight() - cy);
            float finalRadius = (float) Math.hypot(dx, dy);

            Animator animator = ViewAnimationUtils.createCircularReveal(layoutCameraPermissions, cx, cy, finalRadius, 0);
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
        if (audioManager == null)
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null && audioManager.isMusicActive()) {
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            else
                result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                System.out.println("AUDIO CUT");
            }
        }

        if (isAudioMode) {
            hideIcons();
            preview.startRecording(fileId, visualizerView);
            visualizerView.startRecording();
        } else {
            if (fileId != null) preview.startRecording(fileId, null);
        }

        addPathView();
    }

    public void onEndRecord() {
        if (audioManager != null) audioManager.abandonAudioFocus(null);

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
        if (cameraType == SETTING) return cameraHeight;
        else return (int) (screenUtils.getWidthPx() / RATIO * aspectRatio);
    }

    public int getWidthFromRatio() {
        if (cameraType == SETTING) return cameraWidth;
        else return (int) (screenUtils.getWidthPx() / RATIO);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Scale the preview while keeping the aspect ratio
        int fullWidth = 0;
        int fullHeight = 0;

        if (cameraType == SETTING) {
            fullHeight = cameraHeight;
            fullWidth = cameraWidth;
        } else {
            fullHeight = ((int) (screenUtils.getWidthPx() / RATIO * aspectRatio));
            fullWidth = (screenUtils.getWidthPx() / RATIO);
        }

        setMeasuredDimension(fullWidth, fullHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec(fullWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(fullHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cameraType != SETTING) {
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
        } else {
            return false;
        }
    }

    private void snapCamera() {
        int [] locationLayoutCamera = new int[2];
        getLocationOnScreen(locationLayoutCamera);

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();

        if (lp.leftMargin > screenUtils.getWidthPx() / 4) {
            marginLeftInit = screenUtils.getWidthPx() - getWidth() - marginLeftBounds;
        } else {
            marginLeftInit = marginLeftBounds;
        }

        if (lp.topMargin > screenUtils.getHeightPx() / 4) {
            marginTopInit = ((View) getParent()).getHeight() - getMeasuredHeight() - marginBottomBounds;
        } else {
            marginTopInit = marginTopBounds;
        }

        ValueAnimator animator = ValueAnimator.ofInt(lp.leftMargin, marginLeftInit);
        animator.setDuration(DURATION);
        animator.addUpdateListener(animation -> {
            lp.leftMargin = (Integer) animation.getAnimatedValue();
            setLayoutParams(lp);
        });
        animator.start();

        ValueAnimator animator2 = ValueAnimator.ofInt(lp.topMargin, marginTopInit);
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
        tribeMode = AUDIO;
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
        tribeMode = VIDEO;
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
                .translationX(screenUtils.getWidthPx() / RATIO)
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

    public @CameraWrapper.TribeMode String getTribeMode() {
        return tribeMode;
    }

    public Observable<Void> cameraPermissions() {
        return permissionsPublishSubject;
    }

    private void pauseCamera() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(null);
            audioManager = null;
        }

        releaseCamera();
    }

    private void releaseCamera() {
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
            updateFilter();
            cameraView.setPreview(preview);

            if (audioDefault.get()) {
                tribeMode = AUDIO;
                tribeModePublishSubject.onNext(AUDIO);
            } else {
                tribeMode = VIDEO;
                tribeModePublishSubject.onNext(VIDEO);
            }

            Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .onBackpressureDrop()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        if (audioDefault.get()) {
                            activateSound();
                        } else {
                            cameraView.startPreview();
                        }
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

        int width  = viewCameraForeground.getWidth();
        int height = viewCameraForeground.getHeight();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        pathView = new PathView(getContext());
        pathView.setLayoutParams(params);

        addView(pathView);
        pathView.start(width, height);
    }

    private void removePathView() {
        removeView(pathView);
    }

    public void hideCamera() {
        if (marginTopInit < (screenUtils.getHeightPx() >> 1))
            AnimationUtils.animateTopMargin(this, -screenUtils.getHeightPx(), DURATION >> 1, new OvershootInterpolator(OVERSHOOT));
        else
            AnimationUtils.animateTopMargin(this, screenUtils.getHeightPx(), DURATION >> 1, new OvershootInterpolator(OVERSHOOT));
    }

    public void showCamera() {
        AnimationUtils.animateTopMargin(this, marginTopInit, DURATION >> 1, new OvershootInterpolator(OVERSHOOT));
    }

    public void updateFilter() {
        if (preview != null) {
            if (filter.get() == 3) {
                preview.setShader(new GlPixellateShader());
            } else {
                int resourceFilter = -1;
                if (filter.get().equals(0)) resourceFilter = R.drawable.video_filter_punch;
                else if (filter.get().equals(1)) resourceFilter = R.drawable.video_filter_blue;
                else if (filter.get().equals(2)) resourceFilter = R.drawable.video_filter_bw;
                else resourceFilter = R.drawable.video_filter_punch;
                preview.setShader(new GlLutShader(getContext().getResources(), resourceFilter));
            }
        }
    }
}
