package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.video.TribeMediaPlayer;
import com.tribe.app.presentation.view.video.VideoSize;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 8/15/16.
 */
public class TribeVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {

    @BindView(R.id.textureViewLayout)
    CardView textureViewLayout;

    // VARIABLES
    private int scaleType;
    private boolean autoStart;
    private boolean looping;
    private boolean mute;
    private boolean speedControl;
    private VideoTextureView videoTextureView;
    private TribeMediaPlayer mediaPlayer;
    private VideoSize videoSize;

    // OBSERVABLES
    private Unbinder unbinder;
    private final PublishSubject<Boolean> videoStarted = PublishSubject.create();
    private final PublishSubject<VideoSize> videoSizeSubject = PublishSubject.create();
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public TribeVideoView(Context context) {
        this(context, null);
        init(context, null);
    }

    public TribeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_video, this, true);
        unbinder = ButterKnife.bind(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TribeVideoView);
        scaleType = a.getInt(R.styleable.TribeVideoView_scaleVideoType, ScalableTextureView.CENTER_CROP);
        autoStart = a.getBoolean(R.styleable.TribeVideoView_autoStart, true);
        looping = a.getBoolean(R.styleable.TribeVideoView_looping, true);
        mute = a.getBoolean(R.styleable.TribeVideoView_mute, true);
        speedControl = a.getBoolean(R.styleable.TribeVideoView_speedControl, false);
        a.recycle();

        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void createPlayer(String media) {
        videoTextureView = new VideoTextureView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textureViewLayout.addView(videoTextureView, params);
        videoTextureView.setScaleType(scaleType);
        videoTextureView.setSurfaceTextureListener(this);

        mediaPlayer = new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(), media)
                .autoStart(autoStart)
                .looping(looping)
                .mute(mute)
                .canChangeSpeed(speedControl)
                .build();

        subscriptions.add(mediaPlayer.onVideoStarted().subscribe(videoStarted));
        subscriptions.add(mediaPlayer.onPreparedPlayer().subscribe(prepared -> {

        }));

        subscriptions.add(mediaPlayer.onVideoSizeChanged().doOnNext(videoSize -> {
            if (videoTextureView != null && videoTextureView.getContentHeight() != videoSize.getHeight()) {
                videoTextureView.setContentWidth(videoSize.getWidth());
                videoTextureView.setContentHeight(videoSize.getHeight());
                videoTextureView.updateTextureViewSize();
            }

            this.videoSize = videoSize;
        }).subscribe(videoSizeSubject));

        subscriptions.add(mediaPlayer.onErrorPlayer().subscribe(error -> {
            System.out.println("MEDIA PLAYER ERROR");
        }));
    }

    public void releasePlayer() {
        mediaPlayer.release();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.clear();
        }
    }

    public void play() {
        mediaPlayer.play();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void setScaleType(@ScalableTextureView.ScaleVideoType int scaleType) {
        this.scaleType = scaleType;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public void setSpeedControl(boolean speedControl) {
        this.speedControl = speedControl;
    }

    ///////////////////
    /// OBSERVABLES ///
    ///////////////////
    public Observable<Boolean> videoStarted() {
        return videoStarted;
    }

    public PublishSubject<VideoSize> videoSize() {
        return videoSizeSubject;
    }
}