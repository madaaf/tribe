package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
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
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/21/2016.
 */

public class AddMembersGroupView extends LinearLayout {

  private int DURATION_FADE = 150;
  private int RECYCLER_VIEW_ANIMATIONS_DURATION = 200;
  private int RECYCLER_VIEW_ANIMATIONS_DURATION_LONG = 300;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  @BindView(R.id.recyclerViewGroupMembers) RecyclerView recyclerViewGroupMembers;

  @BindView(R.id.txtMembers) TextViewFont txtMembers;

  @BindView(R.id.editTextSearch) EditTextFont editTextSearch;

  @BindView(R.id.viewGroupFocus) ViewGroup viewGroupFocus;

  @BindView(R.id.layoutMembers) ViewGroup layoutMembers;

  // VARIABLES
  private FriendMembersLayoutManager layoutManager;
  private FriendMembersAdapter adapter;
  private Membership membership;
  private MembersLayoutManager layoutMembersManager;
  private MembersAdapter membersAdapter;
  private List<GroupMember> newMembers;
  private String currentFilter = "";

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<List<GroupMember>> membersChanged = PublishSubject.create();
  private PublishSubject<Pair<Integer, GroupMember>> onRemoved = PublishSubject.create();

  public AddMembersGroupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (membership == null) {
      Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

      if (serializable instanceof Membership) {
        membership = (Membership) serializable;
      }

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

    setOrientation(VERTICAL);

    subscriptions = new CompositeSubscription();

    newMembers = new ArrayList<>();

    layoutManager = new FriendMembersLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);

    adapter = new FriendMembersAdapter(getContext());

    setupFriendList();

    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(15f), screenUtils.dpToPx(10), 1));
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(membership != null);

    subscriptions.add(
        RxTextView.textChanges(editTextSearch).map(CharSequence::toString).subscribe(s -> {
          filter(s);
        }));

    subscriptions.add(adapter.clickAdd().map(view -> {
      editTextSearch.setText("");
      int position = recyclerView.getChildLayoutPosition(view);
      return new Pair<>(position, (GroupMember) adapter.getItemAtPosition(position));
    }).subscribe(pair -> {
      GroupMember groupMember = pair.second;
      boolean add = membersAdapter.isAdd(groupMember);
      if (add) {
        membersAdapter.compute(groupMember);
        adapter.notifyItemChanged(pair.first);
        newMembers.add(groupMember);
        membersChanged.onNext(newMembers);
        refactorMembers();
      } else {
        onRemoved.onNext(pair);
      }
    }));

    subscriptions.add(onRemoved.flatMap(
        pair -> DialogFactory.dialog(getContext(), pair.second.getUser().getDisplayName(),
            getContext().getString(R.string.group_members_remove_member_alert_message),
            getContext().getString(R.string.group_members_remove_member_alert_confirm,
                pair.second.getUser().getDisplayName()),
            getContext().getString(R.string.action_nevermind)),
        (pair, aBoolean) -> new Pair<>(pair, aBoolean))
        .filter(pair -> pair.second == true)
        .subscribe(pair -> {
          GroupMember groupMember = pair.first.second;
          adapter.notifyItemChanged(pair.first.first);
          membersAdapter.compute(groupMember);
          newMembers.remove(groupMember);
          membersChanged.onNext(newMembers);
          refactorMembers();
        }));

    subscriptions.add(onRemoved.subscribe(groupMember -> {

    }));

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

    if (membership == null) {
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
  }

  private void refactorMembers() {
    txtMembers.setText(membersAdapter.getItemCount() + " " +
        (membersAdapter.getItemCount() > 1 ? getResources().getString(R.string.group_members)
            : getResources().getString(R.string.group_member)) + " " + "\uD83D\uDD25");
  }

  private void filter(String text) {
    if (text.equals(currentFilter)) return;

    currentFilter = text;
    adapter.filterList(text);
  }

  private String groupName() {
    return membership != null ? membership.getDisplayName() : "";
  }

  // OBSERVABLES
  public Observable<List<GroupMember>> onMembersChanged() {
    return membersChanged;
  }
}
