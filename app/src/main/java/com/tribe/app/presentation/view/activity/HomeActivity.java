package com.tribe.app.presentation.view.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomePresenter;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.view.fragment.ContactsGridFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeView {

    private static final String[] PERMISSIONS_CAMERA = new String[]{ Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private static final int THRESHOLD_SCROLL = 12;
    private static final int DURATION = 500;
    private static final int DELAY_DISMISS_PENDING = 1000;
    private static final int DELAY_DISMISS_NEW_TRIBES = 1000;
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

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

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

    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private List<Message> newMessages;
    private int pendingTribeCount;
    private final int SETTINGS_RESULT = 101;

    // DIMEN
    private int sizeNavMax, sizeNavSmall, marginHorizontalSmall, translationBackToTop;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        subscriptions.add(Observable.
                from(PERMISSIONS_CAMERA)
                .map(permission -> RxPermissions.getInstance(HomeActivity.this)
                        .isGranted(permission))
                .toList()
            .subscribe(grantedList -> {
                boolean areAllGranted = true;

                for (Boolean granted : grantedList) {
                    if (!granted) areAllGranted = false;
                }

                if (areAllGranted) cameraWrapper.onResume(false);
                else cameraWrapper.showPermissions();
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraWrapper.onPause();
    }

    @Override
    protected void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        homePresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_RESULT) reloadGrid();
    }

    private void initUi() {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    private void initDimensions() {
        sizeNavMax = getResources().getDimensionPixelSize(R.dimen.nav_size_max);
        sizeNavSmall = getResources().getDimensionPixelSize(R.dimen.nav_size_small);
        marginHorizontalSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        translationBackToTop = getResources().getDimensionPixelSize(R.dimen.transition_grid_back_to_top);
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

        subscriptions.add(cameraWrapper.tribeMode().subscribe(mode -> {
            if (homeViewPagerAdapter.getHomeGridFragment() != null) {
                homeViewPagerAdapter.getHomeGridFragment().setTribeMode(mode);
            }
        }));

        subscriptions.add(cameraWrapper.cameraPermissions().subscribe(aVoid -> {
            RxPermissions.getInstance(this)
                    .request(PERMISSIONS_CAMERA)
                    .subscribe(granted -> {
                        if (granted) cameraWrapper.onResume(true);
                        else cameraWrapper.showPermissions();
                    });
        }));
    }

    private void initViewPager() {
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homeViewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(1);
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
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, friend.getPosition(), friend);
        }));
    }

    @Override
    public void initClicksOnChat(Observable<Recipient> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToChat(HomeActivity.this, friend);
        }));
    }

    @Override
    public void initOnRecordStart(Observable<String> observable) {
        subscriptions.add(observable.subscribe(id -> {
            cameraWrapper.onStartRecord(id);
        }));
    }

    @Override
    public void initOnRecordEnd(Observable<Recipient> observable) {
        subscriptions.add(observable.subscribe(friend -> {
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

    private void initRegistrationToken() {
        String token = FirebaseInstanceId.getInstance().getToken();

        if (token != null) homePresenter.sendToken(token);
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

    @OnClick(R.id.imgNavGroups)
    public void goToGroups() {
        viewPager.setCurrentItem(GROUPS_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.imgNavFriends)
    public void goToFriends() {
        viewPager.setCurrentItem(CONTACTS_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.layoutNavPending)
    public void sendPendingMessages() {
        homeViewPagerAdapter.getHomeGridFragment().showPendingTribesMenu();
    }

    @OnClick(R.id.layoutNavNewMessages)
    public void updateGrid() {
        homePresenter.updateMessagesToNotSeen(newMessages);
        homeViewPagerAdapter.getHomeGridFragment().reloadGrid();
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

    @OnClick(R.id.layoutNavGridMain)
    public void reloadGrid() {
        if (viewPager.getCurrentItem() == GRID_FRAGMENT_PAGE) {
            homePresenter.reloadData();
            homeViewPagerAdapter.getHomeGridFragment().scrollToTop();
        } else {
            viewPager.setCurrentItem(GRID_FRAGMENT_PAGE, true);
        }
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

    public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

        public String[] pagers = new String[] {"Discover", "Home", "Media"};
        public HomeGridFragment homeGridFragment;
        public ContactsGridFragment contactsGridFragment;
        public GroupsGridFragment groupsGridFragment;

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
                    contactsGridFragment = (ContactsGridFragment) createdFragment;
                    break;
                case 1:
                    homeGridFragment = (HomeGridFragment) createdFragment;
                    break;
                case 2:
                    groupsGridFragment = (GroupsGridFragment) createdFragment;
            }

            return createdFragment;
        }

        public HomeGridFragment getHomeGridFragment() {
            return homeGridFragment;
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

    // ANIMATIONS
    private void hideLayoutPending() {
        if (layoutNavPending.getTranslationY() == 0) {
            layoutNavPending.animate().translationY(translationBackToTop).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void showLayoutPending() {
        if (layoutNavPending.getTranslationY() > 0) {
            layoutNavPending.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void hideLayoutNewMessages() {
        if (layoutNavNewMessages.getTranslationY() == 0) {
            System.out.println("ANIMATING HIDE");
            layoutNavNewMessages.animate().translationY(translationBackToTop).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    AnimationUtils.replaceView(HomeActivity.this, progressBarNewMessages, txtNewMessages, null);
                    layoutNavNewMessages.animate().setListener(null).start();

                    System.out.println("END ANIMATING HIDE");
                }
            });
            layoutNavGridMain.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void showLayoutNewTribes() {
        if (layoutNavNewMessages.getTranslationY() > 0) {
            System.out.println("ANIMATING SHOW");
            layoutNavNewMessages.setTranslationY(translationBackToTop);
            layoutNavGridMain.animate().translationY(translationBackToTop).setDuration(DURATION).start();
            layoutNavNewMessages.animate().translationY(0).setDuration(DURATION).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }
}