package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.GroupType;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GroupMemberPresenter;
import com.tribe.app.presentation.mvp.view.GroupMemberView;
import com.tribe.app.presentation.view.adapter.GroupMemberAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMemberFragment extends BaseFragment implements GroupMemberView {

    public static GroupMemberFragment newInstance(Bundle args) {
        GroupMemberFragment fragment = new GroupMemberFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupMemberFragment() {
        setRetainInstance(true);
    }

    // Subscriptions
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> imageBackClicked = PublishSubject.create();

    // Bind view
    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;
    @BindView(R.id.imgBack)
    ImageView imgBack;
    @BindView(R.id.editTextSearchGroupMembers)
    EditTextFont editTextSearchGroupMembers;
    @BindView(R.id.recyclerViewGroupMembers)
    RecyclerView recyclerViewGroupMembers;

    // Dagger Dependencies
    @Inject
    GroupMemberPresenter groupMemberPresenter;
    @Inject
    GroupMemberAdapter groupMemberAdapter;

    // Variables
    private String groupId;
    private GroupMember groupMemberClicked;
    private int itemPosition;
    private LabelSheetAdapter labelSheetAdapter;
    private List<GroupMember> groupMemberList = new ArrayList<>();
    private List<GroupMember> groupMemberListCopy = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private boolean currentUserIsAdmin = false;
    BottomSheetDialog bottomSheetDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_group_member, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();
        initUi();
        initGroupMemberList();
        return fragmentView;
    }

    @Override
    public void onDestroy() {

        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initUi() {
        // Subscriptions
        subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
            imageBackClicked.onNext(null);
        }));
    }

    public Observable<Void> imageBackClicked() {
        return imageBackClicked;
    }

    /**
     * Init Search & List
     */

    private void initGroupMemberList() {
        groupMemberList = getArguments().getParcelableArrayList("groupMemberList");
        groupMemberListCopy = new ArrayList<>();
        groupMemberPresenter.attachView(this);
        if (groupMemberList != null) {
            groupMemberListCopy.addAll(groupMemberList);
            groupMemberAdapter.setHasStableIds(true);
            groupMemberAdapter.setItems(groupMemberList);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewGroupMembers.setLayoutManager(linearLayoutManager);
        recyclerViewGroupMembers.setAdapter(groupMemberAdapter);

        for (GroupMember groupMember : groupMemberList) {
            if (groupMember.isAdmin() && groupMember.getUserId().equals(getCurrentUser().getId())) currentUserIsAdmin = true;
        }

        // Set up bottom sheet
        subscriptions.add(groupMemberAdapter.clickMemberItem().subscribe(groupMemberView -> {
            itemPosition = (Integer) groupMemberView.getTag(R.id.tag_position);
            GroupMember groupMember = groupMemberAdapter.getItemAtPosition(itemPosition);
            groupMemberClicked = groupMember;
            List<LabelType> labelTypes = new ArrayList<>();
            if (groupMember.isCurrentUser()) return;
            if (currentUserIsAdmin) {
                labelTypes.add(new GroupType(getString(R.string.group_members_action_remove_member), GroupType.REMOVE_MEMBER));
                if (!groupMember.isAdmin()) labelTypes.add(new GroupType(getString(R.string.group_members_action_add_admin), GroupType.ADD_ADMIN));
                if (groupMember.isAdmin()) labelTypes.add(new GroupType(getString(R.string.group_members_action_remove_admin), GroupType.REMOVE_ADMIN));
            }
            if (groupMember.isFriend()) {
                labelTypes.add(new GroupType(getString(R.string.group_members_action_remove_friend), GroupType.UNFRIEND));
            } else {
                labelTypes.add(new GroupType(getString(R.string.group_members_action_add_friend), GroupType.ADD_FRIEND));
            }
            labelTypes.add(new GroupType(getString(R.string.action_cancel), GroupType.CANCEL));
            prepareBottomSheet(labelTypes);
        }));
    }

    private void prepareBottomSheet(List<LabelType> items) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        RecyclerView recyclerViewBottomSheet = (RecyclerView) view.findViewById(R.id.recyclerViewBottomSheet);
        recyclerViewBottomSheet.setHasFixedSize(true);
        recyclerViewBottomSheet.setLayoutManager(new LinearLayoutManager(getActivity()));
        labelSheetAdapter = new LabelSheetAdapter(getContext(), items);
        labelSheetAdapter.setHasStableIds(true);
        recyclerViewBottomSheet.setAdapter(labelSheetAdapter);
        bottomSheetDialog = new BottomSheetDialog(getContext());
        subscriptions.add(labelSheetAdapter.clickLabelItem().subscribe(labelView -> {
            GroupType groupType = (GroupType) labelSheetAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position));
            if (groupType.getGroupTypeDef().equals(GroupType.REMOVE_MEMBER)) {
                List<String> memberIds = new ArrayList<>();
                memberIds.add(groupMemberClicked.getUserId());
                groupMemberPresenter.removeMembersFromGroup(groupId, memberIds);
            }
            if (groupType.getGroupTypeDef().equals(GroupType.ADD_ADMIN)) {
                List<String> memberIds = new ArrayList<>();
                memberIds.add(groupMemberClicked.getUserId());
                groupMemberPresenter.addAdminsToGroup(groupId, memberIds);
            }
            if (groupType.getGroupTypeDef().equals(GroupType.REMOVE_ADMIN)) {
                List<String> memberIds = new ArrayList<>();
                memberIds.add(groupMemberClicked.getUserId());
                groupMemberPresenter.removeAdminsFromGroup(groupId, memberIds);
            }
            if (groupType.getGroupTypeDef().equals(GroupType.ADD_FRIEND)) {
                groupMemberPresenter.createFriendship(groupMemberClicked.getUserId());
            }
            if (groupType.getGroupTypeDef().equals(GroupType.UNFRIEND)) {
                groupMemberPresenter.removeFriendship(groupMemberClicked.getFriendshipId());
            }
            bottomSheetDialog.dismiss();

        }));
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            labelSheetAdapter.releaseSubscriptions();
            bottomSheetDialog = null;
        });
    }

    @Override
    public void createFriendship() {
        groupMemberClicked.setFriend(true);
        groupMemberAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeFriend() {
        groupMemberClicked.setFriend(false);
        groupMemberAdapter.notifyDataSetChanged();
    }

    @Override
    public void setAdmin() {
        groupMemberClicked.setAdmin(true);
        groupMemberAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeAdmin() {
        groupMemberClicked.setAdmin(false);
        groupMemberAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeMember() {
        groupMemberList.remove(itemPosition);
        groupMemberAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return null;
    }

    /**
     * Dependency injection set-up
     */
    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}
