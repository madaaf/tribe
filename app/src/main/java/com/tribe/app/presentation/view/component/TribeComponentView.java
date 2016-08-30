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
import com.tribe.app.presentation.view.video.TribeMediaPlayer;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.LabelButton;
import com.tribe.app.presentation.view.widget.ScalableTextureView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.VideoTextureView;

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

    @Inject User currentUser;
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
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private final PublishSubject<View> clickEnableLocation = PublishSubject.create();

    // PLAYER
    private TribeMediaPlayer mediaPlayer;
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

    public void preparePlayer(boolean autoStart) {
        mediaPlayer = new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(), FileUtils.getPathForId(tribe.getId()))
                .autoStart(autoStart)
                .looping(true)
                .mute(false)
                .build();

        subscriptions.add(mediaPlayer.onPreparedPlayer().subscribe(prepared -> {

        }));

        subscriptions.add(mediaPlayer.onVideoSizeChanged().subscribe(videoSize -> {
            if (videoTextureView != null && videoTextureView.getContentHeight() != videoSize.getHeight()) {
                videoTextureView.setContentWidth(videoSize.getWidth());
                videoTextureView.setContentHeight(videoSize.getHeight());
                videoTextureView.updateTextureViewSize();
            }

            subscriptions.add(mediaPlayer.onErrorPlayer().subscribe(error -> {
                System.out.println("MEDIA PLAYER ERROR");
            }));
        }));
    }

    public void releasePlayer() {
        mediaPlayer.releasePlayer();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.clear();
        }
    }

    public void play() {
        mediaPlayer.resumePlayer();
    }

    public void pausePlayer() {
        mediaPlayer.pausePlayer();
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
        //if (mediaPlayer.isPlaying()) {
        //    mediaPlayer.setRate(speedPlayack.get());
        //}
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
}
