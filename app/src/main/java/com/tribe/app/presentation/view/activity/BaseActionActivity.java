package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.scope.HasRatedApp;
import com.tribe.app.presentation.internal.di.scope.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.internal.di.scope.LastVersionCode;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.LocationPopup;
import com.tribe.app.presentation.internal.di.scope.WasAskedForCameraPermission;
import com.tribe.app.presentation.mvp.presenter.ActionPresenter;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.component.RatingView;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 25/10/2016.
 */
public class BaseActionActivity extends BaseActivity {

    @IntDef({ACTION_PERMISSIONS_CAMERA, ACTION_PERMISSIONS_LOCATION, ACTION_RATING, ACTION_NONE})
    public @interface ActionType {}

    public static final int ACTION_PERMISSIONS_CAMERA = 0;
    public static final int ACTION_PERMISSIONS_LOCATION = 1;
    public static final int ACTION_RATING = 2;
    public static final int ACTION_NONE = 3;

    private static final String TYPE = "TYPE";
    private static final String IS_ONBOARDING = "IS_ONBOARDING";

    public static Intent getCallingIntent(Context context, boolean isOnboarding, @ActionType int type) {
        Intent intent = new Intent(context, BaseActionActivity.class);
        intent.putExtra(TYPE, type);
        intent.putExtra(IS_ONBOARDING, isOnboarding);

        return intent;
    }

    @Inject
    User user;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @Inject
    @LocationPopup
    Preference<Boolean> locationPopup;

    @Inject
    @LocationContext
    Preference<Boolean> locationContext;

    @Inject
    @HasReceivedPointsForCameraPermission
    Preference<Boolean> hasReceivedPointsForCameraPermission;

    @Inject
    @WasAskedForCameraPermission
    Preference<Boolean> wasAskedForCameraPermission;

    @Inject
    @HasRatedApp
    Preference<Boolean> hasRatedApp;

    @Inject
    @LastVersionCode
    Preference<Integer> lastVersionCode;

    @Inject
    ActionPresenter actionPresenter;

    @BindView(R.id.layoutContent)
    ViewGroup layoutContent;

