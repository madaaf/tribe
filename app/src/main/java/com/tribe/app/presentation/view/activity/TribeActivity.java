package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.view.adapter.pager.TribePagerAdapter;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.component.TribeViewPager;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeView {

    public static final String FRIEND_ID = "FRIEND_ID";

    public static Intent getCallingIntent(Context context, String friendId) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(FRIEND_ID, friendId);
        return intent;
    }

    @Inject
    TribePresenter tribePresenter;

    @Inject
    TribePagerAdapter tribePagerAdapter;

    @BindView(R.id.viewPager)
    TribeViewPager viewPager;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // VARIABLES
    private int previousPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initializeDependencyInjector();
        initViewPager();
        initializeSubscriptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePresenter();
    }

    @Override
    protected void onStop() {
        tribePresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initParams() {

    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initViewPager() {
        tribePagerAdapter = new TribePagerAdapter(this);
        viewPager.setAdapter(tribePagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setScrollDurationFactor(1.0f);
        viewPager.setCurrentItem(0);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_ALL);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int currentPosition) {
                if (currentPosition == tribePagerAdapter.getCount() - 1) viewPager.setBackground(null);
                tribePagerAdapter.setCurrentPosition(currentPosition);
                tribePagerAdapter.releaseTribe(currentPosition, (TribeComponentView) viewPager.findViewWithTag(previousPosition));
                tribePagerAdapter.startTribe(currentPosition, (TribeComponentView) viewPager.findViewWithTag(currentPosition));
                previousPosition = currentPosition;
            }
        });

        List<Tribe> tribeList = new ArrayList<>();
        tribeList.add(new Tribe("0"));
        tribeList.add(new Tribe("1"));
        tribeList.add(new Tribe("2"));
        tribeList.add(new Tribe("3"));
        tribeList.add(new Tribe("4"));
        tribePagerAdapter.setItems(tribeList);
        viewPager.setCount(tribeList.size());
    }

    private void initializeSubscriptions() {
        subscriptions = new CompositeSubscription();

        subscriptions.add(viewPager.onDismissHorizontal().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));
        subscriptions.add(viewPager.onDismissVertical().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));
    }

    private void initializeDependencyInjector() {
        DaggerTribeComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initializePresenter() {
        tribePresenter.onStart();
        tribePresenter.attachView(this);
    }
}