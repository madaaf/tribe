package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.DownloadTribeService;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.entity.PendingType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.internal.di.scope.HasRatedApp;
import com.tribe.app.presentation.internal.di.scope.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.internal.di.scope.TribeSentCount;
import com.tribe.app.presentation.internal.di.scope.WasAskedForCameraPermission;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.utils.DeepLinkUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.FilterView;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.Constants;
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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeGridView, GoogleApiClient.OnConnectionFailedListener {

    private static final SpringConfig FILTER_VIEW_BOUNCE_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(1f, 20f);

    private static final int TIME_MIN_RECORDING = 1500; // IN MS

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
    TutorialManager tutorialManager;

    @Inject
    @TribeSentCount
    Preference<Integer> tribeSentCount;

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
    SoundManager soundManager;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    @BindView(R.id.viewFilter)
    FilterView viewFilter;

    // OBSERVABLES
    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeLayoutManager layoutManager;
    private BottomSheetDialog dialogMore;
    private RecyclerView recyclerViewMore;
    private LabelSheetAdapter moreSheetAdapter;
    private boolean isRecording;
    private long timeRecording;
    private Tutorial tutorial;
    private List<TribeMessage> pendingTribes;
    private BottomSheetDialog bottomSheetPendingTribeDialog;
    private RecyclerView recyclerViewPending;
    private LabelSheetAdapter labelSheetAdapter;
    private boolean isFilterMode = false;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springFilterView;
    private FilterSpringListener springFilterListener;
    //private Spring springFilterView;
    //private FilterSpringListener springFilterListener;

    // DIMEN
    private int marginHorizontalSmall, translationBackToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        init();
        initUi();
        initDimensions();
        initCamera();
        initPresenter();
        initRegistrationToken();
        initFilterView();
        initRecyclerView();
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
    }

    @Override
    protected void onStop() {
        tagManager.onStop(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadData();

        subscriptions.add(Observable.
                from(PermissionUtils.PERMISSIONS_CAMERA)
                .map(permission -> RxPermissions.getInstance(HomeActivity.this).isGranted(permission))
                .toList()
                .subscribe(grantedList -> {
                    boolean areAllGranted = true;

                    for (Boolean granted : grantedList) {
                        if (!granted) areAllGranted = false;
                    }

                    handleCameraPermissions(areAllGranted, false);
                }));
    }

    @Override
    protected void onPause() {
        cameraWrapper.onPause();
        this.homeGridPresenter.onPause();
        cleanTutorial();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(this, DownloadTribeService.class);
        stopService(i);

        recyclerViewFriends.setAdapter(null);

        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

        super.onDestroy();
    }

    private void init() {
        pendingTribes = new ArrayList<>();
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

    private void handleCameraPermissions(boolean isGranted, boolean shouldAnimate) {
        wasAskedForCameraPermission.set(true);

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
        List<Recipient> recipientList = new ArrayList<>();
        Friendship friendship = new Friendship(getCurrentUser().getId());
        friendship.setFriend(getCurrentUser());
        recipientList.add(0, friendship);
        homeGridAdapter.setItems(recipientList);
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

        // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

        recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                recyclerView.computeVerticalScrollOffset();
                // TODO SCROLL
            }
        });

        subscriptions.add(homeGridAdapter.onOpenTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .filter(recipient -> {
                    boolean filter = recipient.getReceivedTribes() != null
                            && recipient.getReceivedTribes().size() > 0
                            && recipient.hasLoadedOrErrorTribes();

                    if (filter) soundManager.playSound(SoundManager.OPEN_TRIBE);

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
                .subscribe(recipient -> {
                    setupBottomSheetMore(recipient);
                }));

        subscriptions.add(homeGridAdapter.onClickErrorTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    setupBottomSheetPendingTribes(recipient);
                }));

        subscriptions.add(homeGridAdapter.onRecordStart()
                .doOnNext(view -> {
                    isRecording = true;
                    soundManager.playSound(SoundManager.START_RECORD);
                })
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipient -> {
                    timeRecording = System.currentTimeMillis();
                    TribeMessage currentTribe = homeGridPresenter.createTribe(getCurrentUser(), recipient, cameraWrapper.getTribeMode());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), currentTribe);
                    recyclerViewFriends.postDelayed(() -> homeGridAdapter.notifyItemChanged(recipient.getPosition()), 300);
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
                    isRecording = true;
                    cameraWrapper.onStartRecord(currentTribe.getLocalId());
                }));

        subscriptions.add(homeGridAdapter.onRecordEnd()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(view -> {
                    isRecording = false;
                    cameraWrapper.onEndRecord();
                })
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    soundManager.playSound(SoundManager.END_RECORD);
                    TileView tileView = (TileView) layoutManager.findViewByPosition(recipient.getPosition());

                    if ((System.currentTimeMillis() - timeRecording) > TIME_MIN_RECORDING) {
                        tileView.showTapToCancel(recipient.getTribe(), cameraWrapper.getTribeMode());
                        recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                    } else {
                        cleanupCurrentTribe(recipient);
                        tileView.resetViewAfterTapToCancel(false);
                    }
                }));

        subscriptions.add(homeGridAdapter.onClickTapToCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    isRecording = false;
                    soundManager.playSound(SoundManager.TAP_TO_CANCEL);
                    cleanupCurrentTribe(recipient);
                }));

        subscriptions.add(homeGridAdapter.onNotCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .filter(recipient -> {
                    isRecording = false;
                    soundManager.playSound(SoundManager.SENT);
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
                .subscribe(s -> homeGridPresenter.confirmTribe(s)));

        subscriptions.add(homeGridAdapter.onClickOpenSettings()
                .subscribe(view -> navigateToSettings()));
    }

    private void initFilterView() {
        springSystem = SpringSystem.create();
        springFilterListener = new FilterSpringListener();
        springFilterView = springSystem.createSpring();
        springFilterView.setSpringConfig(FILTER_VIEW_BOUNCE_SPRING_CONFIG);
        springFilterView.addListener(springFilterListener);
        springFilterView.setEndValue(screenUtils.getHeightPx());

        viewFilter.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewFilter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                showFilterView();
                viewFilter.setVisibility(View.VISIBLE);
            }
        });

        subscriptions.add(viewFilter.onCloseClick().subscribe(aVoid -> {
            hideFilterView();
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

    private void initPresenter() {
        this.homeGridPresenter.attachView(this);
    }

    @Override
    public void onDeepLink(String url) {
        if (!StringUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);

            if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
                if (uri.getPath().startsWith("/u/")) {
                    // TODO USER INVITE
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
            viewFilter.updateFilterList(recipientList.subList(1, recipientList.size() - 1));

            if (tutorialManager.shouldDisplay(TutorialManager.MESSAGES_SUPPORT)) {
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
                                    View view = layoutManager.findViewByPosition(1);
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
    public void onMembershipCreated(Membership membership) {
        // TODO RELOAD GRID
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
        homeGridPresenter.deleteTribe(recipient.getTribe());
        homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
        homeGridAdapter.notifyItemChanged(recipient.getPosition());
    }

    private void setupBottomSheetMore(Recipient recipient) {
        if (dismissDialogSheetMore()) {
            return;
        }

        List<LabelType> moreTypes = new ArrayList<>();
        moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_clear_tribes), MoreType.CLEAR_MESSAGES));

        if (recipient instanceof Friendship) {
            moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_hide, recipient.getDisplayName()), MoreType.HIDE));
            moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_block, recipient.getDisplayName()), MoreType.BLOCK_HIDE));
        }

        if (recipient instanceof Membership) {
            Membership membership = (Membership) recipient;
            moreTypes.add(new MoreType(getString(R.string.grid_menu_group_infos), MoreType.GROUP_INFO));
            if (membership.isAdmin())moreTypes.add(new MoreType(getString(R.string.grid_menu_group_delete), MoreType.GROUP_DELETE));
            else  moreTypes.add(new MoreType(getString(R.string.grid_menu_group_leave), MoreType.GROUP_LEAVE));
        }

        prepareBottomSheetMore(recipient, moreTypes);
    }

    private boolean dismissDialogSheetMore() {
        if (dialogMore != null && dialogMore.isShowing()) {
            dialogMore.dismiss();
            return true;
        }

        return false;
    }

    private void prepareBottomSheetMore(Recipient recipient, List<LabelType> items) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_more, null);
        recyclerViewMore = (RecyclerView) view.findViewById(R.id.recyclerViewMore);
        recyclerViewMore.setHasFixedSize(true);
        recyclerViewMore.setLayoutManager(new LinearLayoutManager(this));
        moreSheetAdapter = new LabelSheetAdapter(context(), items);
        moreSheetAdapter.setHasStableIds(true);
        recyclerViewMore.setAdapter(moreSheetAdapter);
        subscriptions.add(moreSheetAdapter.clickLabelItem()
                .map(labelView -> moreSheetAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    MoreType moreType = (MoreType) labelType;
                    if (moreType.getMoreType().equals(MoreType.CLEAR_MESSAGES)) {
                        homeGridPresenter.markTribeListAsRead(recipient, recipient.getReceivedTribes());
                    } else if (moreType.getMoreType().equals(MoreType.HIDE) || moreType.getMoreType().equals(MoreType.BLOCK_HIDE)) {
                        tagManager.trackEvent(TagManagerConstants.USER_TILE_HIDDEN);
                        homeGridPresenter.updateFriendship((Friendship) recipient, moreType.getMoreType().equals(MoreType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED : FriendshipRealm.HIDDEN);
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_INFO)) {
                        Membership membership = (Membership) recipient;
                        navigator.navigateToGroupInfo(this, membership.getId(), membership.isAdmin(), membership.getGroup().getId(), membership.getGroup().getName(), membership.getGroup().getPicture(), membership.getLink(), membership.getLink_expires_at());
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_LEAVE)) {
                        homeGridPresenter.leaveGroup(recipient.getId());
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_DELETE)) {
                        homeGridPresenter.removeGroup(recipient.getSubId());
                    }

                    dismissDialogSheetMore();
                }));

        dialogMore = new BottomSheetDialog(this);
        dialogMore.setContentView(view);
        dialogMore.show();
        dialogMore.setOnDismissListener(dialog -> {
            moreSheetAdapter.releaseSubscriptions();
            dialogMore = null;
        });
    }

    private void setupBottomSheetPendingTribes(Recipient ... recipientList) {
        if (dismissDialogSheetPendingTribes()) {
            return;
        }

        List<LabelType> pendingTypes = new ArrayList<>();

        for (Recipient recipient : recipientList) {
            if (recipient.getErrorTribes() != null && recipient.getErrorTribes().size() > 0)
                pendingTypes.addAll(recipient.createPendingTribeItems(this, recipientList.length > 1));
        }

        pendingTypes.add(new PendingType(new ArrayList<>(pendingTribes),
                getString(R.string.grid_unsent_tribes_action_resend_all),
                PendingType.RESEND));

        pendingTypes.add(new PendingType(new ArrayList<>(pendingTribes),
                getString(R.string.grid_unsent_tribes_action_delete_all),
                PendingType.DELETE));

        prepareBottomSheetPendingWithList(pendingTypes, recipientList.length > 1);
    }

    private void prepareBottomSheetPendingWithList(List<LabelType> items, boolean isGlobal) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_user_pending, null);
        recyclerViewPending = (RecyclerView) view.findViewById(R.id.recyclerViewPending);
        recyclerViewPending.setHasFixedSize(true);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(this));
        labelSheetAdapter = new LabelSheetAdapter(context(), items);
        labelSheetAdapter.setHasStableIds(true);
        recyclerViewPending.setAdapter(labelSheetAdapter);
        subscriptions.add(labelSheetAdapter.clickLabelItem()
                .map(pendingTribeView -> labelSheetAdapter.getItemAtPosition((Integer) pendingTribeView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
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

                    dismissDialogSheetPendingTribes();
                }));

        bottomSheetPendingTribeDialog = new BottomSheetDialog(this);
        bottomSheetPendingTribeDialog.setContentView(view);
        bottomSheetPendingTribeDialog.show();
        bottomSheetPendingTribeDialog.setOnDismissListener(dialog -> {
            labelSheetAdapter.releaseSubscriptions();
            bottomSheetPendingTribeDialog = null;
        });
    }

    private boolean dismissDialogSheetPendingTribes() {
        if (bottomSheetPendingTribeDialog != null && bottomSheetPendingTribeDialog.isShowing()) {
            bottomSheetPendingTribeDialog.dismiss();
            return true;
        }

        return false;
    }

    /**
     * Loads all friends / tribes.
     */
    private void loadData() {
        subscriptions.add(Observable.timer(100, TimeUnit.MILLISECONDS).onBackpressureDrop().observeOn(AndroidSchedulers.mainThread()).subscribe(t ->  {
            this.homeGridPresenter.onCreate();
        }));
    }

    private void navigateToSettings() {
        HomeActivity.this.navigator.navigateToSettings(HomeActivity.this, SETTINGS_RESULT);
    }

    private void navigateToTribes(Recipient recipient) {
        HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, recipient.getPosition(), recipient, TRIBES_RESULT);
    }

    private void navigateToChat(Recipient recipient) {
        HomeActivity.this.navigator.navigateToChat(HomeActivity.this, recipient.getSubId(), recipient instanceof Membership);
    }

    @Override
    public void setCurrentTribe(TribeMessage tribe) {

    }

    @OnClick(R.id.imgFilterView)
    void openFilterView() {
        isFilterMode = true;
        springFilterView.setEndValue(0f);
        recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
    }

    private void hideFilterView() {
        isFilterMode = false;
        springFilterView.setEndValue(viewFilter.getHeight());
    }

    private void showFilterView() {
        springFilterView.setEndValue(0f);
    }

    private class FilterSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            int value = (int) spring.getCurrentValue();
            viewFilter.setTranslationY(value);
        }
    }

//    private class FilterSpringListener extends SimpleSpringListener {
//        @Override
//        public void onSpringUpdate(Spring spring) {
//            int value = (int) spring.getCurrentValue();
//            viewFilter.setTranslationY(value);
//        }
//    }
}