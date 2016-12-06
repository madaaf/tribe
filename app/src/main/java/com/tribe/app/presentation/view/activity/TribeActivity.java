package com.tribe.app.presentation.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;

import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeMVPView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.LowPassFilter;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeMVPView, SensorEventListener {

    public static final String[] PERMISSIONS_LOCATION = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
    public static final String RECIPIENT = "RECIPIENT";
    public static final String POSITION = "POSITION";
    public static final String TRIBES_SEEN = "TRIBES_SEEN";

    public static Intent getCallingIntent(Context context, int position, Recipient recipient) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(RECIPIENT, recipient);
        return intent;
    }

    @Inject
    ScreenUtils screenUtils;

    @Inject
    TribePresenter tribePresenter;

    @Inject
    PaletteGrid paletteGrid;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @Inject
    @SpeedPlayback
    Preference<Float> speedPlayback;

    @Inject
    SoundManager soundManager;

    @Inject
    TutorialManager tutorialManager;

    @BindView(R.id.viewTribePager)
    TribePagerView viewTribePager;

    @BindView(R.id.earModeView)
    View earModeView;

    // VARIABLES
    private Recipient recipient;
    private int position;
    private User currentUser;
    private TribeMessage currentTribe;
    private boolean isRecording = false;
    private AudioManager audioManager;
    private Tutorial tutorial;
    private boolean isReplyMode;

    // SENSORS
    private SensorManager sensorManager;
    private Sensor proximity;
    private Sensor magnetic;
    private Sensor accelerometer;
    private float[] lastMagFields = new float[3];
    private float[] lastAccels = new float[3];
    private float[] rotationMatrix = new float[16];
    private float[] orientation = new float[4];
    private boolean isNear;
    private boolean earMode = false;
    private float pitch = 0.f;
    private float yaw = 0.f;
    private float roll = 0.f;
    private LowPassFilter filterYaw = new LowPassFilter(0.03f);
    private LowPassFilter filterPitch = new LowPassFilter(0.03f);
    private LowPassFilter filterRoll = new LowPassFilter(0.03f);

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initParams();
        initDependencyInjector();
        initTribePagerView();
        initSubscriptions();
        initPresenter();
        initProximitySensor();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewTribePager.onResume();

        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
    }

    @Override
    protected void onPause() {
        cleanUpTutorial();
        viewTribePager.onPause();
        tribePresenter.onViewDetached();

        sensorManager.unregisterListener(this);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initParams() {
        recipient = (Recipient) getIntent().getSerializableExtra(RECIPIENT);
        position = getIntent().getIntExtra(POSITION, 0);
        currentUser = getCurrentUser();

        Bundle bundle = new Bundle();
        bundle.putString(TagManagerConstants.TYPE, recipient instanceof Membership ? TagManagerConstants.TYPE_TRIBE_GROUP : TagManagerConstants.TYPE_TRIBE_USER);
        tagManager.trackEvent(TagManagerConstants.KPI_TRIBES_OPENED, bundle);
    }

    private void initProximitySensor() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initTribePagerView() {
        int color = PaletteGrid.get(position);
        viewTribePager.setBackgroundColor(color);
        viewTribePager.initWithInfo(recipient);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();

        subscriptions.add(
                viewTribePager
                .onDismissHorizontal()
                .doOnNext(aVoid -> {
                    tribePresenter.markTribeListAsRead(recipient, viewTribePager.getTribeListSeens());
                    Intent intentResult = new Intent();
                    intentResult.putExtra(TRIBES_SEEN, (Serializable) viewTribePager.getTribeListSeens());
                    intentResult.putExtra(RECIPIENT, recipient);
                    setResult(Activity.RESULT_OK, intentResult);
                })
                .delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));

        subscriptions.add(
                viewTribePager
                .onDismissVertical()
                .doOnNext(aVoid -> {
                    tribePresenter.markTribeListAsRead(recipient, viewTribePager.getTribeListSeens());
                    Intent intentResult = new Intent();
                    intentResult.putExtra(TRIBES_SEEN, (Serializable) viewTribePager.getTribeListSeens());
                    intentResult.putExtra(RECIPIENT, recipient);
                    setResult(Activity.RESULT_OK, intentResult);
                })
                .delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));

        subscriptions.add(
                Observable.merge(
                    viewTribePager.onClose(),
                    viewTribePager.onNext(),
                    viewTribePager.onReplyMode()
                ).subscribe(o -> {
                    cleanUpTutorial();
                })
        );

        subscriptions.add(
                viewTribePager.onReplyModeOpened()
                        .delay(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            if (tutorialManager.shouldDisplay(TutorialManager.REPLY)) {
                                isReplyMode = true;

                                TileView tileView = viewTribePager.getBtnReply();
                                tutorial = tutorialManager.showReply(
                                        this,
                                        tileView.avatar,
                                        tileView.avatar.getWidth() - screenUtils.dpToPx(30),
                                        screenUtils.dpToPx(20f),
                                        v -> cleanUpTutorial()
                                );
                            }
                        })
        );

        subscriptions.add(
                viewTribePager.onDown()
                        .subscribe(o -> {
                            if (isReplyMode) {
                                isReplyMode = false;
                                cleanUpTutorial();
                            }
                        })
        );

        subscriptions.add(viewTribePager.onRecordStart()
                .doOnNext(view -> soundManager.playSound(SoundManager.START_RECORD, SoundManager.SOUND_LOW))
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(view -> {
                    isRecording = true;
                    currentTribe = tribePresenter.createTribe(currentUser, recipient, viewTribePager.getTribeMode());
                    recipient.setTribe(currentTribe);

                    if (tutorialManager.shouldDisplay(TutorialManager.RELEASE)) {
                        CameraWrapper cameraWrapper = viewTribePager.getCameraWrapper();
                        tutorial = tutorialManager.showRelease(
                                this,
                                cameraWrapper
                        );
                    }

                    return currentTribe;
                })
                .subscribe(tribe -> viewTribePager.startRecording(tribe.getLocalId())));

        subscriptions.add(
                viewTribePager.onRecordEnd()
                .doOnNext(v -> {
                    soundManager.playSound(SoundManager.END_RECORD, SoundManager.SOUND_LOW);
                    cleanUpTutorial();
                    isRecording = false;
                    viewTribePager.stopRecording();
                    viewTribePager.showTapToCancel(currentTribe);
                })
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view -> {
                    if (tutorialManager.shouldDisplay(TutorialManager.CANCEL)) {
                        TileView tileView = viewTribePager.getBtnReply();
                        tutorial = tutorialManager.showTapToCancel(
                                this,
                                tileView.avatar,
                                tileView.avatar.getWidth() - screenUtils.dpToPx(40f),
                                screenUtils.dpToPx(20f),
                                v -> {
                                    cleanUpTutorial();
                                }
                        );
                    }
                }));

        subscriptions.add(viewTribePager.onClickTapToCancel()
                .subscribe(friendship -> {
                    viewTribePager.resetTileView(true, false);

                    if (tutorial != null) {
                        cleanUpTutorial();
                    }

                    tapToCancel();
                }));

        subscriptions.add(viewTribePager.onNotCancel()
                .subscribe(friendship -> {
                    viewTribePager.resetTileView(tutorial != null, true);

                    if (tutorial != null) {
                        cleanUpTutorial();
                    }

                    onNotCancel();
                }));

        subscriptions.add(viewTribePager.onClickEnableLocation()
                .subscribe(view -> {
                    subscriptions.add(RxPermissions.getInstance(this)
                            .request(PERMISSIONS_LOCATION)
                            .subscribe(granted -> {
                                if (granted) {
                                    subscriptions.add(reactiveLocationProvider
                                            .getLastKnownLocation().subscribe(locationProvided -> {
                                                if (locationProvided != null) {
                                                    Location location = new Location(locationProvided.getLongitude(), locationProvided.getLatitude());
                                                    location.setLatitude(location.getLatitude());
                                                    location.setLongitude(location.getLongitude());
                                                    location.setHasLocation(true);
                                                    location.setId(currentUser.getId());
                                                    currentUser.setLocation(location);
                                                } else {
                                                    currentUser.setLocation(null);
                                                }

                                                updateCurrentView();
                                            }));
                                }
                            }));
                }));

        subscriptions.add(viewTribePager.onClickMore()
                .flatMap(tribe -> DialogFactory.showBottomSheetForTribe(this, tribe, speedPlayback.get()),
                        ((tribeMessage, labelType) -> {
                            if (labelType.getTypeDef().equals(LabelType.TRIBE_SAVE)) {
                                FileUtils.saveToMediaStore(this, FileUtils.getPathForId(this, tribeMessage.getId(), FileUtils.VIDEO));
                                tribePresenter.markTribeAsSave(recipient, tribeMessage);
                            } else if (labelType.getTypeDef().equals(LabelType.TRIBE_INCREASE_SPEED)
                                    || labelType.getTypeDef().equals(LabelType.TRIBE_DECREASE_SPEED)) {
                                viewTribePager.changeSpeed();
                            }

                            return null;
                        }))
                .subscribe()
        );

        subscriptions.add(viewTribePager.onFirstLoop().subscribe(view -> {
            if (recipient.getReceivedTribes() != null && recipient.getReceivedTribes().size() > 1
                    && tutorialManager.shouldDisplay(TutorialManager.NEXT)) {
                View layoutNbTribes = viewTribePager.getLayoutNbTribes();
                layoutNbTribes.setDrawingCacheEnabled(true);
                layoutNbTribes.buildDrawingCache();
                Bitmap bitmapForTutorialOverlay = Bitmap.createBitmap(layoutNbTribes.getDrawingCache(true));
                layoutNbTribes.setDrawingCacheEnabled(false);
                tutorial = tutorialManager.showNext(
                        this,
                        layoutNbTribes,
                        (layoutNbTribes.getWidth() >> 1) + screenUtils.dpToPx(12.5f),
                        -screenUtils.dpToPx(20),
                        ((layoutNbTribes.getWidth() - screenUtils.dpToPx(20)) >> 1),
                        screenUtils.dpToPx(20f),
                        bitmapForTutorialOverlay,
                        layoutNbTribes.getWidth()
                );
            }

            if (tutorialManager.shouldDisplay(TutorialManager.CLOSE)) {
                View btnClose = viewTribePager.getBtnClose();
                btnClose.setDrawingCacheEnabled(true);
                btnClose.buildDrawingCache();
                Bitmap bitmapForTutorialOverlay = Bitmap.createBitmap(btnClose.getDrawingCache(true));
                btnClose.setDrawingCacheEnabled(false);
                tutorial = tutorialManager.showClose(
                        this,
                        btnClose,
                        (btnClose.getWidth() >> 1) + screenUtils.dpToPx(12.5f),
                        -screenUtils.dpToPx(20),
                        ((btnClose.getWidth() - screenUtils.dpToPx(20)) >> 1),
                        screenUtils.dpToPx(20f),
                        bitmapForTutorialOverlay,
                        btnClose.getWidth()
                );
            } else if (tutorialManager.shouldDisplay(TutorialManager.REPLY_MODE)) {
                TileView tileView = viewTribePager.getBtnReply();
                tutorial = tutorialManager.showReplyMode(
                        this,
                        tileView.avatar,
                        tileView.avatar.getWidth() - screenUtils.dpToPx(15),
                        screenUtils.dpToPx(20f),
                        v -> cleanUpTutorial()
                );
            }
        }));

        subscriptions.add(viewTribePager.onErrorTribe()
                .subscribe(tribeMessage -> {
                    FileUtils.delete(context(), tribeMessage.getLocalId(), FileUtils.VIDEO);
                    tribePresenter.updateTribeToDownload(tribeMessage.getId());
                }));
    }

    private void initDependencyInjector() {
        DaggerTribeComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initPresenter() {
        tribePresenter.onViewAttached(this);
        tribePresenter.loadTribes(recipient.getSubId());

        Set<String> userIds = new HashSet<>();

        for (TribeMessage message : recipient.getReceivedTribes()) {
            if (message.getFrom() != null) userIds.add(message.getFrom().getId());
            if (message.isDownloadError()) tribePresenter.updateTribeToDownload(message.getId());
        }

        tribePresenter.updateUserListScore(userIds);
    }

    @Override
    public void setCurrentTribe(TribeMessage tribe) {
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, 0);
    }

    @Override
    public void updateNewTribes(List<TribeMessage> tribeList) {
        if (tribeList != null && tribeList.size() > 0 && !isRecording) {
            viewTribePager.updateItems(tribeList);
        }
    }

    private void updateCurrentView() {
        viewTribePager.updateCurrentView();
    }

    private void cleanUpTutorial() {
        if (tutorial != null) {
            tutorial.cleanUp();
            tutorial = null;
        }
    }

    private void tapToCancel() {
        soundManager.playSound(SoundManager.TAP_TO_CANCEL, SoundManager.SOUND_LOW
        );
        FileUtils.delete(context(), currentTribe.getLocalId(), FileUtils.VIDEO);
        tribePresenter.deleteTribe(currentTribe);
        currentTribe = null;
    }

    private void onNotCancel() {
        soundManager.playSound(SoundManager.SENT, SoundManager.SOUND_LOW);
        tribePresenter.sendTribe(currentTribe);
        currentTribe = null;
    }

    @Override
    public void onBackPressed() {
        tribePresenter.markTribeListAsRead(recipient, viewTribePager.getTribeListSeens());

        super.onBackPressed();
    }

    // SENSOR MANAGER

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY:
                if (sensorEvent.values.length > 0) {
                    // From Android doc : Note: Some proximity sensors only support a binary near or far measurement.
                    // In this case, the sensor should report its maximum range value in the far state and a lesser value in the near state.
                    isNear = sensorEvent.values[0] < sensorEvent.sensor.getMaximumRange();
                }
                break;
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(sensorEvent.values, 0, lastAccels, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(sensorEvent.values, 0, lastMagFields, 0, 3);
                break;
            default:
                return;
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccels, lastMagFields)) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            float yaw = (float) (Math.toDegrees(orientation[0]));
            float pitch = (float) Math.toDegrees(orientation[1]);
            float roll = (float) Math.toDegrees(orientation[2]);

            yaw = filterYaw.lowPass(yaw);
            pitch = filterPitch.lowPass(pitch);
            roll = filterRoll.lowPass(roll);

            if (Math.abs(pitch) > 15 && Math.abs(pitch) < 50
                    && isNear
                    && Math.abs(roll) > 80 && Math.abs(roll) < 130
                    && !earMode) {
                earMode = true;
                viewTribePager.changeAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                audioManager.setSpeakerphoneOn(false);
                earModeView.setVisibility(View.VISIBLE);
            } else if ((Math.abs(pitch) < 15 || Math.abs(pitch) > 50
                    || !isNear
                    || Math.abs(roll) < 80 || Math.abs(roll) > 130)
                    && earMode) {
                earMode = false;
                viewTribePager.changeAudioStreamType(AudioManager.STREAM_MUSIC);
                audioManager.setSpeakerphoneOn(true);
                earModeView.setVisibility(View.GONE);
            }
        }
    }
}