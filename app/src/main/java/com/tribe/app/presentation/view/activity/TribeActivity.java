package com.tribe.app.presentation.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.io.Serializable;
import java.util.ArrayList;
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

public class TribeActivity extends BaseActivity implements TribeView, SensorEventListener {

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
    private BottomSheetDialog dialogMore;
    private LabelSheetAdapter moreTypeAdapter;
    private boolean isRecording = false;
    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Sensor proximity;
    private Tutorial tutorial;
    private boolean isReplyMode;

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
        tribePresenter.onResume();

        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        cleanUpTutorial();
        viewTribePager.onPause();
        tribePresenter.onPause();

        sensorManager.unregisterListener(this);

        super.onPause();
    }

    @Override
    protected void onStop() {
        tribePresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        tribePresenter.onDestroy();

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
    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initTribePagerView() {
        int color = PaletteGrid.get(position - 1);
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
                .doOnNext(view -> soundManager.playSound(SoundManager.START_RECORD))
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
                    soundManager.playSound(SoundManager.END_RECORD);
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

        subscriptions.add(viewTribePager.onClickMore().subscribe(tribeMessage -> {
            setupBottomSheetMore(tribeMessage);
        }));

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
        tribePresenter.onStart();
        tribePresenter.attachView(this);
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
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

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

    /**
     * Bottom sheet set-up
     */
    private void prepareBottomSheetCamera(TribeMessage tribe, List<LabelType> items) {
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_more, null);
        RecyclerView recyclerViewMore = (RecyclerView) view.findViewById(R.id.recyclerViewMore);
        recyclerViewMore.setHasFixedSize(true);
        recyclerViewMore.setLayoutManager(new LinearLayoutManager(this));
        moreTypeAdapter = new LabelSheetAdapter(this, items);
        moreTypeAdapter.setHasStableIds(true);
        recyclerViewMore.setAdapter(moreTypeAdapter);
        subscriptions.add(moreTypeAdapter.clickLabelItem()
                .map((View labelView) -> moreTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    MoreType moreType = (MoreType) labelType;

                    if (moreType.getMoreType().equals(MoreType.TRIBE_SAVE)) {
                        FileUtils.saveToMediaStore(this, FileUtils.getPathForId(this, tribe.getId(), FileUtils.VIDEO));
                        tribePresenter.markTribeAsSave(recipient, tribe);
                    } else if (moreType.getMoreType().equals(MoreType.TRIBE_INCREASE_SPEED)
                            || moreType.getMoreType().equals(MoreType.TRIBE_DECREASE_SPEED)) {
                        viewTribePager.changeSpeed();
                    }

                    dismissDialogSheetMore();
                }));

        dialogMore = new BottomSheetDialog(this);
        dialogMore.setContentView(view);
        dialogMore.show();
        dialogMore.setOnDismissListener(dialog -> {
            moreTypeAdapter.releaseSubscriptions();
            dialogMore = null;
        });
    }

    private void setupBottomSheetMore(TribeMessage tribe) {
        if (dismissDialogSheetMore()) {
            return;
        }

        List<LabelType> moreType = new ArrayList<>();
        if (tribe.isCanSave()) moreType.add(new MoreType(getString(R.string.tribe_more_save), MoreType.TRIBE_SAVE));
        moreType.add(new MoreType(
                speedPlayback.get().equals(TribePagerView.SPEED_NORMAL) ? getString(R.string.tribe_more_set_speed_2x) : getString(R.string.tribe_more_set_speed_1x),
                speedPlayback.get().equals(TribePagerView.SPEED_NORMAL) ? MoreType.TRIBE_INCREASE_SPEED : MoreType.TRIBE_DECREASE_SPEED));

        prepareBottomSheetCamera(tribe, moreType);
    }

    private boolean dismissDialogSheetMore() {
        if (dialogMore != null && dialogMore.isShowing()) {
            dialogMore.dismiss();
            return true;
        }

        return false;
    }

    private void cleanUpTutorial() {
        if (tutorial != null) {
            tutorial.cleanUp();
            tutorial = null;
        }
    }

    private void tapToCancel() {
        soundManager.playSound(SoundManager.TAP_TO_CANCEL);
        FileUtils.delete(context(), currentTribe.getLocalId(), FileUtils.VIDEO);
        tribePresenter.deleteTribe(currentTribe);
        currentTribe = null;
    }

    private void onNotCancel() {
        soundManager.playSound(SoundManager.SENT);
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

        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY &&
                sensorEvent.values.length > 0) {

            // From Android doc : Note: Some proximity sensors only support a binary near or far measurement.
            // In this case, the sensor should report its maximum range value in the far state and a lesser value in the near state.
            boolean isNear = sensorEvent.values[0] < sensorEvent.sensor.getMaximumRange();

            if (isNear) {
                audioManager.setSpeakerphoneOn(false);
                earModeView.setVisibility(View.VISIBLE);
                earModeView.setOnClickListener(view -> {

                    // Nothing here, just listening to catch the touch events.
                });

            } else {
                audioManager.setSpeakerphoneOn(true);
                earModeView.setVisibility(View.GONE);
                earModeView.setOnClickListener(null);
            }
        }
    }
}