package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.internal.di.HasComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.view.fragment.FriendsGridFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeView {

    private static final int THRESHOLD_SCROLL = 12;

    public static final int FRIENDS_FRAGMENT_PAGE = 0;
    public static final int GRID_FRAGMENT_PAGE = 1;
    public static final int GROUPS_FRAGMENT_PAGE = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

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

    @BindView(R.id.imgNavBackToTop)
    ImageView imgNavBackToTop;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private HomeViewPagerAdapter homeViewPagerAdapter;

    // DIMEN
    private int sizeNavMax, sizeNavSmall, marginHorizontalSmall, translationBackToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initDimensions();
        initializeCamera();
        initializeViewPager();
        initializeDependencyInjector();
        initializePresenter();
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

    private void initializeCamera() {
        cameraWrapper.initDimens(
                getResources().getDimensionPixelSize(R.dimen.vertical_margin_small),
                getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small),
                getResources().getDimensionPixelSize(R.dimen.nav_layout_height)
        );
    }

    private void initializeViewPager() {
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homeViewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(1);
        viewPager.setPageTransformer(false, new HomePageTransformer());
    }

    private void initializeDependencyInjector() {
        this.userComponent = DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    private void initializePresenter() {

    }

    @Override
    public void initClicksOnChat(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            HomeActivity.this.navigator.navigateToTribe(HomeActivity.this, friend.getPosition(), friend.getId());
            //HomeActivity.this.navigator.navigateToChat(HomeActivity.this, friend.getId());
        }));
    }

    @Override
    public void initOnRecordStart(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            cameraWrapper.onStartRecord();
        }));
    }

    @Override
    public void initOnRecordEnd(Observable<Friendship> observable) {
        subscriptions.add(observable.subscribe(friend -> {
            cameraWrapper.onEndRecord();
        }));
    }

    @Override
    public void initScrollOnGrid(Observable<Integer> observable) {
        subscriptions.add(observable.subscribe(dy -> {
            if (homeViewPagerAdapter.getCurrentFragment() != null && homeViewPagerAdapter.getCurrentFragment() instanceof HomeGridFragment) {
                HomeGridFragment fragment = (HomeGridFragment) homeViewPagerAdapter.getCurrentFragment();

                if (fragment.getNbItems() > THRESHOLD_SCROLL) {
                    float percent = (float) dy / translationBackToTop;

                    if (dy <= translationBackToTop) {
                        imgNavFriends.setTranslationY(dy);
                        imgNavGroups.setTranslationY(dy);
                        imgNavGrid.setTranslationY(dy);
                        imgNavFriends.setAlpha(1 - percent);
                        imgNavGroups.setAlpha(1 - percent);
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
    public void onBackPressed() {
        super.onBackPressed();
        // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
        //  and transparent floating activity and auto finishes)
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
        ((HomeGridView) homeViewPagerAdapter.getCurrentFragment()).scrollToTop();
    }

    @Override
    public UserComponent getComponent() {
        return userComponent;
    }

    public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

        public String[] pagers = new String[] {"Discover", "Home", "Media"};
        public Fragment currentFragment;

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
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (currentFragment != object) {
                currentFragment = (Fragment) object;
            }

            super.setPrimaryItem(container, position, object);
        }

        public Fragment getCurrentFragment() {
            return currentFragment;
        }
    }

    public class HomePageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {
            int pagePosition = (int) page.getTag();
            int pageWidth = page.getWidth();

            if (pagePosition == 1 && position > 0) {
                cameraWrapper.setTranslationX(pageWidth * position);

                float sizeImgNavFriends = sizeNavSmall + ((sizeNavMax - sizeNavSmall) * position);
                float translationImgNavFriends = ((pageWidth >> 1) - marginHorizontalSmall - (imgNavFriends.getWidth() / 2)) * position;
                layoutNav(imgNavFriends, sizeImgNavFriends, translationImgNavFriends);

                float sizeImgNavGrid = sizeNavMax - ((sizeNavMax - sizeNavSmall) * position);
                float translationImgNavGrid = ((pageWidth >> 1) - (imgNavGrid.getWidth() / 2) - 2 * marginHorizontalSmall - imgNavGroups.getWidth()) * position;
                layoutNav(imgNavGrid, sizeImgNavGrid, translationImgNavGrid);
            } else if (pagePosition == 1 && position < 0) {
                cameraWrapper.setTranslationX(pageWidth * position);

                float sizeImgNavGroups = sizeNavSmall + ((sizeNavMax - sizeNavSmall) * -position);
                float translationImgNavGroups = ((pageWidth >> 1) - marginHorizontalSmall - (imgNavGroups.getWidth() / 2)) * position;
                layoutNav(imgNavGroups, sizeImgNavGroups, translationImgNavGroups);

                float sizeImgNavGrid = sizeNavMax - ((sizeNavMax - sizeNavSmall) * -position);
                float translationImgNavGrid = ((pageWidth >> 1) - (imgNavGrid.getWidth() / 2) - 2 * marginHorizontalSmall - imgNavFriends.getWidth()) * position;
                layoutNav(imgNavGrid, sizeImgNavGrid, translationImgNavGrid);
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
}