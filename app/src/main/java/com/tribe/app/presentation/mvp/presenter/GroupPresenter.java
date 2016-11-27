package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateGroup;
import com.tribe.app.domain.interactor.user.DiskGetMembership;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.domain.interactor.user.UpdateGroup;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupPresenter implements Presenter {

    private final GetGroupMembers getGroupMembers;
    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;
    private final AddMembersToGroup addMembersToGroup;
    private final JobManager jobManager;
    private final DiskGetMembership diskGetMembership;

    private GroupView groupView;

    @Inject
    GroupPresenter(JobManager jobManager,
                   GetGroupMembers getGroupMembers,
                   CreateGroup createGroup,
                   UpdateGroup updateGroup,
                   AddMembersToGroup addMembersToGroup,
                   DiskGetMembership diskGetMembership) {
        this.jobManager = jobManager;
        this.getGroupMembers = getGroupMembers;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.addMembersToGroup = addMembersToGroup;
        this.diskGetMembership = diskGetMembership;
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
    }

    @Override
    public void attachView(View v) {
        groupView = (GroupView) v;
    }

    @Override
    public void onCreate() {

    }

    public void membershipInfos(String membershipId) {
        diskGetMembership.prepare(membershipId);
        diskGetMembership.execute(new MembershipInfosSubscriber());
    }

    public void createGroup(NewGroupEntity newGroupEntity) {
        groupView.showLoading();
        createGroup.prepare(newGroupEntity);
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
//    public void updateGroup(String groupId, String groupName, String pictureUri) {
//        updateGroup.prepare(groupId, groupName, pictureUri);
//        updateGroup.execute(new UpdateGroupSubscriber());
//    }
//
//    public void addMembersToGroup(String groupId, List<String> memberIds) {
//        addMembersToGroup.prepare(groupId, memberIds);
//        addMembersToGroup.execute(new AddMembersToGroupSubscriber());
//    }
//
//    public void modifyPrivateGroupLink(String membershipId, boolean create) {
//        modifyPrivateGroupLink.prepare(membershipId, create);
//        modifyPrivateGroupLink.execute(new ModifyPrivateGroupSubscriber());
//    }
//
//    private final class GetGroupMemberSubscriber extends DefaultSubscriber<Group> {
//        @Override
//        public void onCompleted() {
//
//        }
//
//        @Override
//        public void onError(Throwable e) {
//            groupView.failedToGetMembers();
//            e.printStackTrace();
//        }
//
//        @Override
//        public void onNext(Group group) {
//            setupMembers(group);
//        }
//    }
}
