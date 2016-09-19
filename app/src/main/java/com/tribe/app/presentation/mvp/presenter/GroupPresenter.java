package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateGroup;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
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

    private GroupView groupView;

    @Inject
    GroupPresenter(GetGroupMembers getGroupMembers, CreateGroup createGroup, UpdateGroup updateGroup, AddMembersToGroup addMembersToGroup) {
        this.getGroupMembers = getGroupMembers;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.addMembersToGroup = addMembersToGroup;
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

}
