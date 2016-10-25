package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.UpdateFriendshipJob;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.data.network.job.UpdateTribeDownloadedJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.ConfirmTribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.DiskMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.user.DiskUpdateFriendship;
import com.tribe.app.domain.interactor.user.DoBootstrapSupport;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveGroup;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends SendTribePresenter {

    private static final int PRELOAD_MAX = 5;

    // VIEW ATTACHED
    private HomeGridView homeGridView;

    // USECASES
    private GetDiskUserInfos diskUserInfosUsecase;
    private UseCaseDisk diskGetMessageReceivedListUsecase;
    private UseCaseDisk diskGetPendingTribeListUsecase;
    private DiskMarkTribeListAsRead diskMarkTribeListAsRead;
    private DiskUpdateFriendship diskUpdateFriendship;
    private final LeaveGroup leaveGroup;
    private final RemoveGroup removeGroup;
    private final DoBootstrapSupport doBootstrapSupport;
    private final UseCaseDisk diskUpdateTribesReceivedToNotSeen;

    // SUBSCRIBERS
    private UpdateTribesReceivedToNotSeenSubscriber updateTribesReceivedToNotSeenSubscriber;
    private TribePendingListSubscriber tribePendingListSubscriber;
    private FriendListSubscriber friendListSubscriber;
    private BootstrapSupportSubscriber bootstrapSupportSubscriber;
    private MessageReceivedListSubscriber messageReceivedListSubscriber;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("jobManagerDownload") JobManager jobManagerDownload,
                             @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("diskConfirmTribe") ConfirmTribe confirmTribe,
                             @Named("diskGetReceivedMessages") UseCaseDisk diskGetReceivedMessageList,
                             @Named("diskGetPendingTribes") UseCaseDisk diskGetPendingTribeList,
                             @Named("diskMarkTribeListAsRead") DiskMarkTribeListAsRead diskMarkTribeListAsRead,
                             LeaveGroup leaveGroup,
                             RemoveGroup removeGroup,
                             DiskUpdateFriendship diskUpdateFriendship,
                             DoBootstrapSupport bootstrapSupport,
                             @Named("diskUpdateMessagesReceivedToNotSeen") UseCaseDisk diskUpdateTribesReceivedToNotSeen) {
        super(jobManager, jobManagerDownload, diskSaveTribe, diskDeleteTribe, confirmTribe);
        this.diskUserInfosUsecase = diskUserInfos;
        this.diskGetMessageReceivedListUsecase = diskGetReceivedMessageList;
        this.diskGetPendingTribeListUsecase = diskGetPendingTribeList;
        this.diskMarkTribeListAsRead = diskMarkTribeListAsRead;
        this.leaveGroup = leaveGroup;
        this.removeGroup = removeGroup;
        this.diskUpdateFriendship = diskUpdateFriendship;
        this.doBootstrapSupport = bootstrapSupport;
        this.diskUpdateTribesReceivedToNotSeen = diskUpdateTribesReceivedToNotSeen;
    }

    @Override
    public void onCreate() {
        jobManager.addJobInBackground(new UpdateTribeDownloadedJob());
        jobManager.addJobInBackground(new UpdateTribesErrorStatusJob());
        jobManager.addJobInBackground(new UpdateUserJob());
        onResume();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        reload();
        loadTribeList();
        loadPendingTribeList();
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
        super.onPause();
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
        diskGetMessageReceivedListUsecase.unsubscribe();
        diskGetPendingTribeListUsecase.unsubscribe();
        diskMarkTribeListAsRead.unsubscribe();
        leaveGroup.unsubscribe();
        removeGroup.unsubscribe();
        diskUpdateTribesReceivedToNotSeen.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onPause();
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
    }

    public void reload() {
        if (updateTribesReceivedToNotSeenSubscriber != null) {
            updateTribesReceivedToNotSeenSubscriber.unsubscribe();
        }

        updateTribesReceivedToNotSeenSubscriber = new UpdateTribesReceivedToNotSeenSubscriber();
        diskUpdateTribesReceivedToNotSeen.execute(updateTribesReceivedToNotSeenSubscriber);
    }

    public void loadFriendList(String filter) {
        showViewLoading();

        if (friendListSubscriber != null) {
            friendListSubscriber.unsubscribe();
        }

        friendListSubscriber = new FriendListSubscriber();
        diskUserInfosUsecase.prepare(null, filter);
        diskUserInfosUsecase.execute(friendListSubscriber);
    }

    public void loadTribeList() {
        jobManager.addJobInBackground(new UpdateMessagesJob());

        if (messageReceivedListSubscriber != null) {
            messageReceivedListSubscriber.unsubscribe();
        }

        messageReceivedListSubscriber = new MessageReceivedListSubscriber();
        diskGetMessageReceivedListUsecase.execute(messageReceivedListSubscriber);
    }

    public void loadPendingTribeList() {
        if (tribePendingListSubscriber == null) {
            tribePendingListSubscriber = new TribePendingListSubscriber();
        }

        diskGetPendingTribeListUsecase.execute(tribePendingListSubscriber);
    }

    private void showFriendCollectionInView(List<Recipient> recipientList) {
        this.homeGridView.renderRecipientList(recipientList);
    }

    private void updateReceivedMessageList(List<Message> messageList) {
        this.homeGridView.updateReceivedMessages(messageList);
    }

    private void updatePendingTribes(List<TribeMessage> pendingTribes) {
        this.homeGridView.updatePendingTribes(pendingTribes);
    }

    public void markTribeListAsRead(Recipient recipient, List<TribeMessage> tribeMessageList) {
        jobManager.addJobInBackground(new MarkTribeListAsReadJob(recipient, tribeMessageList));
    }

    public void updateFriendship(Friendship friendship, @FriendshipRealm.FriendshipStatus String status) {
        diskUpdateFriendship.prepare(friendship.getId(), status);
        diskUpdateFriendship.execute(new UpdateFriendshipSubscriber());
        jobManager.addJobInBackground(new UpdateFriendshipJob(friendship.getId(), status));
    }

    public void leaveGroup(String membershipId) {
        leaveGroup.prepare(membershipId);
        leaveGroup.execute(new LeaveGroupSubscriber());
    }

    public void removeGroup(String groupId) {
        removeGroup.prepare(groupId);
        removeGroup.execute(new RemoveGroupSubscriber());
    }

    public void boostrapSupport() {
        if (bootstrapSupportSubscriber != null)
            bootstrapSupportSubscriber.unsubscribe();

        bootstrapSupportSubscriber = new BootstrapSupportSubscriber();
        doBootstrapSupport.execute(bootstrapSupportSubscriber);
    }

    @Override
    protected SendTribeView getView() {
        return homeGridView;
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(User user) {
            List<Recipient> recipients = user.getFriendshipList();
            Friendship recipient = new Friendship(user.getId());
            recipient.setFriend(user);
            recipients.add(0, recipient);
            showFriendCollectionInView(recipients);
        }
    }

    private final class MessageReceivedListSubscriber extends DefaultSubscriber<List<Message>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(List<Message> messageList) {
            updateReceivedMessageList(messageList);
        }
    }

    private final class TribePendingListSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<TribeMessage> tribes) {
            updatePendingTribes(tribes);
        }
    }

    private final class LeaveGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            homeGridView.refreshGrid();
        }
    }

    private final class RemoveGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            homeGridView.refreshGrid();
        }
    }

    private final class UpdateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Friendship friendship) {
            homeGridView.onFriendshipUpdated(friendship);
        }
    }

    private class BootstrapSupportSubscriber extends DefaultSubscriber<Void> {

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

    private class UpdateTribesReceivedToNotSeenSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            loadFriendList(null);
        }
    }
}
