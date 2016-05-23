package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.internal.di.HasComponent;
import com.tribe.app.presentation.internal.di.components.DaggerFriendshipComponent;
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.view.fragment.DiscoverGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.fragment.MediaGridFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity implements HasComponent<FriendshipComponent>,
        HomeGridFragment.FriendListListener {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    private FriendshipComponent friendshipComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initializeViewPager();
        initializeDependencyInjector();
        initializePresenter();
    }

    private void initUi() {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    private void initializeViewPager() {
        viewPager.setAdapter(new HomeViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(1);
    }

    private void initializeDependencyInjector() {
        this.friendshipComponent = DaggerFriendshipComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }

    private void initializePresenter() {

    }

    @Override
    public void onTextClicked(MarvelCharacter friend) {
        this.navigator.navigateToChat(this, friend.getId());
    }

    @Override
    public FriendshipComponent getComponent() {
        return friendshipComponent;
    }

    public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

        public String[] pagers = new String[] {"Discover", "Home", "Media"};

        public HomeViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return new DiscoverGridFragment();
            else if (position == 1) return new HomeGridFragment();
            else return new MediaGridFragment();
        }

        @Override
        public int getCount() {
            return pagers.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagers[position];
        }
    }
}