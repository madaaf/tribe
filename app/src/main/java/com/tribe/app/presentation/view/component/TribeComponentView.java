package com.tribe.app.presentation.view.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.Weather;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.scope.DistanceUnits;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.video.TribeMediaPlayer;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.ButtonCardView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.video.ScalableVideoView;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class TribeComponentView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private static final int DURATION = 200;

    @Inject User currentUser;
    @Inject ScreenUtils screenUtils;
    @Inject @SpeedPlayback Preference<Float> speedPlayack;
    @Inject @DistanceUnits Preference<String> distanceUnits;
    @Inject @WeatherUnits Preference<String> weatherUnits;

    @BindView(R.id.videoTextureView)
    ScalableVideoView videoTextureView;

    @BindView(R.id.avatar)
    AvatarView avatarView;

    @BindView(R.id.imgMore)
    ImageView imgMore;

    @BindView(R.id.visualizerView)
    VisualizerView visualizerView;

    @BindView(R.id.labelLevel)
    ButtonCardView labelLevel;

    @BindView(R.id.labelCity)
    ButtonCardView labelCity;

    @BindView(R.id.labelLocation)
    ButtonCardView labelLocation;

    @BindView(R.id.labelWeather)
    ButtonCardView labelWeather;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    @BindView(R.id.txtTime)
    TextViewFont txtTime;

    @BindView(R.id.txtSwipeDown)
    TextViewFont txtSwipeDown;

    @BindView(R.id.txtTranscript)
    TextViewFont txtTranscript;

    @BindView(R.id.viewBGProgress)
    View viewBGProgress;

    @BindView(R.id.layoutDownloadProgress)
    ViewGroup layoutDownloadProgress;

    @BindView(R.id.progressBarDownload)
    ProgressBar progressBarDownload;

    @BindView(R.id.progressBarDownloadIndeterminate)
    CircularProgressView progressBarDownloadIndeterminate;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private final PublishSubject<View> clickEnableLocation = PublishSubject.create();
    private final PublishSubject<TribeMessage> onPlayerError = PublishSubject.create();
    private final PublishSubject<TribeMessage> clickMore = PublishSubject.create();
    private final PublishSubject<TribeComponentView> onFirstLoop = PublishSubject.create();

    // PLAYER
    private boolean isFirstLoop = true;
    private boolean isPrepared = false;
    private TribeMediaPlayer mediaPlayer;
    private TribeMessage tribe;
    private SurfaceTexture surfaceTexture;
    private long lastPosition = -1L;
    private ValueAnimator animatorProgress;
    private Visualizer visualizer;

    public TribeComponentView(Context context) {
        super(context);
        init(context, null);
    }

    public TribeComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        cancelProgress();

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_tribe, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        videoTextureView.setSurfaceTextureListener(this);
        super.onFinishInflate();
    }

    public void setTribe(TribeMessage tribe) {
        this.tribe = tribe;
        txtName.setText(tribe.getFrom().getDisplayName());
        avatarView.load(tribe.getFrom().getProfilePicture());
        txtTime.setText(DateUtils.getRelativeTimeSpanString(tribe.getRecordedAt().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));

        if (tribe.getFrom() != null && !tribe.getFrom().getId().equals(Constants.SUPPORT_ID)) {
            ScoreUtils.Level level = ScoreUtils.getLevelForScore(tribe.getFrom().getScore());
            labelLevel.setText(level.getStringId());
            labelLevel.setDrawableResource(level.getDrawableId());
        } else {
            labelLevel.setVisibility(View.GONE);
        }

        updateLocation();

        if (!StringUtils.isEmpty(tribe.getTranscript())) {
            txtTranscript.setText(tribe.getTranscript());
        }
    }

    public void updateLocation() {
        if (tribe.getLocation() != null && tribe.getLocation().hasLocation()) {
            Location location = tribe.getLocation();
            Weather weatherObj = tribe.getWeather();

            if (!StringUtils.isEmpty(location.getCity()) && !StringUtils.isEmpty(location.getCountryCode())) {
                labelCity.setText(location.getCity());
                labelCity.setDrawableResource(getResources().getIdentifier("picto_flag_" + location.getCountryCode().toLowerCase(), "drawable", getContext().getPackageName()));
                labelCity.setVisibility(View.VISIBLE);
            } else {
                labelCity.setVisibility(View.GONE);
            }

            labelLocation.setText(currentUser.getLocation() != null ? currentUser.getLocation().distanceTo(getContext(), distanceUnits.get(), tribe.getLocation()) : getContext().getString(R.string.tribe_metadata_activate_location));

            if (tribe.getWeather() != null && !StringUtils.isEmpty(tribe.getWeather().getIcon())) {
                labelWeather.setVisibility(View.VISIBLE);
                labelWeather.setText((weatherUnits.get().equals(com.tribe.app.presentation.view.utils.Weather.CELSIUS) ? weatherObj.getTempC() : weatherObj.getTempF()) + "°");
                labelWeather.setDrawableResource(getResources().getIdentifier("weather_" + weatherObj.getIcon(), "drawable", getContext().getPackageName()));
            } else {
                labelWeather.setVisibility(View.GONE);
            }
        } else {
            labelCity.setVisibility(View.GONE);
            labelLocation.setVisibility(View.GONE);
            labelWeather.setVisibility(View.GONE);
        }
    }

    public void preparePlayer(boolean autoStart) {
        if (layoutDownloadProgress.getVisibility() == View.VISIBLE) {
            AnimationUtils.fadeOut(layoutDownloadProgress, DURATION);
        }

        if (tribe.isDownloaded() && !isPrepared) {
            mediaPlayer = new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(), FileUtils.getPathForId(getContext(), tribe.getId(), FileUtils.VIDEO))
                    .autoStart(autoStart)
                    .looping(true)
                    .mute(false)
                    .canChangeSpeed(true)
                    .forceLegacy(true)
                    .audioStreamType(AudioManager.STREAM_VOICE_CALL)
                    .build();

            if (tribe.getType().equals(CameraWrapper.AUDIO) && visualizer == null) {
                visualizerView.setVisibility(VISIBLE);
                visualizerView.setAvatarPicture(tribe.getFrom().getProfilePicture());
                setupVisualizerFxAndUI();
            }

            if (surfaceTexture != null)
                mediaPlayer.setSurface(surfaceTexture);

            subscriptions.add(mediaPlayer.onPreparedPlayer().subscribe(prepared -> {
                isPrepared = true;
            }));

            subscriptions.add(mediaPlayer.onVideoSizeChanged().subscribe(videoSize -> {
                videoTextureView.scaleVideoSize(videoSize.getWidth(), videoSize.getHeight());
            }));

            subscriptions.add(mediaPlayer.onErrorPlayer().subscribe(error -> {
                onPlayerError.onNext(tribe);
            }));

            subscriptions.add(mediaPlayer.onVideoStarted().subscribe(started -> {
                animateProgress();
            }));

            subscriptions.add(mediaPlayer.onCompletion().subscribe(started -> {
                lastPosition = 0;
                animateProgress();

                if (isFirstLoop) {
                    isFirstLoop = false;
                    onFirstLoop.onNext(this);
                }
            }));

            if (lastPosition != -1) mediaPlayer.seekTo(lastPosition);
        }
    }

    private void setupVisualizerFxAndUI() {
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        if (visualizerView != null) visualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {

                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);
    }

    public void showProgress() {
        if (layoutDownloadProgress.getVisibility() == View.GONE) {
            layoutDownloadProgress.setVisibility(View.VISIBLE);
        }

        if (tribe.getTotalSize() <= 0) {
            progressBarDownload.setVisibility(View.GONE);
            progressBarDownloadIndeterminate.setVisibility(View.VISIBLE);
        } else {
            progressBarDownload.setVisibility(View.VISIBLE);
            progressBarDownloadIndeterminate.setVisibility(View.GONE);

            if (progressBarDownload.getMax() != tribe.getTotalSize()) {
                progressBarDownload.setMax((int) tribe.getTotalSize());
            }
        }

        progressBarDownload.setProgress((int) tribe.getProgress());
    }

    public void releasePlayer() {
        if (visualizer != null) {
            visualizer.release();
            visualizer = null;
        }

        if (visualizerView != null) visualizerView.release();

        if (mediaPlayer != null)
            mediaPlayer.release();

        mediaPlayer = null;
        isPrepared = false;
        lastPosition = -1;

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.clear();
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            animateProgress();
            mediaPlayer.play();

        } else {
            preparePlayer(true);
        }
    }

    public void pausePlayer() {
        if (mediaPlayer != null) {
            lastPosition = mediaPlayer.getPosition();
            cancelProgress();
        }

        releasePlayer();
    }

    private void animateProgress() {
        cancelProgress();

        long duration = mediaPlayer.getDuration();

        if (duration > -1) {
            animatorProgress = ValueAnimator.ofInt(lastPosition != -1 ? (int) lastPosition : 0, screenUtils.getWidthPx());
            animatorProgress.addUpdateListener(valueAnimator -> {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = viewBGProgress.getLayoutParams();
                layoutParams.width = val;
                viewBGProgress.setLayoutParams(layoutParams);
            });

            int animatorDuration = (int) (duration / speedPlayack.get());
            animatorProgress.setDuration(animatorDuration);
            animatorProgress.start();
        }
    }

    private void cancelProgress() {
        if (animatorProgress != null)
            animatorProgress.cancel();
    }

    private void removeProgressBar() {
        viewBGProgress.setVisibility(INVISIBLE);
        layoutDownloadProgress.setVisibility(INVISIBLE);
    }

    public void setIconsAlpha(float alpha) {
        avatarView.setAlpha(alpha);
        imgMore.setAlpha(alpha);
        labelCity.setAlpha(alpha);
        labelLocation.setAlpha(alpha);
        labelLevel.setAlpha(alpha);
        txtName.setAlpha(alpha);
        txtTime.setAlpha(alpha);
        labelWeather.setAlpha(alpha);
    }

    public void setSwipeDownAlpha(float alpha) {
        txtSwipeDown.setAlpha(alpha);
    }

    public void setColor(int color) {
        viewBGProgress.setBackgroundColor(color);
    }

    public void changeSpeed() {
        mediaPlayer.setPlaybackRate();
        lastPosition = mediaPlayer.getPosition();
        animateProgress();
    }

    public Observable<View> onClickEnableLocation() {
        return clickEnableLocation;
    }

    public Observable<TribeMessage> onErrorTribe() {
        return onPlayerError;
    }

    public Observable<TribeMessage> onClickMore() {
        return clickMore;
    }

    public Observable<TribeComponentView> onFirstLoop() {
        return onFirstLoop;
    }

    @OnClick(R.id.labelLocation)
    void enableDistance(View view) {
        if (currentUser.getLocation() == null || !currentUser.getLocation().hasLocation()) {
            clickEnableLocation.onNext(this);
        } else if (currentUser.getLocation() != null && currentUser.getLocation().hasLocation()) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr="
                            + currentUser.getLocation().getLatitude() + "," + currentUser.getLocation().getLongitude()
                            + "&daddr=" + tribe.getLocation().getLatitude() + "," + tribe.getLocation().getLongitude()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().getApplicationContext().startActivity(intent);
        }
    }

    @OnClick(R.id.imgMore)
    void clickMore(View v) {
        clickMore.onNext(tribe);
    }

    public static String fmt(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;
        if (mediaPlayer != null && tribe.isDownloaded()) mediaPlayer.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surfaceTexture = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
