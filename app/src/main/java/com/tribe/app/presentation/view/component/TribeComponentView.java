package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
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
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.LabelButton;
import com.tribe.app.presentation.view.widget.ScalableTextureView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.VideoTextureView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/06/2016.
 */
public class TribeComponentView extends FrameLayout implements TextureView.SurfaceTextureListener, IVLCVout.Callback {

    @Inject User currentUser;
    @Inject LibVLC libVLC;
    @Inject @SpeedPlayback Preference<Float> speedPlayack;
    @Inject @DistanceUnits Preference<String> distanceUnits;
    @Inject @WeatherUnits Preference<String> weatherUnits;

    @BindView(R.id.videoTextureView)
    VideoTextureView videoTextureView;

    @BindView(R.id.avatar)
    AvatarView avatarView;

    @BindView(R.id.imgMore)
    ImageView imgMore;

    @BindView(R.id.labelDistance)
    LabelButton labelDistance;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    @BindView(R.id.txtTime)
    TextViewFont txtTime;

    @BindView(R.id.labelCity)
    LabelButton labelCity;

    @BindView(R.id.txtSwipeDown)
    TextViewFont txtSwipeDown;

    @BindView(R.id.labelWeather)
    LabelButton labelWeather;

    // OBSERVABLES
    private Unbinder unbinder;
    private final PublishSubject<View> clickEnableLocation = PublishSubject.create();

    // PLAYER
    private boolean isPaused = false;
    private boolean isReadyToPlay = false;
    private boolean shouldAutoPlay = false;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer.EventListener playerListener;
    private int videoWidth, videoHeight;
    private SurfaceTexture surfaceTexture;
    private TribeMessage tribe;

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
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_tribe, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        try {
            mediaPlayer = new MediaPlayer(libVLC);
            playerListener = new PlayerListener();
            mediaPlayer.setEventListener(playerListener);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
        }

        videoTextureView.setScaleType(ScalableTextureView.ScaleType.CENTER_CROP_FILL);
        videoTextureView.setSurfaceTextureListener(this);
        super.onFinishInflate();
    }

    public void setTribe(TribeMessage tribe) {
        this.tribe = tribe;
        txtName.setText(tribe.getFrom().getDisplayName());
        avatarView.load(tribe.getFrom().getProfilePicture());
        txtTime.setText(DateUtils.getRelativeTimeSpanString(tribe.getRecordedAt().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));

        if (tribe.getLocation() != null && tribe.getLocation().hasLocation()) {
            labelCity.setVisibility(View.VISIBLE);
            labelDistance.setVisibility(View.VISIBLE);

            Location location = tribe.getLocation();
            Weather weatherObj = tribe.getWeather();
            labelCity.setText(location.getCity());
            labelDistance.setText(currentUser.getLocation() != null ? currentUser.getLocation().distanceTo(getContext(), distanceUnits.get(), tribe.getLocation()) : getContext().getString(R.string.tribe_distance_enable));
            labelDistance.setType(currentUser.getLocation() != null ? LabelButton.INFOS : LabelButton.ACTION);

            if (tribe.getWeather() != null) {
                labelWeather.setVisibility(View.VISIBLE);
                labelWeather.setText((weatherUnits.get().equals(com.tribe.app.presentation.view.utils.Weather.CELSIUS) ? weatherObj.getTempC() : weatherObj.getTempF()) + "°");
                labelWeather.setDrawableResource(getResources().getIdentifier("weather_" + weatherObj.getIcon(), "drawable", getContext().getPackageName()));
            } else {
                labelWeather.setVisibility(View.GONE);
            }
        } else {
            labelCity.setVisibility(View.GONE);
            labelDistance.setVisibility(View.GONE);
            labelWeather.setVisibility(View.GONE);
        }
    }

    public void startPlayer() {
        setMedia();
        //setTxtSpeed();
        if (surfaceTexture != null) prepareWithSurface();
    }

    public void setMedia() {
        Media media = new Media(libVLC, FileUtils.getPathForId(tribe.getId()));
        mediaPlayer.setMedia(media);
    }

    public void releasePlayer() {
        if (mediaPlayer == null) return;

        isReadyToPlay = false;
        new Thread(() -> {
            try {
                mediaPlayer.stop();
                final IVLCVout vout = mediaPlayer.getVLCVout();
                vout.removeCallback(TribeComponentView.this);
                vout.detachViews();
                mediaPlayer.release();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void play() {
        shouldAutoPlay = true;

        if (isReadyToPlay) {
            isPaused = false;
            mediaPlayer.play();
        }
    }

    public void pausePlayer() {
        if (mediaPlayer != null && !isPaused) {
            isPaused = true;
            mediaPlayer.pause();
        }
    }

    public void resumePlayer() {
        if (mediaPlayer != null && isPaused) {
            isPaused = false;
            mediaPlayer.play();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setIconsAlpha(float alpha) {
        avatarView.setAlpha(alpha);
        imgMore.setAlpha(alpha);
        labelCity.setAlpha(alpha);
        labelDistance.setAlpha(alpha);
        txtName.setAlpha(alpha);
        txtTime.setAlpha(alpha);
        labelWeather.setAlpha(alpha);
    }

    public void setSwipeDownAlpha(float alpha) {
        txtSwipeDown.setAlpha(alpha);
    }

    public Observable<View> onClickEnableLocation() {
        return clickEnableLocation;
    }

    public void changeSpeed() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.setRate(speedPlayack.get());
        }
    }

    @OnClick(R.id.labelDistance)
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

//    private void setTxtSpeed() {
//        txtSpeed.setText(getContext().getResources().getString(R.string.Tribe_Speed, fmt(speedPlayback.get())));
//    }

    public static String fmt(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surfaceTexture = surface;
        if (mediaPlayer != null) prepareWithSurface();
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

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        videoWidth = width;
        videoHeight = height;

        if (videoTextureView != null && videoTextureView.getContentHeight() != height) {
            videoTextureView.setContentWidth(width);
            videoTextureView.setContentHeight(height);
            videoTextureView.updateTextureViewSize();
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        this.releasePlayer();
        Toast.makeText(getContext(), "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }

    private void prepareWithSurface() {
        if (mediaPlayer != null && isPaused()) {
            mediaPlayer.play();
        } else if (mediaPlayer.getMedia() != null) {
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoSurface(surfaceTexture);
            vout.addCallback(this);
            vout.attachViews();
            isReadyToPlay = true;

            if (shouldAutoPlay) {
                play();
            } else {
                //play();
                //pausePlayer();
            }
        }
    }

    private class PlayerListener implements MediaPlayer.EventListener {

        public PlayerListener() {

        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    setMedia();
                    mediaPlayer.play();
                    break;
                case MediaPlayer.Event.Vout:
                    if (mediaPlayer != null && mediaPlayer.getRate() != speedPlayack.get()) {
                        mediaPlayer.setRate(speedPlayack.get());
                    }
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }
}
