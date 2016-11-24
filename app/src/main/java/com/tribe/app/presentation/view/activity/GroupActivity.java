package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.view.component.group.AddMembersGroupView;
import com.tribe.app.presentation.view.component.group.CreateGroupView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class GroupActivity extends BaseActivity implements GroupView {

    public static final String GROUP = "GROUP";

    public static Intent getCallingIntent(Context context, Group group) {
        Intent intent = new Intent(context, GroupActivity.class);
        if (group != null) intent.putExtra(GROUP, group);
        return intent;
    }

    private static final int DURATION = 300;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    GroupPresenter groupPresenter;

    @BindView(R.id.viewNavigatorStack)
    ViewStack viewStack;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtTitleTwo)
    TextViewFont txtTitleTwo;

    @BindView(R.id.txtAction)
    TextViewFont txtAction;

    @BindView(R.id.imgBack)
    ImageView imgBack;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VIEWS
    private CreateGroupView viewCreateGroup;
    private AddMembersGroupView viewAddMembersGroup;

    // VARIABLES
    private boolean disableUI = false;
    private Group group;
    private NewGroupEntity newGroupEntity;
    private List<GroupMember> newMembers;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init(savedInstanceState);
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        if (viewCreateGroup != null) viewCreateGroup.onDestroy();
        if (viewAddMembersGroup != null) viewAddMembersGroup.onDestroy();
        groupPresenter.onDestroy();
        if (unbinder != null) unbinder.unbind();
        super.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        newMembers = new ArrayList<>();

        if (getIntent().hasExtra(GROUP)) {
            group = (Group) getIntent().getSerializableExtra(GROUP);
            txtTitle.setText(group.getName());
        } else {
            txtAction.setText(R.string.action_create);
            txtAction.setVisibility(View.VISIBLE);
            txtTitle.setText(R.string.group_identification_title);
        }

        txtAction.setOnClickListener(v -> {
            if (group != null) {

            } else {
                List<String> membersId = new ArrayList<>();

                for (GroupMember groupMember : newMembers) {
                    membersId.add(groupMember.getUser().getId());
                }

                newGroupEntity.setMembersId(membersId);
                groupPresenter.createGroup(newGroupEntity);
            }
        });

        txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

        viewStack.setAnimationHandler(createCustomAnimationHandler());
        viewStack.addTraversingListener(traversingState -> disableUI = traversingState != TraversingState.IDLE);

        if (savedInstanceState == null) {
            viewCreateGroup = (CreateGroupView) viewStack.push(R.layout.view_create_group);
            subscriptions.add(
                    viewCreateGroup.onCreateNewGroup()
                            .subscribe(newGroup -> {
                                newGroupEntity = newGroup;
                                screenUtils.hideKeyboard(this);
                                viewAddMembersGroup = (AddMembersGroupView) viewStack.pushWithParameter(R.layout.view_add_members_group, newGroup);
                                subscriptions.add(
                                        viewAddMembersGroup.onMembersChanged()
                                                .subscribe(addedMembers -> {
                                                    newMembers.clear();
                                                    newMembers.addAll(addedMembers);

                                                    if (group != null) {
                                                        if (newMembers.size() > 0) {
                                                            txtAction.setVisibility(View.VISIBLE);
                                                            txtAction.setText(getString(R.string.action_add, newMembers.size()));
                                                        } else {
                                                            txtAction.setVisibility(View.GONE);
                                                        }
                                                    } else {
                                                        txtAction.setVisibility(View.VISIBLE);
                                                    }
                                                })
                                );
                            }));
        }
    }

    private void initPresenter() {
        groupPresenter.attachView(this);
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    @OnClick(R.id.imgBack)
    void clickBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (disableUI) {
            return;
        }

        if (!viewStack.pop()) {
            super.onBackPressed();
        } else {
            txtAction.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return disableUI || super.dispatchTouchEvent(ev);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (ViewStackHelper.matchesServiceName(name)) {
            return viewStack;
        }

        return super.getSystemService(name);
    }

    @NonNull
    private AnimationHandler createCustomAnimationHandler() {
        return (from, to, operation) -> {
            boolean forward = operation != TraversingOperation.POP;

            AnimatorSet set = new AnimatorSet();

            set.setInterpolator(new DecelerateInterpolator());

            final int width = from.getWidth();

            computeTitle(forward, from);

            if (forward) {
                to.setTranslationX(width);
                set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0 - (width)));
                set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
            } else {
                to.setTranslationX(0 - (width));
                set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, width));
                set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
            }

            return TraversalAnimation.newInstance(set,
                    forward ? TraversalAnimation.ABOVE : TraversalAnimation.BELOW);
        };
    }

    private void computeTitle(boolean forward, View from) {
        if (from instanceof AddMembersGroupView) {
            setupTitle(getString(R.string.group_identification_title), forward);
        } else if (from instanceof CreateGroupView) {
            setupTitle(newGroupEntity.getName(), forward);
        }
    }

    private void setupTitle(String title, boolean forward) {
        if (txtTitle.getTranslationX() == 0) {
            txtTitleTwo.setText(title);
            hideTitle(txtTitle, forward);
            showTitle(txtTitleTwo, forward);
        } else {
            hideTitle(txtTitleTwo, forward);
            showTitle(txtTitle, forward);
        }
    }

    private void hideTitle(View view, boolean forward) {
        if (forward) {
            view.animate()
                    .translationX(-(screenUtils.getWidthPx() / 3))
                    .alpha(0)
                    .setDuration(DURATION)
                    .start();
        } else {
            view.animate()
                    .translationX(screenUtils.getWidthPx())
                    .setDuration(DURATION)
                    .start();
        }
    }

    private void showTitle(View view, boolean forward) {
        if (forward) {
            view.setTranslationX(screenUtils.getWidthPx());
            view.setAlpha(1);
        } else {
            view.setTranslationX(-(screenUtils.getWidthPx() / 3));
            view.setAlpha(0);
        }

        view.animate()
                .translationX(0)
                .alpha(1)
                .setDuration(DURATION)
                .start();
    }

    @Override
    public void onGetMembersFailed() {

    }

    @Override
    public void onGroupCreatedSuccess(Membership membership) {
        finish();
    }

    @Override
    public void onGroupCreatedError() {

    }

    @Override
    public void onGroupUpdatedSuccess() {

    }

    @Override
    public void onGroupUpdatedError() {

    }

    @Override
    public void onMemberAddedSuccess() {

    }

    @Override
    public void onMemberAddedError() {

    }

    @Override
    public void showLoading() {
        txtAction.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        txtAction.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
    }
}