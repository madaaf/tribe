package com.tribe.app.presentation.mvp.presenter;

import android.os.Parcelable;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.AddAdminsToGroup;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.RemoveAdminsFromGroup;
import com.tribe.app.domain.interactor.user.RemoveFriendship;
import com.tribe.app.domain.interactor.user.RemoveMembersFromGroup;
import com.tribe.app.presentation.mvp.view.GroupMemberView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMemberPresenter implements Presenter {

    private final RemoveMembersFromGroup removeMembersFromGroup;
    private final RemoveFriendship removeFriendship;
    private final CreateFriendship createFriendship;
    private final AddAdminsToGroup addAdminsToGroup;
    private final RemoveAdminsFromGroup removeAdminsFromGroup;
    // TODO: add block member

    private GroupMemberView groupMemberView;

    @Inject
    GroupMemberPresenter(RemoveMembersFromGroup removeMembersFromGroup,
                         RemoveFriendship removeFriendship,
                         CreateFriendship createFriendship,
                         AddAdminsToGroup addAdminsToGroup,
                         RemoveAdminsFromGroup removeAdminsFromGroup) {
        this.removeMembersFromGroup = removeMembersFromGroup;
        this.removeFriendship = removeFriendship;
        this.createFriendship = createFriendship;
        this.addAdminsToGroup = addAdminsToGroup;
        this.removeAdminsFromGroup = removeAdminsFromGroup;
    }

    public void removeMembersFromGroup(String groupId, List<String> memberIds) {
        removeMembersFromGroup.prepare(groupId, memberIds);
        removeMembersFromGroup.execute(new RemoveMemberFromGroupSubscriber());
    }

    public void removeFriendship(String friendshipId) {
        removeFriendship.setFriendshipId(friendshipId);
        removeFriendship.execute(new RemoveFriendshipSubscriber());
    }

    public void createFriendship(String userId) {
        createFriendship.setUserId(userId);
        createFriendship.execute(new CreateFriendshipSubscriber());
    }

    public void addAdminsToGroup(String groupId, List<String> memberIds) {
        addAdminsToGroup.prepare(groupId, memberIds);
        addAdminsToGroup.execute(new AddAdminsToGroupSubscriber());
    }

    public void removeAdminsFromGroup(String groupId, List<String> memberIds) {
        removeAdminsFromGroup.prepare(groupId, memberIds);
        removeAdminsFromGroup.execute(new RemoveAdminsFromGroupSubscriber());
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
        groupMemberView = (GroupMemberView) v;
    }

    @Override
    public void onCreate() {

    }

    private final class RemoveMemberFromGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            groupMemberView.removeMember();
        }
    }

    private final class RemoveFriendshipSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            groupMemberView.removeFriend();
        }
    }

    private final class CreateFriendshipSubscriber extends DefaultSubscriber<Friendship> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Friendship friendship) {
            groupMemberView.createFriendship();
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
            groupMemberView.setAdmin();
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
            groupMemberView.removeAdmin();
        }
    }
}
