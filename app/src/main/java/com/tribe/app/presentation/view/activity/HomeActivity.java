package com.tribe.app.presentation.view.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.network.DownloadTribeService;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.PendingType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.utils.DeepLinkUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.HasRatedApp;
import com.tribe.app.presentation.utils.preferences.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.utils.preferences.LastOnlineNotification;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LocationContext;
import com.tribe.app.presentation.utils.preferences.TribeSentCount;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.FilterView;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.TopBarContainer;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeGridMVPView, GoogleApiClient.OnConnectionFailedListener {

    private static final SpringConfig FILTER_VIEW_BOUNCE_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(1f, 2.5f);
    private static final SpringConfig FILTER_VIEW_NO_BOUNCE_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(1f, 2.5f);

    private static final int TIME_MIN_RECORDING = 1500; // IN MS
    private static final float OVERSHOOT_TENSION_LIGHT = 1.25f;
    private static final int DURATION = 600;
    private static final int DURATION_FAST = 300;

    public static final int SETTINGS_RESULT = 101, TRIBES_RESULT = 104;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Inject
    HomeGridPresenter homeGridPresenter;

    @Inject
    HomeGridAdapter homeGridAdapter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    PaletteGrid paletteGrid;

    @Inject
    TutorialManager tutorialManager;

    @Inject
    @TribeSentCount
    Preference<Integer> tribeSentCount;

    @Inject
    @HasReceivedPointsForCameraPermission
    Preference<Boolean> hasReceivedPointsForCameraPermission;

    @Inject
    @HasRatedApp
    Preference<Boolean> hasRatedApp;

    @Inject
    @LastOnlineNotification
    Preference<Long> lastOnlineNotification;

    @Inject
    @AddressBook
    Preference<Boolean> addressBook;

    @Inject
    @LastVersionCode
    Preference<Integer> lastVersion;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @Inject
    @LocationContext
    Preference<Boolean> locationContext;

    @Inject
    SoundManager soundManager;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    @BindView(R.id.viewFilter)
    FilterView viewFilter;

    @BindView(R.id.viewBG)
    View viewBG;

    @BindView(R.id.imgFilter)
    View imgFilter;

    @BindView(R.id.imgFilterSelected)
    View imgFilterSelected;

    @BindView(R.id.imgBackToTop)
    View imgBackToTop;

    @BindView(R.id.topBarContainer)
    TopBarContainer topBarContainer;

    // OBSERVABLES
    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeLayoutManager layoutManager;
    private boolean isRecording;
    private long timeRecording;
    private Tutorial tutorial;
    private List<TribeMessage> pendingTribes;
    private BottomSheetDialog bottomSheetPendingTribeDialog;
    private RecyclerView recyclerViewPending;
    private LabelSheetAdapter labelSheetAdapter;
    private boolean isFilterMode = false;
    private boolean hasFilter = false;
    private boolean isAnimatingNavigation = false;
    private boolean canEndRefresh = false;
    private List<Recipient> latestRecipientList;
    private boolean shouldOverridePendingTransactions = false;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springFilterView;
    private FilterSpringListener springFilterListener;
    private Spring springGrid;
    private GridSpringListener springGridListener;

    // DIMEN
    private int marginHorizontalSmall, translationBackToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.black);

        super.onCreate(savedInstanceState);

        initDependencyInjector();
        init();
        initUi();
        initDimensions();
        initCamera();
        initRegistrationToken();
        initFilterView();
        initRecyclerView();
        initPullToRefresh();
        initRemoteConfig();
        manageDeepLink(getIntent());

        subscriptions.add(
                Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            startService(DownloadTribeService.getCallingIntent(this, null));
                        }));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        manageDeepLink(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tagManager.onStart(this);
        homeGridPresenter.onViewAttached(this);
    }

    @Override
    protected void onStop() {
        tagManager.onStop(this);
        homeGridPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!lastVersion.get().equals(DeviceUtils.getVersionCode(this))) {
            subscriptions.add(RxPermissions.getInstance(HomeActivity.this)
                    .requestEach(PermissionUtils.PERMISSIONS_HOME)
                    .subscribe(granted -> {
                        lastVersion.set(DeviceUtils.getVersionCode(this));
                        handleCameraPermissions(PermissionUtils.hasPermissionsCamera(this), false);
                        handleLocationPermissions(PermissionUtils.hasPermissionsLocation(this));
                    }));
        } else {
            subscriptions.add(Observable.from(PermissionUtils.PERMISSIONS_CAMERA)
                    .map(permission -> RxPermissions.getInstance(HomeActivity.this).isGranted(permission))
                    .toList()
                    .subscribe(grantedList -> {
                        boolean areAllGranted = true;

                        for (Boolean granted : grantedList) {
                            if (!granted) areAllGranted = false;
                        }

                        handleCameraPermissions(areAllGranted, false);
                    })
            );
        }

        if (shouldOverridePendingTransactions) {
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
            shouldOverridePendingTransactions = false;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        firebaseRemoteConfig.fetch(BuildConfig.DEBUG ? 1 : 3600)
            .addOnSuccessListener(aVoid -> {
                firebaseRemoteConfig.activateFetched();
                sendOnlineNotification();
            })
            .addOnFailureListener(exception -> Log.d("Tribe", "Fetch failed"));
    }

    @Override
    protected void onPause() {
        cameraWrapper.onPause();
        cleanTutorial();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(this, DownloadTribeService.class);
        stopService(i);

        recyclerViewFriends.setAdapter(null);
        springFilterView.removeAllListeners();
        springGrid.removeAllListeners();
        springSystem.removeAllListeners();

        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

        super.onDestroy();
    }

    private void init() {
        pendingTribes = new ArrayList<>();
        latestRecipientList = new ArrayList<>();
    }

    private void initUi() {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    private void initDimensions() {
        marginHorizontalSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        translationBackToTop = getResources().getDimensionPixelSize(R.dimen.transition_grid_back_to_top);
    }

    private void initCamera() {
        int marginBounds = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);

        cameraWrapper.initDimens(
                screenUtils.getHeightPx()
                        - marginBounds
                        - cameraWrapper.getHeightFromRatio(),
                marginBounds,
                marginBounds,
                marginBounds,
                marginBounds,
                marginBounds,
                true
        );

        subscriptions.add(cameraWrapper.cameraPermissions().subscribe(aVoid -> {
            RxPermissions.getInstance(this)
                    .request(PermissionUtils.PERMISSIONS_CAMERA)
                    .subscribe(granted -> {
                        handleCameraPermissions(granted, true);
                    });
        }));
    }

    private void askLocationPermissions() {
        subscriptions.add(RxPermissions.getInstance(this)
                .request(PermissionUtils.PERMISSIONS_LOCATION)
                .subscribe(granted -> handleLocationPermissions(granted)));
    }

    private void handleLocationPermissions(boolean isGranted) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.LOCATION_ENABLED, isGranted);
        tagManager.setProperty(bundle);

        if (isGranted) {
            homeGridPresenter.updateScoreLocation();
            locationContext.set(true);
            subscriptions.add(reactiveLocationProvider
                    .getLastKnownLocation().subscribe(locationProvided -> {
                        if (locationProvided != null) {
                            Location location = new Location(locationProvided.getLongitude(), locationProvided.getLatitude());
                            location.setLatitude(location.getLatitude());
                            location.setLongitude(location.getLongitude());
                            location.setHasLocation(true);
                            location.setId(getCurrentUser().getId());
                            getCurrentUser().setLocation(location);
                        } else {
                            getCurrentUser().setLocation(null);
                        }
                    }));
        } else {
            locationContext.set(false);
        }
    }

    private void handleCameraPermissions(boolean isGranted, boolean shouldAnimate) {
        if (isGranted) {
            if (!hasReceivedPointsForCameraPermission.get()) {
                hasReceivedPointsForCameraPermission.set(true);
                homeGridPresenter.updateScoreCamera();
            }

            cameraWrapper.onResume(shouldAnimate);
        } else {
            cameraWrapper.showPermissions();
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.CAMERA_ENABLED, isGranted);
        tagManager.setProperty(bundle);
    }

    private void initRecyclerView() {
        this.layoutManager = new HomeLayoutManager(context());
        this.recyclerViewFriends.setLayoutManager(layoutManager);
        this.recyclerViewFriends.setItemAnimator(null);
        homeGridAdapter.setItems(new ArrayList<>());
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

        // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

        this.layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(homeGridAdapter.getItemViewType(position)) {
                    case HomeGridAdapter.EMPTY_HEADER_VIEW_TYPE:
                        return layoutManager.getSpanCount();
                    default:
                        return 1;
                }
            }
        });

        recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                float percent = recyclerView.computeVerticalScrollOffset() / translationBackToTop;

                if (!hasFilter && percent >= 1 && !isAnimatingNavigation && imgFilter.getTranslationY() == 0) {
                    isAnimatingNavigation = true;
                    showView(imgBackToTop, DURATION_FAST);
                    hideView(imgFilter, DURATION_FAST);
                } else if (!hasFilter && percent < 1 && !isAnimatingNavigation && imgFilter.getTranslationY() == translationBackToTop) {
                    isAnimatingNavigation = true;
                    hideView(imgBackToTop, DURATION_FAST);
                    showView(imgFilter, DURATION_FAST);
                }
            }
        });

        subscriptions.add(homeGridAdapter.onOpenTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .filter(recipient -> {
                    boolean filter = recipient.getReceivedTribes() != null
                            && recipient.getReceivedTribes().size() > 0
                            && recipient.hasLoadedOrErrorTribes();

                    if (filter) soundManager.playSound(SoundManager.OPEN_TRIBE, SoundManager.SOUND_LOW);

                    return filter;
                })
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(recipient -> {
                    if (tutorial == null) navigateToTribes(recipient);
                    else {
                        tutorial.cleanUp();
                        tutorial = null;

                        subscriptions.add(
                                Observable
                                        .timer(300, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(aLong -> navigateToTribes(recipient)));
                    }
                }));

        subscriptions.add(homeGridAdapter.onClickChat()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    navigateToChat(recipient);
                }));

        subscriptions.add(homeGridAdapter.onClickMore()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .flatMap(recipient -> {
                            if (recipient instanceof Membership && (recipient.getReceivedTribes() == null || recipient.getReceivedTribes().size() == 0)) {
                                Membership membership = (Membership) recipient;
                                navigator.navigateToGroupDetails(this, membership);
                                return Observable.empty();
                            } else {
                                return DialogFactory.showBottomSheetForRecipient(this, recipient);
                            }
                        },
                        ((recipient, labelType) -> {
                            if (labelType != null) {
                                if (labelType.getTypeDef().equals(LabelType.CLEAR_MESSAGES)) {
                                    homeGridPresenter.markTribeListAsRead(recipient, recipient.getReceivedTribes());
                                } else if (labelType.getTypeDef().equals(LabelType.HIDE) || labelType.getTypeDef().equals(LabelType.BLOCK_HIDE)) {
                                    tagManager.trackEvent(TagManagerConstants.USER_TILE_HIDDEN);
                                    homeGridPresenter.updateFriendship((Friendship) recipient, labelType.getTypeDef().equals(LabelType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED : FriendshipRealm.HIDDEN);
                                } else if (labelType.getTypeDef().equals(LabelType.GROUP_INFO)) {
                                    Membership membership = (Membership) recipient;
                                    navigator.navigateToGroupDetails(this, membership);
                                } else if (labelType.getTypeDef().equals(LabelType.GROUP_LEAVE)) {
                                    homeGridPresenter.leaveGroup(recipient.getId());
                                }
                            }

                            return recipient;
                        }))
                .subscribe());


        subscriptions.add(homeGridAdapter.onClickErrorTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .flatMap(recipient -> DialogFactory.showBottomSheetForPending(this, recipient),
                        ((recipient, labelType) -> {
                            PendingType pendingType = (PendingType) labelType;

                            List<TribeMessage> messages = new ArrayList<>();
                            for (Message message : pendingType.getPending()) {
                                messages.add((TribeMessage) message);
                            }

                            if (pendingType.getPendingType().equals(PendingType.DELETE)) {
                                homeGridPresenter.deleteTribe(pendingType.getPending().toArray(new TribeMessage[pendingType.getPending().size()]));
                            } else {
                                homeGridPresenter.sendTribe(pendingType.getPending().toArray(new TribeMessage[pendingType.getPending().size()]));
                            }

                            return null;
                        }))
                .subscribe()
        );

        subscriptions.add(homeGridAdapter.onRecordStart()
                .doOnNext(view -> {
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
                    isRecording = true;
                    soundManager.playSound(SoundManager.START_RECORD, SoundManager.SOUND_LOW);
                })
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipient -> {
                    timeRecording = System.currentTimeMillis();
                    TribeMessage currentTribe = homeGridPresenter.createTribe(getCurrentUser(), recipient, cameraWrapper.getTribeMode());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), currentTribe);
                    recyclerViewFriends.postDelayed(() -> homeGridAdapter.notifyItemChanged(recipient.getPosition()), 300);
                    cameraWrapper.onStartRecord(currentTribe.getLocalId());
                })
        );

        subscriptions.add(homeGridAdapter.onRecordEnd()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(view -> cameraWrapper.onEndRecord())
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    soundManager.playSound(SoundManager.END_RECORD, SoundManager.SOUND_LOW);
                    TileView tileView = (TileView) layoutManager.findViewByPosition(recipient.getPosition());

                    if ((System.currentTimeMillis() - timeRecording) > TIME_MIN_RECORDING) {
                        tileView.showTapToCancel(recipient.getTribe(), cameraWrapper.getTribeMode());
                        recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                    } else {
                        cleanupCurrentTribe(recipient);
                        tileView.resetViewAfterTapToCancel(false);
                    }

                    isRecording = false;
                })
        );

        subscriptions.add(homeGridAdapter.onClickTapToCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    soundManager.playSound(SoundManager.TAP_TO_CANCEL, SoundManager.SOUND_LOW);
                    cleanupCurrentTribe(recipient);
                })
        );

        subscriptions.add(homeGridAdapter.onNotCancel()
                .filter(recipient -> {
                    soundManager.playSound(SoundManager.SENT, SoundManager.SOUND_LOW);
                    TribeMessage tr = recipient.getTribe();

                    if (tr == null || tr.getTo() == null) {
                        cleanupCurrentTribe(recipient);
                        return false;
                    }

                    return true;
                })
                .map(recipient -> {
                    TribeMessage tr = recipient.getTribe();

                    homeGridPresenter.sendTribe(recipient.getTribe());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
                    return tr.getLocalId();
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(s -> {
                    int tribeSent = tribeSentCount.get();
                    tribeSentCount.set(++tribeSent);

                    if (tribeSentCount.get() >= Constants.RATING_COUNT && tribeSentCount.get() % Constants.RATING_COUNT == 0 && !hasRatedApp.get())
                        navigator.computeActions(this, false, BaseActionActivity.ACTION_RATING);
                })
                .delay(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> homeGridPresenter.confirmTribe(s))
        );
    }

    private void initFilterView() {
        springSystem = SpringSystem.create();

        springFilterListener = new FilterSpringListener();
        springFilterView = springSystem.createSpring();
        springFilterView.setSpringConfig(FILTER_VIEW_BOUNCE_SPRING_CONFIG);
        springFilterView.addListener(springFilterListener);
        springFilterView.setCurrentValue(screenUtils.getHeightPx(), true);

        springGridListener = new GridSpringListener();
        springGrid = springSystem.createSpring();
        springGrid.setSpringConfig(FILTER_VIEW_NO_BOUNCE_SPRING_CONFIG);
        springGrid.addListener(springGridListener);
        springGrid.setCurrentValue(1f).setAtRest();

        subscriptions.add(viewFilter.onCloseClick().subscribe(aVoid -> {
            hideFilterView();
        }));

        subscriptions.add(viewFilter.onLetterSelected().subscribe(s -> {
            hasFilter = true;
            hideView(imgFilter, DURATION);
            showView(imgFilterSelected, DURATION);
            homeGridAdapter.filterList(s);
            hideFilterView();
        }));

        subscriptions.add(viewFilter.onSyncFBClick().subscribe(syncView -> {
            if (!syncView.isActive())
                homeGridPresenter.loginFacebook();
            else {
                Bundle bundle = new Bundle();
                bundle.putBoolean(TagManagerConstants.FACEBOOK_CONNECTED, false);
                tagManager.setProperty(bundle);
                homeGridPresenter.updateUserFacebook(null);
                FacebookUtils.logout();
                viewFilter.setFBSync(false);
            }
        }));

        subscriptions.add(viewFilter.onSyncABClick().subscribe(syncView -> {
            if (!syncView.isActive()) {
                RxPermissions.getInstance(this)
                        .request(Manifest.permission.READ_CONTACTS)
                        .subscribe(hasPermission -> {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED, hasPermission);
                            tagManager.setProperty(bundle);

                            addressBook.set(hasPermission);
                            viewFilter.setABSync(hasPermission);
                        });
            } else {
                Bundle bundle = new Bundle();
                bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED, false);
                tagManager.setProperty(bundle);

                addressBook.set(false);
                viewFilter.setABSync(false);
            }
        }));
    }

    private void initDependencyInjector() {
        this.userComponent = DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();

        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void initPullToRefresh() {
        subscriptions.add(topBarContainer.onRefresh()
                .filter(b -> b)
                .doOnNext(b -> {
                    canEndRefresh = false;
                    homeGridPresenter.reload();
                })
                .delay(TopBarContainer.MIN_LENGTH, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(refresh -> {
                    if (canEndRefresh) topBarContainer.setRefreshing(false, false);

                    canEndRefresh = true;
                }));

        subscriptions.add(topBarContainer.onClickSettings()
                .subscribe(aVoid -> {
                    navigateToSettings();
                }));

        subscriptions.add(topBarContainer.onClickInvites()
                .flatMap(aVoid -> DialogFactory.showBottomSheetForInvites(this),
                        ((aVoid, labelType) -> {
                            if (labelType.getTypeDef().equals(LabelType.SEARCH)) {
                                navigateToSearch(null);
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_SMS)) {
                                navigateToInvitesSMS();
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_WHATSAPP)) {
                                navigateToInvitesWhatsapp();
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_MESSENGER)) {
                                navigateToInvitesMessenger();
                            }

                            return null;
                        }))
                .subscribe()
        );

        subscriptions.add(topBarContainer.onClickGroups()
                .subscribe(aVoid -> {
                    navigateToCreateGroup();
                })
        );
    }

    private void initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.firebase_default_config);
    }

    @Override
    public void onDeepLink(String url) {
        if (!StringUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);

            if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
                if (uri.getPath().startsWith("/u/")) {
                    navigateToSearch(StringUtils.getLastBitFromUrl(url));
                } else if (uri.getPath().startsWith("/g/")) {
                    homeGridPresenter.createMembership(StringUtils.getLastBitFromUrl(url));
                }
            }
        }
    }

    @Override
    public void renderRecipientList(List<Recipient> recipientList) {
        if (recipientList != null && !isRecording && tutorial == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(TagManagerConstants.COUNT_FRIENDS, getCurrentUser().getFriendships().size());
            bundle.putInt(TagManagerConstants.COUNT_GROUPS, getCurrentUser().getFriendships().size());
            tagManager.setProperty(bundle);

            homeGridAdapter.setItems(recipientList);
            viewFilter.updateFilterList(recipientList);

            if (tutorialManager.shouldDisplay(TutorialManager.MESSAGES_SUPPORT) && !topBarContainer.isRefreshing()) {
                computeSupportTutorial(recipientList);
            }
        }
    }

    private void computeSupportTutorial(List<Recipient> recipientList) {
        for (Recipient recipient : recipientList) {
            if (Constants.SUPPORT_ID.equals(recipient.getSubId()) && recipient.getReceivedTribes() != null
                    && recipient.getReceivedTribes().size() == 2 && recipient.hasLoadedOrErrorTribes()) {
                subscriptions.add(
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(aLong -> {
                                    View view = layoutManager.findViewByPosition(0);
                                    if (view != null && view instanceof SquareFrameLayout) {
                                        TileView tileView = (TileView) view.findViewById(R.id.viewTile);
                                        if (tileView != null && tileView.getType() == TileView.TYPE_SUPPORT) {
                                            tileView.layoutNbTribes.setDrawingCacheEnabled(true);
                                            tileView.layoutNbTribes.buildDrawingCache();
                                            Bitmap bitmapForTutorialOverlay = Bitmap.createBitmap(tileView.layoutNbTribes.getDrawingCache(true));
                                            tileView.layoutNbTribes.setDrawingCacheEnabled(false);

                                            tutorial = tutorialManager.showMessagesSupport(
                                                    this,
                                                    tileView.avatar,
                                                    (tileView.avatar.getWidth() >> 1) + screenUtils.dpToPx(10),
                                                    -screenUtils.dpToPx(15),
                                                    (tileView.avatar.getWidth() >> 1) - screenUtils.dpToPx(1),
                                                    screenUtils.dpToPx(20f),
                                                    bitmapForTutorialOverlay,
                                                    tileView.layoutNbTribes.getWidth()
                                            );
                                        }
                                    }
                                }));

            }
        }
    }

    @Override
    public void updateReceivedMessages(List<Message> messageList) {
        topBarContainer.showNewMessages(messageList);
    }

    @Override
    public void updatePendingTribes(List<TribeMessage> pendingTribes) {
        this.pendingTribes.clear();
        this.pendingTribes.addAll(pendingTribes);
    }

    @Override
    public void showPendingTribesMenu() {

    }

    @Override
    public void scrollToTop() {

    }

    @Override
    public int getNbItems() {
        return 0;
    }

    @Override
    public void refreshGrid() {

    }

    @Override
    public void onFriendshipUpdated(Friendship friendship) {

    }

    @Override
    public void successFacebookLogin() {
        homeGridPresenter.updateUserFacebook(AccessToken.getCurrentAccessToken().getUserId());
        viewFilter.setFBSync(true);
    }

    @Override
    public void errorFacebookLogin() {
        viewFilter.setFBSync(false);
    }

    @Override
    public void onMembershipCreated(Membership membership) {

    }

    private void initRegistrationToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) homeGridPresenter.sendToken(token);
    }

    private void manageDeepLink(Intent intent) {
        if (intent != null) {
            if (intent.getData() != null) {
                homeGridPresenter.getHeadDeepLink(intent.getDataString());
            } else if (!StringUtils.isEmpty(intent.getAction()) && intent.getAction().equals(DeepLinkUtils.MESSAGE_ACTION)) {
                String recipientId = intent.getStringExtra("t_from");
                boolean isToGroup = Boolean.valueOf(intent.getStringExtra("to_group"));
                navigator.navigateToChat(this, recipientId, isToGroup);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isFilterMode) {
            super.onBackPressed();
            // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
            // and transparent floating activity and auto finishes)
            startActivity(new Intent(this, DummyActivity.class));
            finish();
        } else {
            hideFilterView();
        }
    }

    @Override
    public UserComponent getComponent() {
        return userComponent;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {
        if (topBarContainer.isRefreshing() && canEndRefresh) topBarContainer.setRefreshing(false, false);
        canEndRefresh = true;
    }

    @Override
    public void showError(String message) {
        topBarContainer.showError();
    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("TRIBE", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private void cleanTutorial() {
        if (tutorial != null) {
            tutorial.cleanUp();
            tutorial = null;
        }
    }

    private void cleanupCurrentTribe(Recipient recipient) {
        if (recipient.getTribe() != null) homeGridPresenter.deleteTribe(recipient.getTribe());
        homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
        homeGridAdapter.notifyItemChanged(recipient.getPosition());
    }

    private void navigateToSettings() {
        navigator.navigateToSettings(HomeActivity.this, SETTINGS_RESULT);
    }

    private void navigateToSearch(String username) {
        navigator.navigateToSearchUser(HomeActivity.this, username);
    }

    private void navigateToTribes(Recipient recipient) {
        HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, recipient.getPosition(), recipient, TRIBES_RESULT);
    }

    private void navigateToChat(Recipient recipient) {
        HomeActivity.this.navigator.navigateToChat(HomeActivity.this, recipient.getSubId(), recipient instanceof Membership);
    }

    private void navigateToInvitesSMS() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openSms(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToInvitesMessenger() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openFacebookMessenger(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToInvitesWhatsapp() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openWhatsApp(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToCreateGroup() {
        HomeActivity.this.navigator.navigateToCreateGroup(this);
    }

    @Override
    public void setCurrentTribe(TribeMessage tribe) {

    }

    @OnClick(R.id.imgFilter)
    void openFilterView() {
        if (!isRecording) showFilterView();
    }

    @OnClick(R.id.imgFilterSelected)
    void clearFilter() {
        hasFilter = false;
        hideView(imgFilterSelected, DURATION);
        showView(imgFilter, DURATION);
        homeGridAdapter.filterList(null);
    }

    @OnClick(R.id.viewBG)
    void closeFilterView() {
        hideFilterView();
    }

    @OnClick(R.id.imgBackToTop)
    void backToTop() {
        if (homeGridAdapter != null && layoutManager.findFirstVisibleItemPosition() > 15)
            this.recyclerViewFriends.scrollToPosition(10);

        this.recyclerViewFriends.postDelayed(() -> recyclerViewFriends.smoothScrollToPosition(0), 100);
    }

    private void hideFilterView() {
        isFilterMode = false;
        springFilterView.setSpringConfig(FILTER_VIEW_NO_BOUNCE_SPRING_CONFIG);
        springFilterView.setEndValue(screenUtils.getHeightPx());
        springGrid.setEndValue(1f);
        recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
        homeGridAdapter.setAllItemsEnabled(true);
    }

    private void showFilterView() {
        isFilterMode = true;
        viewBG.setVisibility(View.VISIBLE);
        springFilterView.setSpringConfig(FILTER_VIEW_BOUNCE_SPRING_CONFIG);
        springFilterView.setEndValue(marginHorizontalSmall);
        springGrid.setEndValue(0.925f);
        recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
        homeGridAdapter.setAllItemsEnabled(false);
    }

    private void hideView(View view, int duration) {
        view.clearAnimation();
        view.animate()
                .setDuration(duration)
                .translationY(translationBackToTop)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void showView(View view, int duration) {
        view.clearAnimation();
        view.animate()
                .setDuration(duration)
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION_LIGHT))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate().setListener(null).start();
                        isAnimatingNavigation = false;
                    }
                })
                .start();
    }

    private class FilterSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            int value = (int) spring.getCurrentValue();
            viewFilter.setTranslationY(value);
            float alpha = 1 - ((float) SpringUtil.mapValueFromRangeToRange(value, marginHorizontalSmall, screenUtils.getHeightPx(), 0, 1));
            viewBG.setAlpha(alpha);

            if (spring.getEndValue() == screenUtils.getHeightPx() && spring.getEndValue() - value > marginHorizontalSmall * 2) {
                viewBG.setVisibility(View.GONE);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            if (spring.getEndValue() == screenUtils.getHeightPx())
                viewFilter.clean();
        }
    }

    private class GridSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            scale(value);
        }
    }

    private void scale(float value) {
        recyclerViewFriends.setScaleX(value);
        recyclerViewFriends.setScaleY(value);
    }

    private void sendOnlineNotification() {
        boolean canSendOnlineNotification = !firebaseRemoteConfig.getBoolean(Constants.FIREBASE_DISABLE_ONLINE_NOTIFICATIONS);

        int onlineNotificationIntervalMs = Integer.valueOf(firebaseRemoteConfig.getString(Constants.FIREBASE_DELAY_ONLINE_NOTIFICATIONS)) * 60 * 1000;

        if (canSendOnlineNotification && (System.currentTimeMillis() - lastOnlineNotification.get()) > onlineNotificationIntervalMs) {
            //homeGridPresenter.sendOnlineNotification();
            lastOnlineNotification.set(System.currentTimeMillis());
        }
    }
}