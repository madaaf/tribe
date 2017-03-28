package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupMVPView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.component.group.AddMembersGroupView;
import com.tribe.app.presentation.view.component.group.GroupDetailsView;
import com.tribe.app.presentation.view.component.group.UpdateGroupView;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class GroupActivity extends BaseActivity implements GroupMVPView {

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

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject GroupPresenter groupPresenter;

  @BindView(R.id.viewNavigatorStack) ViewStack viewStack;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtTitleTwo) TextViewFont txtTitleTwo;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.imgBack) ImageView imgBack;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VIEWS
  private AddMembersGroupView viewAddMembersGroup;
  private UpdateGroupView viewUpdateGroup;
  private GroupDetailsView viewDetailsGroup;
  private Map<String, Object> tagMap;

  // VARIABLES
  private boolean disableUI = false;
  private String membershipId;
  private String groupName;
  private Membership membership;
  private List<GroupMember> newMembers;
  private TextViewFont currentTitle;
  private FirebaseRemoteConfig firebaseRemoteConfig;

  // RESOURCES
  private int margin;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription settingsSubscriptions = new CompositeSubscription();
  GroupEntity groupEntity;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group);

    unbinder = ButterKnife.bind(this);
    initResources();
    initDependencyInjector();
    init(savedInstanceState);
    initRemoteConfig();
    initPresenter();
  }

  @Override protected void onStart() {
    super.onStart();
    groupPresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    groupPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    screenUtils.hideKeyboard(this);
    if (viewAddMembersGroup != null) viewAddMembersGroup.onDestroy();
    if (viewUpdateGroup != null) viewUpdateGroup.onDestroy();
    if (viewDetailsGroup != null) viewDetailsGroup.onDestroy();
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (settingsSubscriptions.hasSubscriptions()) settingsSubscriptions.unsubscribe();
    super.onDestroy();
  }

  private void init(Bundle savedInstanceState) {
    newMembers = new ArrayList<>();
    tagMap = new HashMap<>();

    if (getIntent().hasExtra(MEMBERSHIP_ID)) {
      membershipId = getIntent().getStringExtra(MEMBERSHIP_ID);
      groupName = getIntent().getStringExtra(GROUP_NAME);
      txtTitle.setText(groupName);
      setupAction(getString(R.string.group_details_invite_link));
      txtAction.setVisibility(View.VISIBLE);
    } else {
      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Groups_Creation);
      setupAction(getString(R.string.action_create));
      txtAction.setVisibility(View.VISIBLE);
      txtTitle.setText(R.string.group_identification_title);
    }

    txtAction.setOnClickListener(v -> {
      if (membershipId == null) {
        List<String> membersId = new ArrayList<>();

        for (GroupMember groupMember : newMembers) {
          membersId.add(groupMember.getUser().getId());
        }

        tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.CREATED);
        tagMap.put(TagManagerUtils.MEMBERS_COUNT, membersId.size());
        TagManagerUtils.manageTags(tagManager, tagMap);
        tagManager.increment(TagManagerUtils.USER_GROUPS_COUNT);

        groupEntity = new GroupEntity();
        groupEntity.setMembersId(membersId);
        groupEntity.setName(EmojiParser.demojizedText(getDefaultGroupName()));

        groupPresenter.createGroup(groupEntity);
      } else {
        if (viewStack.getTopView() instanceof UpdateGroupView) {
          tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.MODIFIED);
          tagMap.put(TagManagerUtils.NOTIFICATIONS_ENABLED, !membership.isMute());
          TagManagerUtils.manageTags(tagManager, tagMap);
          groupPresenter.updateGroup(membership.getSubId(), viewUpdateGroup.getGroupEntity());
        } else if (viewStack.getTopView() instanceof AddMembersGroupView) {
          tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.MODIFIED);
          tagMap.put(TagManagerUtils.MEMBERS_COUNT,
              membership.getGroup().getMembers().size() + newMembers.size());
          tagMap.put(TagManagerUtils.MEMBERS_ADDED_COUNT, newMembers.size());
          TagManagerUtils.manageTags(tagManager, tagMap);

          groupPresenter.addMembersToGroup(membership.getSubId(), newMembers);
        } else if (viewStack.getTopView() instanceof GroupDetailsView) {
          Bundle bundle = new Bundle();
          bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.GROUP);
          bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
          tagManager.trackEvent(TagManagerUtils.Invites, bundle);
          navigator.openSmsForInvite(this);
        }
      }
    });

    txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

    viewStack.setAnimationHandler(createCustomAnimationHandler());
    viewStack.addTraversingListener(
        traversingState -> disableUI = traversingState != TraversingState.IDLE);

    if (savedInstanceState == null) {
      if (membershipId == null) {
        setupAddMembersView(null);
      }
    }
  }

  private void initRemoteConfig() {
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
    firebaseRemoteConfig.setDefaults(R.xml.firebase_default_config);

    firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
      } else {
        if (groupEntity != null) groupEntity.setName(EmojiParser.demojizedText(getDefaultGroupName()));
      }
    });
  }

  private String getDefaultGroupName() {
    String defaultGroupName =
        firebaseRemoteConfig.getString(Constants.FIREBASE_GROUP_DEFAULT_NAMES);
    String[] result = defaultGroupName.split(",");
    Random r = new Random();
    int randomPosition = r.nextInt(result.length - 1);
    return result[randomPosition];
  }

  private void initResources() {
    margin = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
  }

  private void initPresenter() {
    groupPresenter.onViewAttached(this);

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

  @OnClick(R.id.imgBack) void clickBack() {
    if (!tagMap.containsKey(TagManagerUtils.ACTION) && tagMap.containsKey(TagManagerUtils.EVENT)) {
      tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.CANCELLED);

      if (TagManagerUtils.Groups_Creation.equals(tagMap.get(TagManagerUtils.EVENT))) {
        tagMap.put(TagManagerUtils.MEMBERS_COUNT, 0);
      } else if (TagManagerUtils.Groups_Members.equals(tagMap.get(TagManagerUtils.EVENT))) {
        tagMap.put(TagManagerUtils.MEMBERS_COUNT, membership.getGroup().getMembers().size());
        tagMap.put(TagManagerUtils.MEMBERS_ADDED_COUNT, 0);
      } else if (TagManagerUtils.Groups_Settings.equals(tagMap.get(TagManagerUtils.EVENT))) {
        tagMap.put(TagManagerUtils.NOTIFICATIONS_ENABLED, !membership.isMute());
      }

      TagManagerUtils.manageTags(tagManager, tagMap);
    }

    onBackPressed();
  }

  @Override public void onBackPressed() {
    if (disableUI) {
      return;
    }

    if (!viewStack.pop()) {
      super.onBackPressed();
    }
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    return disableUI || super.dispatchTouchEvent(ev);
  }

  @Override public Object getSystemService(@NonNull String name) {
    if (ViewStackHelper.matchesServiceName(name)) {
      return viewStack;
    }

    return super.getSystemService(name);
  }

  @NonNull private AnimationHandler createCustomAnimationHandler() {
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
    if (membership != null) {
      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Groups_Members);
    }

    viewAddMembersGroup =
        (AddMembersGroupView) viewStack.pushWithParameter(R.layout.view_group_add_members, param);
    subscriptions.add(viewAddMembersGroup.onMembersChanged().subscribe(addedMembers -> {
      newMembers.clear();
      newMembers.addAll(addedMembers);

      if (membershipId != null) {
        if (newMembers.size() > 0) {
          txtAction.setVisibility(View.VISIBLE);
          setupAction(getString(R.string.action_add, newMembers.size()));
        } else {
          txtAction.setVisibility(View.GONE);
        }
      } else {
        setupAction(getString(R.string.action_create) + (newMembers.size() > 0 ? " ("
            + newMembers.size()
            + ")" : ""));
        txtAction.setVisibility(View.VISIBLE);
      }
    }));
  }

  private void setupGroupDetails(Serializable param) {
    viewDetailsGroup =
        (GroupDetailsView) viewStack.pushWithParameter(R.layout.view_group_details, param);

    subscriptions.add(viewDetailsGroup.onClickAddFriend().subscribe(user -> {
      if (user.isInvisible()) {
        DialogFactory.dialog(this, user.getDisplayName(),
            EmojiParser.demojizedText(getString(R.string.add_friend_error_invisible)),
            getString(R.string.add_friend_error_invisible_invite_ios),
            getString(R.string.add_friend_error_invisible_cancel))
            .filter(x -> x == true)
            .subscribe(a -> {
              navigator.openSmsForInvite(this);
            });
      } else {
        groupPresenter.createFriendship(user.getId());
      }
    }));

    subscriptions.add(viewDetailsGroup.onHangLive().subscribe(friendship -> {
      navigator.navigateToLive(this, friendship, PaletteGrid.get(0));
    }));

    subscriptions.add(viewDetailsGroup.onAddMembers().subscribe(aVoid -> {
      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Groups_Infos);
      tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.MEMBERS);
      TagManagerUtils.manageTags(tagManager, tagMap);

      setupAddMembersView(membership);
    }));

    subscriptions.add(viewDetailsGroup.onGroupInfos().subscribe(aVoid -> {
      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Groups_Infos);
      tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.SETTINGS);
      TagManagerUtils.manageTags(tagManager, tagMap);

      setupUpdateView();
    }));
  }

  private void setupUpdateView() {
    tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Groups_Settings);

    viewUpdateGroup =
        (UpdateGroupView) viewStack.pushWithParameter(R.layout.view_group_update, membership);

    subscriptions.add(viewUpdateGroup.onLeaveGroup().subscribe(aVoid -> {
      tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.LEFT);
      tagMap.put(TagManagerUtils.NOTIFICATIONS_ENABLED, !membership.isMute());
      TagManagerUtils.manageTags(tagManager, tagMap);
      groupPresenter.leaveGroup(membershipId);
    }));

    subscriptions.add(viewUpdateGroup.onNotificationsChange().subscribe(aBoolean -> {
      tagMap.put(TagManagerUtils.ACTION, TagManagerUtils.MODIFIED);
      tagMap.put(TagManagerUtils.NOTIFICATIONS_ENABLED, !aBoolean);
      TagManagerUtils.manageTags(tagManager, tagMap);
      membership.setMute(!aBoolean);
      groupPresenter.updateMembership(membershipId, !aBoolean);
    }));
  }

  private void computeTitle(boolean forward, View to) {
    if (to instanceof GroupDetailsView) {
      setupTitle(membership.getDisplayName(), forward);
      txtAction.setVisibility(View.VISIBLE);
      setupAction(getString(R.string.group_details_invite_link));
    } else if (to instanceof AddMembersGroupView) {
      setupTitle(getString(R.string.group_add_members_title), forward);
      if (newMembers.size() > 0) {
        txtAction.setVisibility(View.VISIBLE);
        setupAction(getString(R.string.action_add, newMembers.size()));
      } else {
        txtAction.setVisibility(View.GONE);
      }
    } else if (to instanceof UpdateGroupView) {
      setupTitle(getString(R.string.group_settings_title), forward);
      txtAction.setVisibility(View.VISIBLE);
      setupAction(getString(R.string.action_save));
    }
  }

  private void setupTitle(String title, boolean forward) {
    if (txtTitle.getTranslationX() == 0) {
      currentTitle = txtTitleTwo;
      txtTitleTwo.setText(title);
      hideTitle(txtTitle, forward);
      showTitle(txtTitleTwo, forward);
    } else {
      currentTitle = txtTitle;
      txtTitle.setText(title);
      hideTitle(txtTitleTwo, forward);
      showTitle(txtTitle, forward);
    }
  }

  private void setupAction(String action) {
    txtAction.setText(action);
    txtAction.measure(0, 0);

    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) txtTitle.getLayoutParams();
    params.leftMargin = txtAction.getMeasuredWidth() + margin * 2;
    params.rightMargin = params.leftMargin;
    txtTitle.setLayoutParams(params);

    params = (ViewGroup.MarginLayoutParams) txtTitleTwo.getLayoutParams();
    params.leftMargin = txtAction.getMeasuredWidth() + margin * 2;
    params.rightMargin = params.leftMargin;
    txtTitleTwo.setLayoutParams(params);
  }

  private void hideTitle(View view, boolean forward) {
    if (forward) {
      view.animate()
          .translationX(-(screenUtils.getWidthPx() / 3))
          .alpha(0)
          .setDuration(DURATION)
          .start();
    } else {
      view.animate().translationX(screenUtils.getWidthPx()).setDuration(DURATION).start();
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

    view.animate().translationX(0).alpha(1).setDuration(DURATION).start();
  }

  private void updateGroup(Group group, boolean full) {
    if (full) {
      membership.setGroup(group);
    } else {
      membership.getGroup().setName(group.getName());
      membership.getGroup().setPicture(group.getPicture());
    }

    if (viewAddMembersGroup != null) viewAddMembersGroup.updateGroup(group, full);
    if (viewDetailsGroup != null) viewDetailsGroup.updateGroup(group);
  }

  @Override public void onGroupInfosSuccess(Group group) {

  }

  @Override public void onGroupInfosFailed() {

  }

  @Override public void onMembershipInfosSuccess(Membership membership) {
    this.membership = membership;
    setupGroupDetails(this.membership);
    updateGroup(membership.getGroup(), true);
  }

  @Override public void onMembershipInfosFailed() {

  }

  @Override public void onGetMembersFailed() {

  }

  @Override public void onGroupCreatedSuccess(Membership membership) {
    finish();
  }

  @Override public void onGroupCreatedError() {

  }

  @Override public void onGroupUpdatedSuccess(Group group) {
    txtAction.setVisibility(View.GONE);
    updateGroup(group, false);

    if (viewStack.getTopView() instanceof UpdateGroupView) viewStack.pop();
  }

  @Override public void onGroupUpdatedError() {
    txtAction.setVisibility(View.GONE);
  }

  @Override public void onMemberAddedSuccess() {
    finish();
  }

  @Override public void onMemberAddedError() {

  }

  @Override public void onLeaveGroupError() {

  }

  @Override public void onUserAddSuccess(Friendship friendship) {
    user.getFriendships().add(friendship);

    viewDetailsGroup.postDelayed(() -> {
      if (viewStack.getTopView() instanceof GroupDetailsView) {
        viewDetailsGroup.updateGroup(membership.getGroup());
      }
    }, 1000);
  }

  @Override public void onUserAddError() {

  }

  @Override public void onMemberRemoveError() {

  }

  @Override public void onMemberRemoveSuccess() {

  }

  @Override public void onAddAdminError() {

  }

  @Override public void onAddAdminSuccess() {

  }

  @Override public void onRemoveAdminError() {

  }

  @Override public void onRemoveAdminSuccess() {

  }

  @Override public void onLeaveGroupSuccess() {
    finish();
  }

  @Override public void showLoading() {
    txtAction.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    txtAction.setVisibility(View.VISIBLE);
    progressView.setVisibility(View.GONE);
  }
}