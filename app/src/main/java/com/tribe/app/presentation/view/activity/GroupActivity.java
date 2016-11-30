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
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.group.AddMembersGroupView;
import com.tribe.app.presentation.view.component.group.CreateGroupView;
import com.tribe.app.presentation.view.component.group.MembersGroupView;
import com.tribe.app.presentation.view.component.group.SettingsGroupView;
import com.tribe.app.presentation.view.component.group.UpdateGroupView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class GroupActivity extends BaseActivity implements GroupView {

    public static final String MEMBERSHIP_ID = "MEMBERSHIP_ID";
    public static final String GROUP_NAME = "GROUP_NAME";
    public static final String GROUP_PICTURE = "GROUP_PICTURE";


    public static Intent getCallingIntent(Context context, Membership membership) {
        Intent intent = new Intent(context, GroupActivity.class);
        if (membership != null) {
            intent.putExtra(MEMBERSHIP_ID, membership.getId());
            intent.putExtra(GROUP_NAME, membership.getDisplayName());
            intent.putExtra(GROUP_PICTURE, membership.getProfilePicture());
        }

        return intent;
    }

    private static final int DURATION = 200;

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
    private SettingsGroupView viewSettingsGroup;
    private UpdateGroupView viewUpdateGroup;
    private MembersGroupView viewMembersGroup;

    // VARIABLES
    private boolean disableUI = false;
    private String membershipId;
    private String groupName;
    private String groupPicture;
    private Membership membership;
    private GroupEntity groupEntity;
    private List<GroupMember> newMembers;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeSubscription settingsSubscriptions = new CompositeSubscription();

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
        if (viewSettingsGroup != null) viewSettingsGroup.onDestroy();
        if (viewUpdateGroup != null) viewUpdateGroup.onDestroy();
        if (viewMembersGroup != null) viewMembersGroup.onDestroy();
        groupPresenter.onDestroy();
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (settingsSubscriptions.hasSubscriptions()) settingsSubscriptions.unsubscribe();
        super.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        newMembers = new ArrayList<>();

        if (getIntent().hasExtra(MEMBERSHIP_ID)) {
            membershipId = getIntent().getStringExtra(MEMBERSHIP_ID);
            groupName = getIntent().getStringExtra(GROUP_NAME);
            groupPicture = getIntent().getStringExtra(GROUP_PICTURE);
            txtTitle.setText(groupName);
        } else {
            txtAction.setText(R.string.action_create);
            txtAction.setVisibility(View.GONE);
            txtTitle.setText(R.string.group_identification_title);
        }

        txtAction.setOnClickListener(v -> {
            if (membershipId == null) {
                List<String> membersId = new ArrayList<>();

                for (GroupMember groupMember : newMembers) {
                    membersId.add(groupMember.getUser().getId());
                }

                groupEntity.setMembersId(membersId);
                groupPresenter.createGroup(groupEntity);
            } else {
                if (viewStack.getTopView() instanceof UpdateGroupView) {
                    groupPresenter.updateGroup(membership.getSubId(), viewUpdateGroup.getGroupEntity());
                } else if (viewStack.getTopView() instanceof AddMembersGroupView) {
                    groupPresenter.updateGroup(membership.getSubId(), viewUpdateGroup.getGroupEntity());
                }
            }
        });

        txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

        viewStack.setAnimationHandler(createCustomAnimationHandler());
        viewStack.addTraversingListener(traversingState -> disableUI = traversingState != TraversingState.IDLE);

        if (savedInstanceState == null) {
            if (membershipId == null) {
                viewCreateGroup = (CreateGroupView) viewStack.push(R.layout.view_create_group);
                subscriptions.add(
                        viewCreateGroup.onCreateNewGroup()
                                .subscribe(newGroup -> {
                                    groupEntity = newGroup;
                                    screenUtils.hideKeyboard(this);
                                    setupAddMembersView(groupEntity);
                                }));
            }
        }
    }

    private void initPresenter() {
        groupPresenter.attachView(this);

        if (!StringUtils.isEmpty(membershipId)) {
            groupPresenter.membershipInfos(membershipId);
        }
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

            set.setDuration(DURATION);
            set.setInterpolator(new DecelerateInterpolator());

            final int width = from.getWidth();

            computeTitle(forward, to);

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

    private void setupAddMembersView(Serializable param) {
        viewAddMembersGroup = (AddMembersGroupView) viewStack.pushWithParameter(R.layout.view_add_members_group, param);
        subscriptions.add(
                viewAddMembersGroup.onMembersChanged()
                        .subscribe(addedMembers -> {
                            newMembers.clear();
                            newMembers.addAll(addedMembers);

                            if (membershipId != null) {
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

        subscriptions.add(
                viewAddMembersGroup.onClickMembers()
                        .subscribe(aVoid -> {
                            setupMemberListGroupView();
                        })
        );

        if (param instanceof Membership) {
            subscriptions.add(viewAddMembersGroup.onClickSettings()
                    .subscribe(aVoid -> {
                        setupSettingsView();
                    })
            );

            subscriptions.add(viewAddMembersGroup.onClickShareLink()
                    .subscribe(aVoid -> {
                        navigator.shareGenericText(membership.getGroup().getGroupLink(), this);
                    })
            );
        }
    }

    private void setupSettingsView() {
        viewSettingsGroup = (SettingsGroupView) viewStack.pushWithParameter(R.layout.view_settings_group, membership);
        viewSettingsGroup.onEditGroup()
                .subscribe(aVoid -> {
                    setupUpdateView();
                });

        viewSettingsGroup.onLeaveGroup()
                .subscribe(aVoid -> {
                    groupPresenter.leaveGroup(membershipId);
                });

        viewSettingsGroup.onNotificationsChange()
                .subscribe(aBoolean -> {
                    groupPresenter.updateMembership(membershipId, !aBoolean);
                });
    }

    private void setupUpdateView() {
        viewUpdateGroup = (UpdateGroupView) viewStack.pushWithParameter(R.layout.view_update_group, membership);
        txtAction.setVisibility(View.VISIBLE);
        txtAction.setText(getString(R.string.action_save));
    }

    private void setupMemberListGroupView() {
        viewMembersGroup = (MembersGroupView) viewStack.pushWithParameter(R.layout.view_members_group, membership);
    }

    private void computeTitle(boolean forward, View to) {
        if (to instanceof AddMembersGroupView) {
            setupTitle(groupEntity == null ? membership.getDisplayName() : groupEntity.getName(), forward);
        } else if (to instanceof CreateGroupView) {
            setupTitle(getString(R.string.group_identification_title), forward);
        } else if (to instanceof SettingsGroupView) {
            setupTitle(getString(R.string.group_settings_title), forward);
        } else if (to instanceof UpdateGroupView) {
            setupTitle(getString(R.string.group_name_title), forward);
        }
    }

    private void setupTitle(String title, boolean forward) {
        if (txtTitle.getTranslationX() == 0) {
            txtTitleTwo.setText(title);
            hideTitle(txtTitle, forward);
            showTitle(txtTitleTwo, forward);
        } else {
            txtTitle.setText(title);
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

    private void updateGroup(Group group, boolean full) {
        if (full) {
            membership.setGroup(group);
        } else {
            membership.getGroup().setName(group.getName());
            membership.getGroup().setPicture(group.getPicture());
        }
    }

    @Override
    public void onGroupInfosSuccess(Group group) {
        updateGroup(group, true);
    }

    @Override
    public void onGroupInfosFailed() {

    }

    @Override
    public void onMembershipInfosSuccess(Membership membership) {
        this.membership = membership;
        setupAddMembersView(this.membership);
        groupPresenter.refreshGroupInfos(membership.getSubId());
    }

    @Override
    public void onMembershipInfosFailed() {

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
    public void onGroupUpdatedSuccess(Group group) {
        txtAction.setVisibility(View.GONE);
        updateGroup(group, false);
        viewSettingsGroup.updateGroup(group, false);
        viewAddMembersGroup.updateGroup(membership.getGroup(), false);

        if (viewStack.getTopView() instanceof UpdateGroupView) viewStack.pop();
    }

    @Override
    public void onGroupUpdatedError() {
        txtAction.setVisibility(View.GONE);
    }

    @Override
    public void onMemberAddedSuccess() {

    }

    @Override
    public void onMemberAddedError() {

    }

    @Override
    public void onLeaveGroupError() {

    }

    @Override
    public void onLeaveGroupSuccess() {
        finish();
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