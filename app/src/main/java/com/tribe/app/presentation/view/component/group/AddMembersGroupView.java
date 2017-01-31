package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.FriendMembersAdapter;
import com.tribe.app.presentation.view.adapter.MembersAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.diff.GroupMemberDiffCallback;
import com.tribe.app.presentation.view.adapter.manager.FriendMembersLayoutManager;
import com.tribe.app.presentation.view.adapter.manager.MembersLayoutManager;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/21/2016.
 */

public class AddMembersGroupView extends FrameLayout {

  private int DURATION_FADE = 150;
  private int RECYCLER_VIEW_ANIMATIONS_DURATION = 200;
  private int RECYCLER_VIEW_ANIMATIONS_DURATION_LONG = 300;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  @BindView(R.id.recyclerViewGroupMembers) RecyclerView recyclerViewGroupMembers;

  @BindView(R.id.appbar) AppBarLayout appBarLayout;

  @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbarLayout;

  @BindView(R.id.viewActionSettings) ActionView viewActionSettings;

  @BindView(R.id.viewActionShareLink) ActionView viewActionShareLink;

  @BindView(R.id.txtGroupName) TextViewFont txtGroupName;

  @BindView(R.id.txtMembers) TextViewFont txtMembers;

  @BindView(R.id.editTextSearch) EditTextFont editTextSearch;

  @BindView(R.id.viewGroupFocus) ViewGroup viewGroupFocus;

  @BindView(R.id.layoutMembers) ViewGroup layoutMembers;

  // VARIABLES
  private FriendMembersLayoutManager layoutManager;
  private FriendMembersAdapter adapter;
  private GroupEntity groupEntity;
  private Membership membership;
  private MembersLayoutManager layoutMembersManager;
  private MembersAdapter membersAdapter;
  private List<GroupMember> newMembers;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<List<GroupMember>> membersChanged = PublishSubject.create();
  private PublishSubject<Void> clickShareLink = PublishSubject.create();
  private PublishSubject<Void> clickSettings = PublishSubject.create();
  private PublishSubject<Void> clickMembers = PublishSubject.create();

  public AddMembersGroupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (membership == null && groupEntity == null) {
      Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

      if (serializable instanceof GroupEntity) {
        groupEntity = (GroupEntity) serializable;
      } else {
        membership = (Membership) serializable;
      }

      updateInfos();
      initAppBar();
      init();
    }

    viewGroupFocus.requestFocus();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (editTextSearch.hasFocus()) {
        Rect outRect = new Rect();
        editTextSearch.getGlobalVisibleRect(outRect);

        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
          editTextSearch.clearFocus();
          screenUtils.hideKeyboard(editTextSearch);
          viewGroupFocus.requestFocus();
        }
      }
    }

    return super.dispatchTouchEvent(event);
  }

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    subscriptions = new CompositeSubscription();

    newMembers = new ArrayList<>();

    layoutManager = new FriendMembersLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);

    adapter = new FriendMembersAdapter(getContext());

    setupFriendList();

    if (membership != null) {
      subscriptions.add(viewActionSettings.onClick().subscribe(clickSettings));

      subscriptions.add(viewActionShareLink.onClick().subscribe(clickShareLink));

      layoutMembers.setOnClickListener(v -> clickMembers.onNext(null));
    }

    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(2.5f), screenUtils.dpToPx(10), 0));
    recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(groupEntity == null);

    subscriptions.add(
        RxTextView.textChanges(editTextSearch).map(CharSequence::toString).subscribe(s -> {
          filter(s);
        }));

    subscriptions.add(adapter.clickAdd()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
        .subscribe(groupMember -> {
          boolean add = membersAdapter.compute(groupMember);
          if (add) {
            newMembers.add(groupMember);
          } else {
            newMembers.remove(groupMember);
          }
          membersChanged.onNext(newMembers);
          refactorMembers();
        }));

    editTextSearch.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        appBarLayout.setExpanded(false, true);
      }
    });

    setupMembers();
    refactorMembers();
  }

  private void setupFriendList() {
    List<GroupMember> userListTemp = new ArrayList<>(user.getUserList());
    if (membership != null) membership.getGroup().computeGroupMembers(userListTemp);
    adapter.setItems(userListTemp);
  }

  public void updateGroup(Group group, boolean full) {
    if (full) {
      membership.setGroup(group);
      DiffUtil.DiffResult result = DiffUtil.calculateDiff(
          new GroupMemberDiffCallback(membersAdapter.getItems(),
              membership.getGroup().getGroupMembers()));
      membersAdapter.setItems(membership.getGroup().getGroupMembers(), false);
      result.dispatchUpdatesTo(membersAdapter);
      refactorMembers();
      setupFriendList();
    } else {
      membership.getGroup().setPicture(group.getPicture());
      membership.getGroup().setName(group.getName());
    }

    updateInfos();
  }

  private void updateInfos() {
    txtGroupName.setText(groupName());

    if (membership != null) {
      viewActionShareLink.setBody("");
    }
  }

  private void initAppBar() {
    appBarLayout.setExpanded(groupEntity == null);

    CoordinatorLayout.LayoutParams params =
        (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
    AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
      @Override public boolean canDrag(AppBarLayout appBarLayout) {
        return groupEntity == null;
      }
    });
    params.setBehavior(behavior);
    appBarLayout.setLayoutParams(params);
  }

  private void setupMembers() {
    layoutMembersManager = new MembersLayoutManager(getContext());
    recyclerViewGroupMembers.setLayoutManager(layoutMembersManager);
    recyclerViewGroupMembers.setItemAnimator(new ScaleInAnimator(new OvershootInterpolator(1.5f)));
    recyclerViewGroupMembers.getItemAnimator()
        .setAddDuration(RECYCLER_VIEW_ANIMATIONS_DURATION_LONG);
    recyclerViewGroupMembers.getItemAnimator().setRemoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
    recyclerViewGroupMembers.getItemAnimator().setMoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
    recyclerViewGroupMembers.getItemAnimator().setChangeDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);

    membersAdapter = new MembersAdapter(getContext());

    if (groupEntity != null) {
      GroupMember groupMember = new GroupMember(user);
      groupMember.setOgMember(true);
      groupMember.setMember(true);
      membersAdapter.add(groupMember);
    } else {
      membersAdapter.setItems(membership.getGroup().getGroupMembers(), true);
    }

    recyclerViewGroupMembers.setAdapter(membersAdapter);
    recyclerViewGroupMembers.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewGroupMembers.setHasFixedSize(true);
    //recyclerViewGroupMembers.addItemDecoration(new MemberListLastItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small)));

    subscriptions.add(membersAdapter.onClick().subscribe(clickMembers));
  }

  private void refactorMembers() {
    txtMembers.setText(membersAdapter.getItemCount() + " " +
        (membersAdapter.getItemCount() > 1 ? getResources().getString(R.string.group_members)
            : getResources().getString(R.string.group_member)));
  }

  private void filter(String text) {
    adapter.filterList(text);
  }

  private String groupName() {
    return groupEntity == null ? membership.getDisplayName() : groupEntity.getName();
  }

  // OBSERVABLES
  public Observable<List<GroupMember>> onMembersChanged() {
    return membersChanged;
  }

  public Observable<Void> onClickSettings() {
    return clickSettings;
  }

  public Observable<Void> onClickShareLink() {
    return clickShareLink;
  }

  public Observable<Void> onClickMembers() {
    return clickMembers;
  }
}
