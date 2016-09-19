package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddAdminsToGroup;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateGroup;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveAdminsFromGroup;
import com.tribe.app.domain.interactor.user.RemoveGroup;
import com.tribe.app.domain.interactor.user.RemoveMembersFromGroup;
import com.tribe.app.domain.interactor.user.UpdateGroup;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;

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
    private final RemoveMembersFromGroup removeMembersFromGroup;
    private final AddAdminsToGroup addAdminsToGroup;
    private final RemoveAdminsFromGroup removeAdminsFromGroup;


    private GroupView groupView;

    @Inject
    GroupPresenter(GetGroupMembers getGroupMembers, CreateGroup createGroup, UpdateGroup updateGroup,
                   AddMembersToGroup addMembersToGroup, RemoveMembersFromGroup removeMembersFromGroup,
                   AddAdminsToGroup addAdminsToGroup, RemoveAdminsFromGroup removeAdminsFromGroup) {
        this.getGroupMembers = getGroupMembers;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.addMembersToGroup = addMembersToGroup;
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

    }

    @Override
    public void attachView(View v) {
        groupView = (GroupView) v;
    }

    @Override
    public void onCreate() {

    }

    public void setupMembers(Group group) {
        this.groupView.setupGroup(group);
    }
    public void backToHome() {
        this.groupView.backToHome();
    }

    public void getGroupMembers(String groupId) {
        getGroupMembers.prepare(groupId);
        getGroupMembers.execute(new GetGroupMemberSubscriber());
    }

    public void createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri) {
        createGroup.prepare(groupName, memberIds, isPrivate, pictureUri);
        createGroup.execute(new CreateGroupSubscriber());
    }

    public void updateGroup(String groupId, String groupName, String pictureUri) {
        updateGroup.prepare(groupId, groupName, pictureUri);
        updateGroup.execute(new UpdateGroupSubscriber());
    }

    public void addMembersToGroup(String groupId, List<String> memberIds) {
        addMembersToGroup.prepare(groupId, memberIds);
        addMembersToGroup.execute(new AddMembersToGroupSubscriber());
    }

    public void removeMembersFromGroup(String groupId, List<String> memberIds) {
        removeMembersFromGroup.prepare(groupId, memberIds);
        removeMembersFromGroup.execute(new RemoveMembersFromGroupSubscriber());
    }

    public void addAdminsToGroup(String groupId, List<String> memberIds) {
        addAdminsToGroup.prepare(groupId, memberIds);
        addAdminsToGroup.execute(new AddAdminsToGroupSubscriber());
    }

    public void removeAdminsFromGroup(String groupId, List<String> memberIds) {
        removeAdminsFromGroup.prepare(groupId, memberIds);
        removeAdminsFromGroup.execute(new RemoveAdminsFromGroupSubscriber());
    }



    private final class GetGroupMemberSubscriber extends DefaultSubscriber<Group> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Group group) {
            setupMembers(group);
        }
    }

    private final class CreateGroupSubscriber extends DefaultSubscriber<Group> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Group group) {
            backToHome();
        }
    }

    private final class UpdateGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {

        }
    }

    private final class AddMembersToGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            backToHome();
        }
    }

    private final class RemoveMembersFromGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {

        }
    }

    private final class AddAdminsToGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {

        }
    }

    private final class RemoveAdminsFromGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {

        }
    }
}
