package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.MemberListAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.MemberListLayoutManager;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/21/2016.
 */

public class MembersGroupView extends FrameLayout {

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    // VARIABLES
    private MemberListLayoutManager layoutManager;
    private MemberListAdapter adapter;
    private Membership membership;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<GroupMember> clickRemoveFromGroup = PublishSubject.create();
    private PublishSubject<User> clickAddFriend = PublishSubject.create();
    private PublishSubject<GroupMember> clickRemoveAdmin = PublishSubject.create();
    private PublishSubject<GroupMember> clickAddAdmin = PublishSubject.create();

    public MembersGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (membership == null) {
            Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);
            membership = (Membership) serializable;
            init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    public void updateGroup(Group group) {
        membership.setGroup(group);
        List<GroupMember> memberList = new ArrayList<>(membership.getGroup().getGroupMembers());
        user.computeMemberFriends(memberList);
        adapter.setItems(memberList);
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        layoutManager = new MemberListLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);

        adapter = new MemberListAdapter(getContext(), membership != null ? membership.isAdmin() : false);

        List<GroupMember> memberList = new ArrayList<>();
        if (membership.getGroup().getGroupMembers() != null) memberList.addAll(membership.getGroup().getGroupMembers());
        user.computeMemberFriends(memberList);
        adapter.setItems(memberList);
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerFirstLastItemDecoration(screenUtils.dpToPx(5), screenUtils.dpToPx(5)));

        subscriptions.add(adapter.clickAdd()
                .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)).getUser())
                .subscribe(clickAddFriend));

        subscriptions.add(adapter.longClick()
                .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
                .filter(groupMember -> !groupMember.getUser().equals(user))
                .flatMap(groupMember -> DialogFactory.showBottomSheetForGroupMembers(
                        getContext(),
                        groupMember),
                        (groupMember, genericType) -> {
                            if (genericType.getTypeDef().equals(LabelType.SET_AS_ADMIN)) {
                                clickAddAdmin.onNext(groupMember);
                            } else if (genericType.getTypeDef().equals(LabelType.REMOVE_FROM_ADMIN)) {
                                clickRemoveAdmin.onNext(groupMember);
                            } else if (genericType.getTypeDef().equals(LabelType.REMOVE_FROM_GROUP)) {
                                clickRemoveFromGroup.onNext(groupMember);
                            }

                            return groupMember.getUser();
                        }
                )
                .subscribe());
    }

    // OBSERVABLES
    public Observable<User> onClickAddFriend() {
        return clickAddFriend;
    }

    public Observable<GroupMember> onClickAddAdmin() {
        return clickAddAdmin;
    }

    public Observable<GroupMember> onClickRemoveFromGroup() {
        return clickRemoveFromGroup;
    }

    public Observable<GroupMember> onClickRemoveAdmin() {
        return clickRemoveAdmin;
    }
}
