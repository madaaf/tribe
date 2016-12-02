package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddAdminsToGroup;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.CreateGroup;
import com.tribe.app.domain.interactor.user.DiskGetMembership;
import com.tribe.app.domain.interactor.user.GetGroupInfos;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveAdminsFromGroup;
import com.tribe.app.domain.interactor.user.RemoveMembersFromGroup;
import com.tribe.app.domain.interactor.user.UpdateGroup;
import com.tribe.app.domain.interactor.user.UpdateMembership;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupPresenter implements Presenter {

    private final JobManager jobManager;
    private final GetGroupMembers getGroupMembers;
    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;
    private final AddMembersToGroup addMembersToGroup;
    private final DiskGetMembership diskGetMembership;
    private final LeaveGroup leaveGroup;
    private final UpdateMembership updateMembership;
    private final GetGroupInfos getGroupInfos;
    private final CreateFriendship createFriendship;
    private final RemoveMembersFromGroup removeMembersFromGroup;
    private final AddAdminsToGroup addAdminsToGroup;
    private final RemoveAdminsFromGroup removeAdminsFromGroup;

    private GroupView groupView;

    @Inject
    GroupPresenter(JobManager jobManager,
                   GetGroupMembers getGroupMembers,
                   CreateGroup createGroup,
                   UpdateGroup updateGroup,
                   AddMembersToGroup addMembersToGroup,
                   DiskGetMembership diskGetMembership,
                   LeaveGroup leaveGroup,
                   UpdateMembership updateMembership,
                   GetGroupInfos getGroupInfos,
                   CreateFriendship createFriendship,
                   RemoveMembersFromGroup removeMembersFromGroup,
                   AddAdminsToGroup addAdminsToGroup,
                   RemoveAdminsFromGroup removeAdminsFromGroup) {
        this.jobManager = jobManager;
        this.getGroupMembers = getGroupMembers;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.addMembersToGroup = addMembersToGroup;
        this.diskGetMembership = diskGetMembership;
        this.leaveGroup = leaveGroup;
        this.updateMembership = updateMembership;
        this.getGroupInfos = getGroupInfos;
        this.createFriendship = createFriendship;
        this.removeMembersFromGroup = removeMembersFromGroup;
        this.addAdminsToGroup = addAdminsToGroup;
        this.removeAdminsFromGroup = removeAdminsFromGroup;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        getGroupMembers.unsubscribe();
        createGroup.unsubscribe();
        updateGroup.unsubscribe();
        addMembersToGroup.unsubscribe();
        diskGetMembership.unsubscribe();
        leaveGroup.unsubscribe();
        getGroupInfos.unsubscribe();
        createFriendship.unsubscribe();
        removeMembersFromGroup.unsubscribe();
        addAdminsToGroup.unsubscribe();
        removeAdminsFromGroup.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        groupView = (GroupView) v;
    }

    @Override
    public void onCreate() {

    }

    public void createFriendship(String userId) {
        createFriendship.setUserId(userId);
        createFriendship.execute(new CreateFriendshipSubscriber());
    }

    private final class CreateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            groupView.onUserAddError();
            e.printStackTrace();
        }

        @Override
        public void onNext(Friendship friendship) {
            if (friendship == null) groupView.onUserAddError();
            else {
                jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.NEW_FRIENDSHIP, 1));
                groupView.onUserAddSuccess(friendship);
            }
        }
    }

    public void refreshGroupInfos(String groupId) {
        getGroupInfos.prepare(groupId);
        getGroupInfos.execute(new GroupInfosSubscriber());
    }

    private final class GroupInfosSubscriber extends DefaultSubscriber<Group> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Group group) {
            groupView.onGroupInfosSuccess(group);
        }
    }

    public void createGroup(GroupEntity groupEntity) {
        groupView.showLoading();
        createGroup.prepare(groupEntity);
        createGroup.execute(new CreateGroupSubscriber());
    }

    private final class CreateGroupSubscriber extends DefaultSubscriber<Membership> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            groupView.hideLoading();
            groupView.onGroupCreatedError();
        }

        @Override
        public void onNext(Membership membership) {
            groupView.hideLoading();
            groupView.onGroupCreatedSuccess(membership);
        }
    }

    public void membershipInfos(String membershipId) {
        diskGetMembership.prepare(membershipId);
        diskGetMembership.execute(new MembershipInfosSubscriber());
    }

    private final class MembershipInfosSubscriber extends DefaultSubscriber<Membership> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Membership membership) {
            groupView.onMembershipInfosSuccess(membership);
        }
    }

    //    public void setupMembers(Group group) {
