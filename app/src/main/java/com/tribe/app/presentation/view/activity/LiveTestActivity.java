package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.adapter.LiveGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.LiveLayoutManager;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class LiveTestActivity extends BaseActivity {

    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String PICTURE = "PICTURE";
    public static final String IS_GROUP = "IS_GROUP";

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, LiveTestActivity.class);
        intent.putExtra(ID, recipient.getId());
        intent.putExtra(NAME, recipient.getDisplayName());
        intent.putExtra(PICTURE, recipient.getProfilePicture());
        intent.putExtra(IS_GROUP, recipient instanceof Membership);
        return intent;
    }

    @Inject
    ScreenUtils screenUtils;

    @Inject
    LiveGridAdapter liveGridAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    // VARIABLES
    private Unbinder unbinder;
    private String id;
    private String name;
    private String picture;
    private boolean isGroup;
    private LiveLayoutManager layoutManager;
    private List<UserLive> userLiveList;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_test);

        unbinder = ButterKnife.bind(this);

        initParams();
        initDependencyInjector();
        init();
        initResources();
        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void initParams() {
        if (getIntent().hasExtra(ID)) {
            id = getIntent().getStringExtra(ID);
            name = getIntent().getStringExtra(NAME);
            picture = getIntent().getStringExtra(PICTURE);
            isGroup = getIntent().getBooleanExtra(IS_GROUP, false);
        }
    }

    private void init() {
        if (isGroup) {
            txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
        } else {
            txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        txtName.setText(name);
    }

    private void initRecyclerView() {
        userLiveList = new ArrayList<>();
        liveGridAdapter.setScreenHeight(getScreenHeight());
        layoutManager = new LiveLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        userLiveList.add(new UserLive(new User("0"), new View(this)));
        liveGridAdapter.setItems(userLiveList);

        recyclerView.setAdapter(liveGridAdapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 1);
        recyclerView.requestDisallowInterceptTouchEvent(true);
    }

    private void initResources() {

    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private int getScreenHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        return screenUtils.getHeightPx() - result;
    }

    @OnClick(R.id.imgAdd)
    void onAdd() {
        int position = liveGridAdapter.getItemCount();

        if (position < 8) {
            UserLive userLive = new UserLive(new User(String.valueOf(position)), new View(this));
            refactorPositionInGrid(position, userLive);
            liveGridAdapter.setItems(userLiveList);
        }
    }

    private void refactorPositionInGrid(int position, UserLive userLive) {
        if (position == 3 || position == 5 || position == 7) { // The 4th and the 6th live feed go right next to the 0
            userLiveList.add(1, userLive);
        } else if (position == 4 || position == 6) { // The 5th and 7th live feed go right below to the 0
            userLiveList.add(0, userLive);
        } else {
            userLiveList.add(position, userLive);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}