    @BindView(R.id.viewRating)
    RatingView viewRating;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.layoutPermission)
    ViewGroup layoutPermission;

    @BindView(R.id.txtPermissionType)
    TextViewFont txtPermissionType;

    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    @BindView(R.id.btnGo)
    TextViewFont btnGo;

    @BindView(R.id.btnSkip)
    View btnSkip;

    // RESOURCES

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // VARIABLES
    private int type;
    private boolean isOnboarding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDependencyInjector();
        initParams(getIntent());
        initResources();
        initUi();
        initSubscriptions();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        if (viewRating != null) viewRating.onDestroy();

        super.onDestroy();
    }

    private void initParams(Intent intent) {
        type = intent.getIntExtra(TYPE, ACTION_RATING);
        isOnboarding = intent.getBooleanExtra(IS_ONBOARDING, true);
    }

    private void initResources() {

    }

    private void initUi() {
        setContentView(R.layout.activity_action_base);
        unbinder = ButterKnife.bind(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        lastVersionCode.set(DeviceUtils.getVersionCode(this));

        if (type == ACTION_PERMISSIONS_CAMERA) {
            txtTitle.setText(R.string.permission_screen_camera_mic_title);
            setPermissions(ScoreUtils.Point.CAMERA);
            btnGo.setText(R.string.permission_screen_camera_mic_next_popup_authorize);
            inflater.inflate(R.layout.view_permission_camera, layoutContent, true);
        } else if (type == ACTION_PERMISSIONS_LOCATION) {
            txtTitle.setText(R.string.permission_screen_location_title);
            setPermissions(ScoreUtils.Point.LOCATION);
            btnGo.setText(R.string.permission_screen_location_next_popup_authorize);
            inflater.inflate(R.layout.view_permission_location, layoutContent, true);
        } else {
            viewRating.setVisibility(View.VISIBLE);
            txtTitle.setText(R.string.rating_title);
            layoutPermission.setVisibility(View.GONE);
            btnGo.setText(R.string.rating_rate);
        }
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void setPermissions(ScoreUtils.Point point) {
        txtPermissionType.setText(type == ACTION_PERMISSIONS_CAMERA ?
                R.string.permission_screen_camera_mic_points : R.string.permission_screen_location_points);
        txtPoints.setText(getString(R.string.points_suffix, "" + point.getPoints()));
    }

    @OnClick(R.id.btnSkip)
    void skip(View v) {
        if (type == ACTION_PERMISSIONS_CAMERA) {
            DialogFactory.createConfirmationDialog(this, getString(R.string.permission_screen_camera_mic_next_popup_title),
                    getString(R.string.permission_screen_camera_mic_next_popup_description),
                    getString(R.string.permission_screen_camera_mic_next_popup_authorize),
                    getString(R.string.permission_screen_camera_mic_next_popup_cancel),
                    (dialog, which) -> askForCameraPermissions(),
                    (dialog, which) -> handleFinish())
                    .show();
        } else if (type == ACTION_PERMISSIONS_LOCATION) {
            DialogFactory.createConfirmationDialog(this, getString(R.string.permission_screen_location_next_popup_title),
                    getString(R.string.permission_screen_location_next_popup_description),
                    getString(R.string.permission_screen_location_next_popup_authorize),
                    getString(R.string.permission_screen_location_next_popup_cancel),
                    (dialog, which) -> askForLocationPermissions(),
                    (dialog, which) -> handleFinish())
                    .show();
        } else if (type == ACTION_RATING) {
            finish();
        }
    }

    @OnClick(R.id.btnGo)
    void go(View v) {
        if (type == ACTION_PERMISSIONS_CAMERA) {
           askForCameraPermissions();
        } else if (type == ACTION_PERMISSIONS_LOCATION) {
            askForLocationPermissions();
        } else if (type == ACTION_RATING) {
            actionPresenter.updateScoreRating();
            hasRatedApp.set(true);
            navigator.rateApp(this);
            finish();
        }
    }

    private void askForCameraPermissions() {
        RxPermissions
                .getInstance(this)
                .request(PermissionUtils.PERMISSIONS_CAMERA)
                .subscribe(granted -> handleCameraPermissions(granted));
    }

    private void handleCameraPermissions(boolean isGranted) {
        if (isGranted) {
            if (!hasReceivedPointsForCameraPermission.get()) {
                hasReceivedPointsForCameraPermission.set(true);
                actionPresenter.updateScoreCamera();
            }
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.CAMERA_ENABLED, isGranted);
        bundle.putBoolean(TagManagerConstants.MICROPHONE_ENABLED, isGranted);
        tagManager.setProperty(bundle);

        handleFinish();
    }

    private void askForLocationPermissions() {
        subscriptions.add(RxPermissions.getInstance(this)
                .request(PermissionUtils.PERMISSIONS_LOCATION)
                .subscribe(granted -> handleLocationPermissions(granted), error -> handleFinish()));
    }

    private void handleLocationPermissions(boolean isGranted) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.LOCATION_ENABLED, isGranted);
        tagManager.setProperty(bundle);

        if (isGranted) {
            actionPresenter.updateScoreLocation();
            locationPopup.set(true);
            locationContext.set(true);
            subscriptions.add(reactiveLocationProvider
                    .getLastKnownLocation().subscribe(locationProvided -> {
                        if (locationProvided != null) {
                            Location location = new Location(locationProvided.getLongitude(), locationProvided.getLatitude());
                            location.setLatitude(location.getLatitude());
                            location.setLongitude(location.getLongitude());
                            location.setHasLocation(true);
                            location.setId(user.getId());
                            user.setLocation(location);
                        } else {
                            user.setLocation(null);
                        }

                        handleFinish();
                    }));
        } else {
            handleFinish();
        }
    }

    private void handleFinish() {
        Observable.timer(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            if (type == ACTION_PERMISSIONS_CAMERA)
                navigator.computeActions(this, true, ACTION_PERMISSIONS_LOCATION);
            else if (type == ACTION_PERMISSIONS_LOCATION)
                navigator.navigateToHome(this, false, null);

            finish();
        });
    }

    @Override
    public void finish() {
        super.finish();

        if (!isOnboarding)
            overridePendingTransition(0, R.anim.slide_out_down);
    }
}
