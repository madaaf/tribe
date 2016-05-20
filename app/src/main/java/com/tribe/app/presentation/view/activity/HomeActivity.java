package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.HasComponent;
import com.tribe.app.presentation.internal.di.components.DaggerAvengersComponent;
import com.tribe.app.presentation.internal.di.components.DaggerFriendshipComponent;
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.CharacterListView;
import com.tribe.app.presentation.view.adapter.AvengersListAdapter;
import com.tribe.app.presentation.view.fragment.DiscoverGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.fragment.MediaGridFragment;
import com.tribe.app.presentation.view.widget.RecyclerInsetsDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity implements HasComponent<FriendshipComponent> {

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