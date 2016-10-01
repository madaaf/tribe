package com.tribe.app.presentation.view.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.ScorePresenter;
import com.tribe.app.presentation.mvp.view.ScoreView;
import com.tribe.app.presentation.view.adapter.LevelAdapter;
import com.tribe.app.presentation.view.adapter.manager.LevelLayoutManager;
import com.tribe.app.presentation.view.decorator.GridDividerTopItemDecoration;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class ScoreActivity extends BaseActivity implements ScoreView {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, ScoreActivity.class);
        return intent;
    }

    private final int DURATION = 500;
    private final float OVERSHOOT = 1.5f;

    @Inject
    ScorePresenter scorePresenter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    User currentUser;

    @BindView(R.id.recyclerViewLevel)
    RecyclerView recyclerViewLevel;

    @BindView(R.id.imgLevel)
    ImageView imgLevel;

    @BindView(R.id.txtLevel)
    TextViewFont txtLevel;

    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    @BindView(R.id.imgLevelSmall)
    ImageView imgLevelSmall;

    @BindView(R.id.viewScoreMax)
    View viewScoreMax;

    @BindView(R.id.viewScoreProgress)
    View viewScoreProgress;

    @BindView(R.id.txtPointsLeft)
    TextViewFont txtPointsLeft;

    // RESOURCES
    private int marginVerticalSmall;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // VARIABLES
    private LevelLayoutManager levelLayoutManager;
    private LevelAdapter levelAdapter;
    private List<ScoreUtils.Level> levelList;
    private ValueAnimator animatorWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDependencyInjector();
        initResources();
        initUi();
        initSubscriptions();
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPresenter();
    }

    @Override
    protected void onStop() {
        scorePresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        animatorWidth.cancel();

        if (unbinder != null) unbinder.unbind();
        if (scorePresenter != null) scorePresenter.onDestroy();
        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
        if (levelAdapter != null) levelAdapter.releaseSubscriptions();

        super.onDestroy();
    }

    private void initResources() {
        marginVerticalSmall = getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);
    }

    private void initUi() {
        setContentView(R.layout.activity_score);
        unbinder = ButterKnife.bind(this);

        ScoreUtils.Level level = ScoreUtils.getLevelForScore(currentUser.getScore());
        ScoreUtils.Level nextLevel = ScoreUtils.getNextLevelForScore(currentUser.getScore());

        txtLevel.setText(level.getStringId());
        txtPoints.setText(getString(R.string.points_suffix, ScoreUtils.formatFloatingPoint(this, currentUser.getScore())));

        imgLevel.setImageResource(level.getDrawableId());
        imgLevelSmall.setImageResource(level.getDrawableId());

        int pointsLeftForNext = ScoreUtils.getRestForNextLevel(currentUser.getScore());
        txtPointsLeft.setText(getString(R.string.points_suffix, ScoreUtils.formatFloatingPoint(this, pointsLeftForNext)));

        int progressHeight = (int) (((float) screenUtils.getWidthPx() / nextLevel.getPoints()) * currentUser.getScore());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewScoreProgress.getLayoutParams();
        animatorWidth = ValueAnimator.ofInt(0, progressHeight);
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorWidth.setStartDelay(1000);
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            viewScoreProgress.setLayoutParams(params);
        });
        animatorWidth.start();
    }

    private void initRecyclerView() {
        levelLayoutManager = new LevelLayoutManager(this);
        recyclerViewLevel.setLayoutManager(levelLayoutManager);
        levelAdapter = new LevelAdapter(this, currentUser.getScore());
        levelAdapter.setItems(Arrays.asList(ScoreUtils.Level.values()));
        recyclerViewLevel.setAdapter(levelAdapter);
        recyclerViewLevel.setHasFixedSize(true);
        recyclerViewLevel.addItemDecoration(new GridDividerTopItemDecoration(marginVerticalSmall, LevelLayoutManager.spanCount));
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initPresenter() {
        scorePresenter.onStart();
        scorePresenter.attachView(this);
    }

    @OnClick(R.id.btnClose)
    public void exit() {
        finish();
    }

    @OnClick(R.id.layoutNextPoints)
    public void goToPoints() {
        navigator.navigateToPoints(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}