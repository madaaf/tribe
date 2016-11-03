package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.DownloadTribeService;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.internal.di.scope.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.internal.di.scope.WasAskedForCameraPermission;
import com.tribe.app.presentation.mvp.presenter.HomePresenter;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.utils.DeepLinkUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.fragment.ContactsGridFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.tutorial.Overlay;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeView, GoogleApiClient.OnConnectionFailedListener {

    public static final int SETTINGS_RESULT = 101, TRIBES_RESULT = 104;

    private static final int DURATION = 500;
    private static final int DURATION_SMALL = 300;
    private static final float OVERSHOOT = 1f;

    public static final int CONTACTS_FRAGMENT_PAGE = 0;
    public static final int GRID_FRAGMENT_PAGE = 1;
    public static final int GROUPS_FRAGMENT_PAGE = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Inject
    HomePresenter homePresenter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    TutorialManager tutorialManager;

    @Inject
    @HasReceivedPointsForCameraPermission
    Preference<Boolean> hasReceivedPointsForCameraPermission;

    @Inject
    @WasAskedForCameraPermission
    Preference<Boolean> wasAskedForCameraPermission;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.layoutNavMaster)
    View layoutNavMaster;

    @BindView(R.id.imgNavGrid)
    ImageView imgNavGrid;

    @BindView(R.id.imgNavFriends)
    ImageView imgNavFriends;

    @BindView(R.id.imgNavGroups)
    ImageView imgNavGroups;

    @BindView(R.id.layoutNavGrid)
    ViewGroup layoutNavGrid;

    @BindView(R.id.layoutNavGridMain)
    ViewGroup layoutNavGridMain;

    @BindView(R.id.layoutNavNewMessages)
    ViewGroup layoutNavNewMessages;

    @BindView(R.id.txtNewMessages)
    TextViewFont txtNewMessages;

    @BindView(R.id.progressBarNewMessages)
    CircularProgressView progressBarNewMessages;

    @BindView(R.id.progressBarReload)
    CircularProgressView progressBarReload;

    @BindView(R.id.layoutNavPending)
    ViewGroup layoutNavPending;

    @BindView(R.id.txtPending)
    TextViewFont txtPending;

    @BindView(R.id.progressBar)
    CircularProgressView progressBar;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    // OBSERVABLES
    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private Context context;
    private int previousViewPagerState;
    private int viewPagerAnimDuration = AnimationUtils.ANIMATION_DURATION_EXTRA_SHORT;
    private List<Message> newMessages;
    private int pendingTribeCount;
    private boolean isRecording;
    private boolean navVisible = true;
    private boolean layoutNavPendingVisible = false;
    private Tutorial tutorial;

    // DIMEN
    private int sizeNavMax, sizeNavSmall, marginHorizontalSmall, translationBackToTop;
    private int tapToRefreshTutorialSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        initUi();
        initDimensions();
        initViewPager();
        initCamera();
        initPresenter();
        initRegistrationToken();
        manageDeepLink(getIntent());

        subscriptions.add(
                Observable.timer(3000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            startService(DownloadTribeService.getCallingIntent(this, null));

                            if (tutorialManager.shouldDisplay(TutorialManager.REFRESH)) {
                                layoutNavGridMain.setDrawingCacheEnabled(true);
                                layoutNavGridMain.buildDrawingCache();
                                Bitmap bitmapForTutorialOverlay = Bitmap.createBitmap(layoutNavGridMain.getDrawingCache(true));
                                layoutNavGridMain.setDrawingCacheEnabled(false);

                                tutorial = tutorialManager.showRefresh(
                                        this,
                                        imgNavGrid,
                                        Overlay.NOT_SET,
                                        bitmapForTutorialOverlay,
                                        sizeNavMax,
                                        v -> {
                                            tutorial.cleanUp();
                                            tutorial = null;
                                        }
                                );
                            }
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

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(context, DownloadTribeService.class);
        context.stopService(i);

        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        homePresenter.onDestroy();

        super.onDestroy();
    }

    private void initUi() {
        context = this;
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    private void initDimensions() {
        sizeNavMax = getResources().getDimensionPixelSize(R.dimen.nav_size_max);
        sizeNavSmall = getResources().getDimensionPixelSize(R.dimen.nav_size_small);
        marginHorizontalSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        translationBackToTop = getResources().getDimensionPixelSize(R.dimen.transition_grid_back_to_top);
        tapToRefreshTutorialSize = 10;
    }

    private void initCamera() {
        int marginBounds = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);

        cameraWrapper.initDimens(
                screenUtils.getHeightPx()
                        - getResources().getDimensionPixelSize(R.dimen.nav_layout_height)
                        - cameraWrapper.getHeightFromRatio(),
                marginBounds,
                getResources().getDimensionPixelSize(R.dimen.nav_layout_height),
                marginBounds,
                marginBounds,
                getResources().getDimensionPixelSize(R.dimen.nav_layout_height),
                true
        );

        subscriptions.add(cameraWrapper.tribeMode().delay(750, TimeUnit.MILLISECONDS).subscribe(mode -> {
            if (homeViewPagerAdapter.getHomeGridFragment() != null) {
                homeViewPagerAdapter.getHomeGridFragment().setTribeMode(mode);
            }
        }));

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
                homePresenter.updateScoreCamera();
            }

            cameraWrapper.onResume(shouldAnimate);
        } else {
            cameraWrapper.showPermissions();
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.CAMERA_ENABLED, isGranted);
        tagManager.setProperty(bundle);
    }

    private void initViewPager() {
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homeViewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollDurationFactor(1f);
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (viewPager.getCurrentItem() == GRID_FRAGMENT_PAGE) {
                    //reloadGrid();
                    if (!navVisible) enableNavigation();
                    screenUtils.hideKeyboard((Activity) context);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    if (viewPager.getCurrentItem() == CONTACTS_FRAGMENT_PAGE) {
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
                        homeViewPagerAdapter.getContactsGridFragment().closeSearch();
                    }
                }
            }
        });
        viewPager.setPageTransformer(false, new HomePageTransformer());
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
        this.homePresenter.attachView(this);
    }

    @Override
    public void initOpenTribes(Observable<Recipient> observable) {
        subscriptions.add(observable
                .subscribe(friend -> {
            HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, friend.getPosition(), friend, TRIBES_RESULT);
        }));
    }

    @Override
    public void initClicksOnChat(Observable<Recipient> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToChat(HomeActivity.this, friend.getSubId(), friend instanceof Membership);
        }));
    }

    @Override
    public void initOnRecordStart(Observable<String> observable) {
        subscriptions.add(observable.subscribe(id -> {
            isRecording = true;
            viewPager.setSwipeable(false);
            cameraWrapper.onStartRecord(id);
        }));
    }

    @Override
    public void initOnRecordEnd(Observable<Recipient> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            isRecording = false;
            viewPager.setSwipeable(true);
            cameraWrapper.onEndRecord();
        }));
    }

    @Override
    public void initScrollOnGrid(Observable<Integer> observable) {
        subscriptions.add(observable.subscribe(dy -> {

        }));
    }

    @Override
    public void initNewMessages(Observable<List<Message>> observable) {
        subscriptions.add(observable.subscribe(newMessages -> {
            boolean shouldUpdateNewMessages = !newMessages.equals(this.newMessages);

            if (shouldUpdateNewMessages) {
                this.newMessages = newMessages;

                if (newMessages.size() > 0) {
                    txtNewMessages.setText("" + newMessages.size());
                    showLayoutNewTribes();
                } else {
                    txtNewMessages.setText("");
                    hideLayoutNewMessages();
                }
            }
        }));
    }

    @Override
    public void initPendingTribes(Observable<Integer> observable) {
        subscriptions.add(observable.subscribe(tribesPending -> {
            pendingTribeCount = tribesPending;
            updatePendingTribeCount();
        }));
    }

    @Override
    public void initPendingTribeItemSelected(Observable<List<TribeMessage>> observable) {
        subscriptions.add(observable.subscribe(tribeList -> {
            pendingTribeCount -= tribeList.size();
            updatePendingTribeCount();
        }));
    }

    @Override
    public void initClickOnPoints(Observable<View> observable) {
        subscriptions.add(observable.subscribe(view -> {
            HomeActivity.this.navigator.navigateToScorePoints(HomeActivity.this);
        }));
    }

    @Override
    public void initClickOnSettings(Observable<View> observable) {
        subscriptions.add(observable.subscribe(view -> {
            HomeActivity.this.navigator.navigateToSettings(HomeActivity.this, SETTINGS_RESULT);
        }));
    }

    @Override
    public void initPullToSearchActive(Observable<Boolean> observable) {
        subscriptions.add(observable.subscribe(active -> {
            animateViewsOnPTSOpen(active);
            viewPager.setSwipeable(!active);
        }));
    }

    @Override
    public void onDeepLink(String url) {
        if (!StringUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);

            if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
                if (uri.getPath().startsWith("/u/") && homeViewPagerAdapter.getContactsGridFragment() != null) {
                    viewPager.setCurrentItem(CONTACTS_FRAGMENT_PAGE, true);
                    homeViewPagerAdapter.getContactsGridFragment().search(StringUtils.getLastBitFromUrl(url));
                } else if (uri.getPath().startsWith("/g/")) {
                    homePresenter.createMembership(StringUtils.getLastBitFromUrl(url));
                }
            }
        }
    }

    @Override
    public void onMembershipCreated(Membership membership) {
        if (homeViewPagerAdapter.getHomeGridFragment() != null) {
            homeViewPagerAdapter.getHomeGridFragment().reloadGrid();
        }
    }

    private void initRegistrationToken() {
        String token = FirebaseInstanceId.getInstance().getToken();

        if (token != null) homePresenter.sendToken(token);
    }

    private void manageDeepLink(Intent intent) {
        if (intent != null) {
            if (intent.getData() != null) {
                homePresenter.getHeadDeepLink(intent.getDataString());
            } else if (!StringUtils.isEmpty(intent.getAction()) && intent.getAction().equals(DeepLinkUtils.MESSAGE_ACTION)) {
                String recipientId = intent.getStringExtra("t_from");
                boolean isToGroup = Boolean.valueOf(intent.getStringExtra("to_group"));
                navigator.navigateToChat(this, recipientId, isToGroup);
            }
        }
    }

    private void updatePendingTribeCount() {
        if (pendingTribeCount > 0) {
            txtPending.setText("" + pendingTribeCount);
            showLayoutPending();
        } else {
            txtPending.setText("");
            hideLayoutPending();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
        // and transparent floating activity and auto finishes)
        startActivity(new Intent(this, DummyActivity.class));
        finish();
    }

    public void goToHome() {
        viewPager.setCurrentItem(GRID_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.imgNavGroups)
    public void goToGroups() {
        animateToGroups();
        if (!isRecording) viewPager.setCurrentItem(GROUPS_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.imgNavFriends)
    public void goToFriends() {
        animateToContacts();
        if (!isRecording) viewPager.setCurrentItem(CONTACTS_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.layoutNavPending)
    public void sendPendingMessages() {
        if (!isRecording) homeViewPagerAdapter.getHomeGridFragment().showPendingTribesMenu();
    }

    @OnClick(R.id.layoutNavNewMessages)
    public void updateGrid() {
        if (!isRecording) {
            homeViewPagerAdapter.getHomeGridFragment().updateNewTribes();
            homeViewPagerAdapter.getHomeGridFragment().scrollToTop();

            AnimationUtils.replaceView(this, txtNewMessages, progressBarNewMessages, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    txtNewMessages.animate().setListener(null).start();
                    progressBarNewMessages.animate().setListener(null).start();
                    hideLayoutNewMessages();
                }
            });
        }
    }

    @OnClick(R.id.layoutNavGridMain)
    public void reloadGrid() {
        animateToGrid();

        if (tutorial != null) {
            tutorial.cleanUp();
            tutorial = null;
            subscriptions.add(
                    Observable
                            .timer(300, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> doReload())
            );
        } else {
            doReload();
        }
    }

    private void doReload() {
        if (!isRecording) {
            if (viewPager.getCurrentItem() == GRID_FRAGMENT_PAGE) {
                homePresenter.reloadData();
                homeViewPagerAdapter.getHomeGridFragment().scrollToTop();
            } else {
                viewPager.setCurrentItem(GRID_FRAGMENT_PAGE, true);
            }
        }
    }

    private void animateToContacts() {
        imgNavGroups.animate()
                .setDuration(viewPagerAnimDuration)
                .x(screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small))
                .start();
        int navGridPosition;
        int navFriendsPosition;
        if (layoutNavPendingVisible) {
            layoutNavPending.animate()
                    .setDuration(viewPagerAnimDuration)
                    .x(screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2)
                    .start();
            navGridPosition = screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 3;
            navFriendsPosition = screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 3 - getResources().getDimensionPixelSize(R.dimen.nav_size_max);
        } else {
            navGridPosition = screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2;
            navFriendsPosition = screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2 - getResources().getDimensionPixelSize(R.dimen.nav_size_max);
        }
        layoutNavGrid.animate()
                .setDuration(viewPagerAnimDuration)
                .x(navGridPosition)
                .start();
        imgNavFriends.animate()
                .setDuration(viewPagerAnimDuration)
                .x(navFriendsPosition)
                .start();
        AnimationUtils.animateSizeFrameLayout(imgNavGroups, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(layoutNavGrid, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(imgNavFriends, getResources().getDimensionPixelSize(R.dimen.nav_size_max), viewPagerAnimDuration);
    }

    private void animateToGrid() {
        imgNavFriends.animate()
                .setDuration(viewPagerAnimDuration)
                .x(getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small))
                .start();
        layoutNavGrid.animate()
                .setDuration(viewPagerAnimDuration)
                .x(screenUtils.getWidthPx() / 2 - layoutNavGrid.getWidth() / 2)
                .start();
        imgNavGroups.animate()
                .setDuration(viewPagerAnimDuration)
                .x(screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small))
                .start();
        if (layoutNavPendingVisible) {
            layoutNavPending.animate()
                    .setDuration(viewPagerAnimDuration)
                    .x(screenUtils.getWidthPx() - getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) - getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2)
                    .start();
        }
        AnimationUtils.animateSizeFrameLayout(imgNavGroups, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(layoutNavGrid, getResources().getDimensionPixelSize(R.dimen.nav_size_max), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(imgNavFriends, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
    }
    private void animateToGroups() {
        imgNavFriends.animate()
                .setDuration(viewPagerAnimDuration)
                .x(getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small))
                .start();
        layoutNavGrid.animate()
                .setDuration(viewPagerAnimDuration)
                .x(getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) + getResources().getDimensionPixelSize(R.dimen.nav_size_small))
                .start();
        int navGroupsPosition;
        if (layoutNavPendingVisible) {
            layoutNavPending.animate()
                    .setDuration(viewPagerAnimDuration)
                    .x(getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) + getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2)
                    .start();
            navGroupsPosition = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) + getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 3;
        } else {
            navGroupsPosition = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small) + getResources().getDimensionPixelSize(R.dimen.nav_size_small) * 2;
        }
        imgNavGroups.animate()
                .setDuration(viewPagerAnimDuration)
                .x(navGroupsPosition)
                .start();
        AnimationUtils.animateSizeFrameLayout(imgNavFriends, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(layoutNavGrid, getResources().getDimensionPixelSize(R.dimen.nav_size_small), viewPagerAnimDuration);
        AnimationUtils.animateSizeFrameLayout(imgNavGroups, getResources().getDimensionPixelSize(R.dimen.nav_size_max), viewPagerAnimDuration);
    }


    @Override
    public UserComponent getComponent() {
        return userComponent;
    }

    @Override
    public void showLoading() {
        imgNavGrid.setVisibility(View.GONE);
        progressBarReload.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        homeViewPagerAdapter.getHomeGridFragment().reloadGrid();
        imgNavGrid.setVisibility(View.VISIBLE);
        progressBarReload.setVisibility(View.GONE);
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
        return null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("TRIBE", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

        public String[] pagers = new String[]{"Discover", "Home", "Media"};
        private WeakReference<HomeGridFragment> homeGridFragment;
        private WeakReference<ContactsGridFragment> contactsGridFragment;
        private WeakReference<GroupsGridFragment> groupsGridFragment;

        public HomeViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return new ContactsGridFragment();
            else if (position == 1) return new HomeGridFragment();
            else return new GroupsGridFragment();
        }

        @Override
        public int getCount() {
            return pagers.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagers[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // Save the appropriate reference depending on position
            switch (position) {
                case 0:
                    contactsGridFragment = new WeakReference<>((ContactsGridFragment) createdFragment);
                    break;
                case 1:
                    homeGridFragment = new WeakReference<>((HomeGridFragment) createdFragment);
                    break;
                case 2:
                    groupsGridFragment = new WeakReference<>((GroupsGridFragment) createdFragment);
                    break;
            }

            return createdFragment;
        }

        public HomeGridFragment getHomeGridFragment() {
            if (homeGridFragment != null && homeGridFragment.get() != null)
                return homeGridFragment.get();

            return null;
        }

        public ContactsGridFragment getContactsGridFragment() {
            return contactsGridFragment.get();
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    public class HomePageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {
            int pagePosition = (int) page.getTag();
            int pageWidth = page.getWidth();
            int marginLayoutNavPending = ((FrameLayout.LayoutParams) layoutNavPending.getLayoutParams()).rightMargin;

            int widthPending = layoutNavPending.getTranslationY() == 0 ? layoutNavPending.getWidth() : 0;
            if (pagePosition == 1 && position > 0) {
                cameraWrapper.setTranslationX(pageWidth * position);

                float sizeLayoutNavPending = sizeNavSmall;
                float translationLayoutNavPending = 0;
                layoutNav(layoutNavPending, sizeLayoutNavPending, translationLayoutNavPending);

                float sizeImgNavGrid = sizeNavMax - ((sizeNavMax - sizeNavSmall) * position);
                float translationImgNavGrid = ((pageWidth >> 1) - (layoutNavGrid.getWidth() / 2) - marginHorizontalSmall - widthPending - imgNavGroups.getWidth()) * position;
                layoutNav(layoutNavGrid, sizeImgNavGrid, translationImgNavGrid);

                float sizeImgNavFriends = sizeNavSmall + ((sizeNavMax - sizeNavSmall) * position);
                float translationImgNavFriends = (pageWidth - imgNavFriends.getWidth() - widthPending - layoutNavGrid.getWidth() - imgNavGroups.getWidth() - 2 * marginHorizontalSmall) * position;
                layoutNav(imgNavFriends, sizeImgNavFriends, translationImgNavFriends);
            } else if (pagePosition == 1 && position < 0) {
                cameraWrapper.setTranslationX(pageWidth * position);

                float sizeImgNavGrid = sizeNavMax - ((sizeNavMax - sizeNavSmall) * -position);
                float translationImgNavGrid = ((pageWidth >> 1) - (layoutNavGrid.getWidth() / 2) - marginHorizontalSmall - imgNavFriends.getWidth()) * position;
                layoutNav(layoutNavGrid, sizeImgNavGrid, translationImgNavGrid);

                float sizeLayoutNavPending = sizeNavSmall;
                float translationLayoutNavPending = (pageWidth - widthPending - marginLayoutNavPending - imgNavFriends.getWidth() - layoutNavGrid.getWidth() - marginHorizontalSmall) * position;
                layoutNav(layoutNavPending, sizeLayoutNavPending, translationLayoutNavPending);

                float sizeImgNavGroups = sizeNavSmall + ((sizeNavMax - sizeNavSmall) * -position);
                float translationImgNavGroups = (pageWidth - imgNavFriends.getWidth() - widthPending - layoutNavGrid.getWidth() - imgNavGroups.getWidth() - 2 * marginHorizontalSmall) * position;
                layoutNav(imgNavGroups, sizeImgNavGroups, translationImgNavGroups);
            }
        }

        public void layoutNav(View v, float size, float translationX) {
            v.setTranslationX(translationX);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.width = (int) size;
            params.height = (int) size;
            v.setLayoutParams(params);
            v.invalidate();
        }
    }

    //////////////////
    //  ANIMATIONS  //
    //////////////////

    private void hideLayoutPending() {
        layoutNavPendingVisible = false;
        if (layoutNavPending.getTranslationY() == 0) {
            layoutNavPending.animate().translationY(translationBackToTop).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void showLayoutPending() {
        layoutNavPendingVisible = true;
        if (layoutNavPending.getTranslationY() > 0) {
            layoutNavPending.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void hideLayoutNewMessages() {
        if (layoutNavNewMessages.getTranslationY() == 0) {
            layoutNavNewMessages.animate().translationY(translationBackToTop).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    AnimationUtils.replaceView(HomeActivity.this, progressBarNewMessages, txtNewMessages, null);
                    layoutNavNewMessages.animate().setListener(null).start();
                }
            });
            layoutNavGridMain.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void showLayoutNewTribes() {
        if (layoutNavNewMessages.getTranslationY() > 0) {
            layoutNavNewMessages.setTranslationY(translationBackToTop);
            layoutNavGridMain.animate().translationY(translationBackToTop).setDuration(DURATION).start();
            layoutNavNewMessages.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void animateViewsOnPTSOpen(boolean active) {
        if (active) {
            disableNavigation();
            cameraWrapper.hideCamera();
        } else {
            enableNavigation();
            cameraWrapper.showCamera();
        }
    }

    private void slideDownNav(View view) {
        view.animate()
                .setDuration(DURATION_SMALL)
                .translationY(translationBackToTop)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void slideUpNav(View view) {
        view.animate()
                .setDuration(DURATION_SMALL)
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public void disableNavigation() {
        slideDownNav(layoutNavMaster);
        viewPager.setSwipeable(false);
        navVisible = false;
    }

    public void enableNavigation() {
        animateToGrid();
        slideUpNav(layoutNavMaster);
        viewPager.setSwipeable(true);
        navVisible = true;
    }
}