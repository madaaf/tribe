package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateGroup;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.domain.interactor.user.ModifyPrivateGroupLink;
import com.tribe.app.domain.interactor.user.UpdateGroup;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupPresenter implements Presenter {

    private final GetGroupMembers getGroupMembers;
    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;
    private final AddMembersToGroup addMembersToGroup;
    private final ModifyPrivateGroupLink modifyPrivateGroupLink;
    private final JobManager jobManager;

    private GroupView groupView;

    @Inject
    GroupPresenter(JobManager jobManager,
                   GetGroupMembers getGroupMembers,
                   CreateGroup createGroup,
                   UpdateGroup updateGroup,
                   AddMembersToGroup addMembersToGroup,
                   ModifyPrivateGroupLink modifyPrivateGroupLink) {
        this.jobManager = jobManager;
        this.getGroupMembers = getGroupMembers;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.addMembersToGroup = addMembersToGroup;
        this.modifyPrivateGroupLink = modifyPrivateGroupLink;
    }

    public void setupMembers(Group group) {
        this.groupView.setupGroup(group);
    }

    public void getGroupMembers(String groupId) {
        getGroupMembers.prepare(groupId);
        getGroupMembers.execute(new GetGroupMemberSubscriber());
    }

    public void createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri) {
        createGroup.prepare(groupName, memberIds, isPrivate, pictureUri);
        createGroup.execute(new CreateGroupSubscriber());
    }

    public void updateScore() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CREATE_GROUP, 1));
    }

    public void updateGroup(String groupId, String groupName, String pictureUri) {
        updateGroup.prepare(groupId, groupName, pictureUri);
        updateGroup.execute(new UpdateGroupSubscriber());
    }

    public void addMembersToGroup(String groupId, List<String> memberIds) {
        addMembersToGroup.prepare(groupId, memberIds);
        addMembersToGroup.execute(new AddMembersToGroupSubscriber());
    }

    public void modifyPrivateGroupLink(String membershipId, boolean create) {
        modifyPrivateGroupLink.prepare(membershipId, create);
        modifyPrivateGroupLink.execute(new ModifyPrivateGroupSubscriber());
    }

    private final class GetGroupMemberSubscriber extends DefaultSubscriber<Group> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            groupView.failedToGetMembers();
            e.printStackTrace();
        }

        @Override
        public void onNext(Group group) {
            setupMembers(group);
        }
    }

    private final class CreateGroupSubscriber extends DefaultSubscriber<Membership> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            groupView.groupCreationFailed();
            e.printStackTrace();
        }

        @Override
        public void onNext(Membership membership) {
            groupView.groupCreatedSuccessfully();
            groupView.setGroupId(membership.getGroup().getId());
            groupView.setMembershipId(membership.getId());
            boolean isPrivate = membership.getGroup().isPrivateGroup();
            String groupLink;
            if (isPrivate) groupLink = membership.getLink();
            else  groupLink = membership.getGroup().getGroupLink();
            groupView.setGroupLink(groupLink);
        }
    }

    private final class UpdateGroupSubscriber extends DefaultSubscriber<Group> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            groupView.groupUpdatedFailed();
        }

        @Override
        public void onNext(Group group) {
            groupView.groupUpdatedSuccessfully();
        }
    }

    private final class AddMembersToGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            groupView.memberAddedFailed();
        }

        @Override
        public void onNext(Void aVoid)
        {
            groupView.memberAddedSuccessfully();
        }
    }

    private final class ModifyPrivateGroupSubscriber extends DefaultSubscriber<Membership> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            groupView.linkCreationFailed();
            e.printStackTrace();
        }

        @Override
        public void onNext(Membership membership) {
            groupView.setGroupLink(membership.getLink());
            groupView.setGroupLinkExpirationDate(membership.getLink_expires_at());
        }
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
        modifyPrivateGroupLink.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        groupView = (GroupView) v;
    }

    @Override
    public void onCreate() {

    }

}
