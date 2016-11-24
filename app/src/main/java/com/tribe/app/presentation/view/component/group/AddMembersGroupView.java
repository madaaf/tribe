package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.FriendMembersAdapter;
import com.tribe.app.presentation.view.adapter.MembersAdapter;
import com.tribe.app.presentation.view.adapter.manager.FriendMembersLayoutManager;
import com.tribe.app.presentation.view.adapter.manager.MembersLayoutManager;
import com.tribe.app.presentation.view.decorator.DividerFirstLastItemDecoration;
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

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.recyclerViewGroupMembers)
    RecyclerView recyclerViewGroupMembers;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.txtGroupName)
    TextViewFont txtGroupName;

    @BindView(R.id.txtMembers)
    TextViewFont txtMembers;

    @BindView(R.id.editTextSearch)
    EditTextFont editTextSearch;

    // VARIABLES
    private FriendMembersLayoutManager layoutManager;
    private FriendMembersAdapter adapter;
    private NewGroupEntity newGroupEntity;
    private Group group;
    private MembersLayoutManager layoutMembersManager;
    private MembersAdapter membersAdapter;
    private List<GroupMember> newMembers;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<List<GroupMember>> onMembersChanged = PublishSubject.create();

    public AddMembersGroupView(Context context, AttributeSet attrs) {
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
        Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

        if (serializable instanceof NewGroupEntity) {
            newGroupEntity = (NewGroupEntity) serializable;
        } else {
            group = (Group) serializable;
        }

        setupInfos();
        setupAppBar();

        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        newMembers = new ArrayList<>();

        layoutManager = new FriendMembersLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);

        adapter = new FriendMembersAdapter(getContext());
        List<GroupMember> userListTemp = new ArrayList<>(user.getUserList());

        if (group != null) group.computeGroupMembers(userListTemp);

        List<GroupMember> userList = new ArrayList<>(userListTemp);
        adapter.setItems(userList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerFirstLastItemDecoration(screenUtils.dpToPx(5), screenUtils.dpToPx(10)));
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(newGroupEntity == null);

        subscriptions.add(
                RxTextView.textChanges(editTextSearch)
                        .map(CharSequence::toString)
                        .subscribe(s -> {
                            filter(s);
                        })
        );

        subscriptions.add(
                adapter.clickAdd()
                        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
                        .subscribe(groupMember -> {
                            boolean add = membersAdapter.compute(groupMember);
                            if (add) newMembers.add(groupMember);
                            else newMembers.remove(groupMember);
                            onMembersChanged.onNext(newMembers);
                            refactorMembers();
                        })
        );

        setupMembers();
        refactorMembers();
    }

    private void setupInfos() {
        txtGroupName.setText(groupName());
    }

    private void setupAppBar() {
        appBarLayout.setExpanded(newGroupEntity == null);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return newGroupEntity == null;
            }
        });
        params.setBehavior(behavior);
        appBarLayout.setLayoutParams(params);
    }

    private void setupMembers() {
        layoutMembersManager = new MembersLayoutManager(getContext());
        recyclerViewGroupMembers.setLayoutManager(layoutMembersManager);
        recyclerViewGroupMembers.setItemAnimator(new ScaleInAnimator(new OvershootInterpolator(1f)));
        recyclerViewGroupMembers.getItemAnimator().setAddDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
        recyclerViewGroupMembers.getItemAnimator().setRemoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
        recyclerViewGroupMembers.getItemAnimator().setMoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
        recyclerViewGroupMembers.getItemAnimator().setChangeDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);

        membersAdapter = new MembersAdapter(getContext());
        membersAdapter.add(new GroupMember(user));
        recyclerViewGroupMembers.setAdapter(membersAdapter);
        recyclerViewGroupMembers.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerViewGroupMembers.setHasFixedSize(true);
    }

    private void refactorMembers() {
        txtMembers.setText(membersAdapter.getItemCount() + " " +
                (membersAdapter.getItemCount() > 1 ? getResources().getString(R.string.group_members) : getResources().getString(R.string.group_member)));
    }

    private void filter(String text) {
        adapter.filterList(text);
    }

    private String groupName() {
        return newGroupEntity == null ? group.getName() : newGroupEntity.getName();
    }

    public Observable<List<GroupMember>> onMembersChanged() {
        return onMembersChanged;
    }
}
