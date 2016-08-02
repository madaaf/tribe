package com.tribe.app.presentation.view.activity;

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
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomePresenter;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.view.fragment.FriendsGridFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.TextViewFont;

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

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeView {

    private static final int THRESHOLD_SCROLL = 12;
    private static final int DURATION = 500;
    private static final int DELAY_DISMISS_PENDING = 1000;
    private static final int DELAY_DISMISS_NEW_TRIBES = 1000;
    private static final float OVERSHOOT = 1f;

    public static final int FRIENDS_FRAGMENT_PAGE = 0;
    public static final int GRID_FRAGMENT_PAGE = 1;
    public static final int GROUPS_FRAGMENT_PAGE = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Inject
    HomePresenter homePresenter;

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

    @BindView(R.id.layoutNavNewTribes)
    ViewGroup layoutNavNewTribes;

    @BindView(R.id.txtNewTribes)
    TextViewFont txtNewTribes;

    @BindView(R.id.progressBarNewTribes)
    CircularProgressView progressBarNewTribes;

    @BindView(R.id.layoutNavPending)
    ViewGroup layoutNavPending;

    @BindView(R.id.txtPending)
    TextViewFont txtPending;

    @BindView(R.id.progressBar)
    CircularProgressView progressBar;

    @BindView(R.id.imgNavBackToTop)
    ImageView imgNavBackToTop;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private List<Tribe> newTribes;
    private int pendingTribeCount;

    // DIMEN
    private int sizeNavMax, sizeNavSmall, marginHorizontalSmall, translationBackToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initDimensions();
        initViewPager();
        initCamera();
        initDependencyInjector();
        initPresenter();
        initRegistrationToken();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraWrapper.onResume();
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
        cameraWrapper.initDimens(
                getResources().getDimensionPixelSize(R.dimen.vertical_margin_small),
                getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small),
                getResources().getDimensionPixelSize(R.dimen.nav_layout_height)
        );

        subscriptions.add(cameraWrapper.tribeMode().subscribe(mode -> {
            if (homeViewPagerAdapter.getHomeGridFragment() != null) {
                homeViewPagerAdapter.getHomeGridFragment().setTribeMode(mode);
            }
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
    public void initOpenTribes(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, friend.getPosition(), friend);
        }));
    }

    @Override
    public void initClicksOnChat(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToChat(HomeActivity.this, friend.getId());
        }));
    }

    @Override
    public void initOnRecordStart(Observable<String> observable) {
        subscriptions.add(observable.subscribe(id -> {
            cameraWrapper.onStartRecord(id);
        }));
    }

    @Override
    public void initOnRecordEnd(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            cameraWrapper.onEndRecord(friend.getId());
        }));
    }

    @Override
    public void initScrollOnGrid(Observable<Integer> observable) {
        subscriptions.add(observable.subscribe(dy -> {
            if (homeViewPagerAdapter.getHomeGridFragment() != null) {
                if (homeViewPagerAdapter.getHomeGridFragment().getNbItems() > THRESHOLD_SCROLL) {
                    float percent = (float) dy / translationBackToTop;

                    if (dy <= translationBackToTop) {
                        imgNavFriends.setTranslationY(dy);
                        layoutNavGrid.setTranslationY(dy);
                        imgNavGrid.setTranslationY(dy);
                        imgNavFriends.setAlpha(1 - percent);
                        layoutNavGrid.setAlpha(1 - percent);
                        imgNavGrid.setAlpha(1 - percent);
                        imgNavBackToTop.setTranslationY(translationBackToTop - dy);
                        imgNavBackToTop.setAlpha((float) dy / translationBackToTop);
                    } else if (imgNavBackToTop.getTranslationY() != 0) {
                        imgNavBackToTop.setTranslationY(0);
                        imgNavBackToTop.setAlpha(1f);
                    }
                }
            }
        }));
    }

    @Override
    public void initNewTribes(Observable<List<Tribe>> observable) {
        subscriptions.add(observable.subscribe(newTribes -> {
            if (newTribes.size() > 0) {
                txtNewTribes.setText("" + newTribes);
                showLayoutNewTribes();
            } else {
                txtNewTribes.setText("");
                hideLayoutNewTribes();
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
    public void initPendingTribeItemSelected(Observable<List<Tribe>> observable) {
        subscriptions.add(observable.subscribe(tribeList -> {
            pendingTribeCount -= tribeList.size();
            updatePendingTribeCount();
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

    @OnClick(R.id.imgNavGrid)
    public void goToGrid() {
        viewPager.setCurrentItem(GRID_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.imgNavFriends)
    public void goToFriends() {
        viewPager.setCurrentItem(FRIENDS_FRAGMENT_PAGE, true);
    }

    @OnClick(R.id.imgNavBackToTop)
    public void goBackToTop() {
        homeViewPagerAdapter.getHomeGridFragment().scrollToTop();
    }

    @OnClick(R.id.layoutNavPending)
    public void sendPendingMessages() {
        homeViewPagerAdapter.getHomeGridFragment().showPendingTribesMenu();
    }

    @OnClick(R.id.layoutNavNewTribes)
    public void updateGrid() {
        AnimationUtils.replaceView(this, txtNewTribes, progressBarNewTribes, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                subscriptions.add(Observable.timer(DELAY_DISMISS_NEW_TRIBES, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(time -> {
                            hideLayoutNewTribes();
                        }));
            }
        });
    }

    @Override
    public UserComponent getComponent() {
        return userComponent;
    }

    public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

        public String[] pagers = new String[] {"Discover", "Home", "Media"};
        public HomeGridFragment homeGridFragment;
        public FriendsGridFragment friendsGridFragment;
        public GroupsGridFragment groupsGridFragment;

        public HomeViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return new FriendsGridFragment();
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
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    friendsGridFragment = (FriendsGridFragment) createdFragment;
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

    private void hideLayoutNewTribes() {
        if (layoutNavNewTribes.getVisibility() == View.VISIBLE) {
            layoutNavNewTribes.animate().alpha(0).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutNavNewTribes.setVisibility(View.GONE);
                    layoutNavNewTribes.setAlpha(1);
                }
            }).start();
        }
    }

    private void showLayoutNewTribes() {
        if (layoutNavNewTribes.getVisibility() == View.GONE) {
            layoutNavGrid.animate().translationY(translationBackToTop).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutNavGrid.animate().translationY(0).setDuration(DURATION)
                            .setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
                    layoutNavNewTribes.setVisibility(View.VISIBLE);
                }
            }).start();
        }
    }
}