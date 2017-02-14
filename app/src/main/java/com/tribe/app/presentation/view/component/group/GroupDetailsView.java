package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.adapter.MemberListAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.MemberListLayoutManager;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class GroupDetailsView extends LinearLayout {

  @Inject User currentUser;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewActionInfos) ActionView viewActionInfos;

  @BindView(R.id.viewActionAddMembers) ActionView viewActionAddMembers;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  // VARIABLES
  private Membership membership;
  private MemberListLayoutManager layoutManager;
  private MemberListAdapter adapter;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Void> onGroupInfos = PublishSubject.create();
  private PublishSubject<Void> onAddMembers = PublishSubject.create();
  private PublishSubject<GroupMember> clickRemoveFromGroup = PublishSubject.create();
  private PublishSubject<User> clickAddFriend = PublishSubject.create();
  private PublishSubject<Friendship> onHangLive = PublishSubject.create();

  public GroupDetailsView(Context context, AttributeSet attrs) {
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
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    setOrientation(VERTICAL);

    subscriptions = new CompositeSubscription();

    viewActionInfos.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.group_details_settings_title)));

    viewActionAddMembers.setTitle(EmojiParser.demojizedText(
        getContext().getString(R.string.group_details_add_members_title)));

    subscriptions.add(viewActionInfos.onClick().subscribe(aVoid -> onGroupInfos.onNext(null)));
    subscriptions.add(viewActionAddMembers.onClick().subscribe(aVoid -> onAddMembers.onNext(null)));

    layoutManager = new MemberListLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);

    adapter = new MemberListAdapter(getContext());

    updateMembers();

    recyclerView.setAdapter(adapter);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(15), screenUtils.dpToPx(10), 1));

    subscriptions.add(adapter.clickAdd().map(view -> {
      GroupMember groupMember =
          (GroupMember) adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view));
      return groupMember.getUser();
    }).subscribe(clickAddFriend));

    subscriptions.add(adapter.onHangLive().map(view -> {
      GroupMember groupMember =
          (GroupMember) adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view));
      return groupMember.getFriendship();
    }).subscribe(onHangLive));
  }

  public void updateGroup(Group group) {
    if (group.getMembers() == null || group.getMembers().size() == 0) {
      membership.getGroup().setName(group.getName());
      membership.getGroup().setPicture(group.getPicture());
    } else {
      membership.setGroup(group);
    }

    updateMembers();
  }

  private void updateMembers() {
    List<GroupMember> memberList = new ArrayList<>();
    if (membership.getGroup().getGroupMembers() != null) {
      memberList.addAll(membership.getGroup().getGroupMembers());
    }

    if (adapter.getItemCount() > 0) {
      for (GroupMember groupMember : adapter.getItems()) {
        for (GroupMember newGroupMember : memberList) {
          if (groupMember.getUser().equals(newGroupMember.getUser())) {
            newGroupMember.setAnimateAdd(groupMember.isAnimateAdd());
          }
        }
      }
    }

    currentUser.computeMemberFriends(memberList);
    adapter.setItems(memberList);
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onAddMembers() {
    return onAddMembers;
  }

  public Observable<Void> onGroupInfos() {
    return onGroupInfos;
  }

  public Observable<User> onClickAddFriend() {
    return clickAddFriend;
  }

  public Observable<Friendship> onHangLive() {
    return onHangLive;
  }

  public Observable<GroupMember> onClickRemoveFromGroup() {
    return clickRemoveFromGroup;
  }
}