//        this.groupView.setupGroup(group);
//    }
//
//    public void getGroupMembers(String groupId) {
//        getGroupMembers.prepare(groupId);
//        getGroupMembers.execute(new GetGroupMemberSubscriber());
//    }



//    public void updateScore() {
//        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CREATE_GROUP, 1));
//    }
//
    public void updateGroup(String groupId, GroupEntity groupEntity) {
        List<Pair<String, String>> values = new ArrayList<>();
        if (!StringUtils.isEmpty(groupEntity.getName()))
            values.add(new Pair<>(GroupRealm.NAME, groupEntity.getName()));
        if (!StringUtils.isEmpty(groupEntity.getImgPath()))
            values.add(new Pair<>(GroupRealm.PICTURE, groupEntity.getImgPath()));

        if (values.size() > 0) {
            groupView.showLoading();
            updateGroup.prepare(groupId, values);
            updateGroup.execute(new UpdateGroupSubscriber());
        }
    }

    private final class UpdateGroupSubscriber extends DefaultSubscriber<Group> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            groupView.hideLoading();
            groupView.onGroupUpdatedError();
        }

        @Override
        public void onNext(Group group) {
            groupView.hideLoading();
            groupView.onGroupUpdatedSuccess(group);
        }
    }

    public void leaveGroup(String membershipId) {
        leaveGroup.prepare(membershipId);
        leaveGroup.execute(new LeaveGroupSubscriber());
    }

    private final class LeaveGroupSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            groupView.onLeaveGroupError();
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            groupView.onLeaveGroupSuccess();
        }
    }

    public void updateMembership(String membershipId, boolean mute) {
        List<Pair<String, String>> values = new ArrayList<>();
        values.add(new Pair<>(MembershipRealm.MUTE, String.valueOf(mute)));

        if (values.size() > 0) {
            updateMembership.prepare(membershipId, values);
            updateMembership.execute(new DefaultSubscriber());
        }
    }

    public void addMembersToGroup(String groupId, List<GroupMember> memberList) {
        List<String> memberIds = new ArrayList<>();

        for (GroupMember member : memberList) {
            memberIds.add(member.getUser().getId());
        }

        groupView.showLoading();
        addMembersToGroup.prepare(groupId, memberIds);
        addMembersToGroup.execute(new AddMembersSubscriber());
    }

    private final class AddMembersSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            groupView.hideLoading();
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            groupView.onMemberAddedSuccess();
        }
    }

    public void removeMembersFromGroup(String groupId, User user) {
        List<String> memberIds = new ArrayList<>();
        memberIds.add(user.getId());
        removeMembersFromGroup.prepare(groupId, memberIds);
        removeMembersFromGroup.execute(new RemoveMemberFromGroupSubscriber());
    }

    private final class RemoveMemberFromGroupSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            groupView.onMemberRemoveError();
        }

        @Override
        public void onNext(Void aVoid) {
            groupView.onMemberRemoveSuccess();
        }
    }

    public void addAdminsToGroup(String groupId, User user) {
        List<String> memberIds = new ArrayList<>();
        memberIds.add(user.getId());
        addAdminsToGroup.prepare(groupId, memberIds);
        addAdminsToGroup.execute(new AddAdminsToGroupSubscriber());
    }

    private final class AddAdminsToGroupSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            groupView.onAddAdminError();
        }

        @Override
        public void onNext(Void aVoid) {
            groupView.onAddAdminSuccess();
        }
    }

    public void removeAdminsFromGroup(String groupId, User user) {
        List<String> memberIds = new ArrayList<>();
        memberIds.add(user.getId());
        removeAdminsFromGroup.prepare(groupId, memberIds);
        removeAdminsFromGroup.execute(new RemoveAdminsFromGroupSubscriber());
    }

    private final class RemoveAdminsFromGroupSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            groupView.onRemoveAdminSuccess();
        }

        @Override
        public void onNext(Void aVoid) {
            groupView.onRemoveAdminSuccess();
        }
    }
}
