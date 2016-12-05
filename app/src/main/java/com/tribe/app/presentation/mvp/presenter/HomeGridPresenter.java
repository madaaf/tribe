package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.UpdateFriendshipJob;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.network.job.UpdateTribeDownloadedJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.ConfirmTribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.user.CreateMembership;
import com.tribe.app.domain.interactor.user.DiskUpdateFriendship;
import com.tribe.app.domain.interactor.user.DoBootstrapSupport;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetHeadDeepLink;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveGroup;
import com.tribe.app.domain.interactor.user.SendOnlineNotification;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends SendTribePresenter {

    // VIEW ATTACHED
    private HomeGridView homeGridView;

    // USECASES
    private GetDiskUserInfos diskUserInfosUsecase;
    private UseCaseDisk diskGetMessageReceivedListUsecase;
    private UseCaseDisk diskGetPendingTribeListUsecase;
    private DiskUpdateFriendship diskUpdateFriendship;
    private LeaveGroup leaveGroup;
    private RemoveGroup removeGroup;
    private DoBootstrapSupport doBootstrapSupport;
    private UseCaseDisk diskUpdateTribesReceivedToNotSeen;
    private SendToken sendTokenUseCase;
    private GetHeadDeepLink getHeadDeepLink;
    private CreateMembership createMembership;
    private UseCase cloudUserInfos;
    private UseCase cloudGetMessages;
    private SendOnlineNotification sendOnlineNotification;

    // SUBSCRIBERS
    private UpdateTribesReceivedToNotSeenSubscriber updateTribesReceivedToNotSeenSubscriber;
    private TribePendingListSubscriber tribePendingListSubscriber;
    private FriendListSubscriber diskFriendListSubscriber;
    private FriendListSubscriber cloudFriendListSubscriber;
    private BootstrapSupportSubscriber bootstrapSupportSubscriber;
    private MessageReceivedListSubscriber messageReceivedListSubscriber;
    private CloudMessageListSubscriber cloudMessageListSubscriber;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("jobManagerDownload") JobManager jobManagerDownload,
                             @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("diskConfirmTribe") ConfirmTribe confirmTribe,
                             @Named("diskGetReceivedMessages") UseCaseDisk diskGetReceivedMessageList,
                             @Named("diskGetPendingTribes") UseCaseDisk diskGetPendingTribeList,
                             LeaveGroup leaveGroup,
                             RemoveGroup removeGroup,
                             DiskUpdateFriendship diskUpdateFriendship,
                             DoBootstrapSupport bootstrapSupport,
                             @Named("diskUpdateMessagesReceivedToNotSeen") UseCaseDisk diskUpdateTribesReceivedToNotSeen,
                             @Named("sendToken") SendToken sendToken,
                             GetHeadDeepLink getHeadDeepLink,
                             CreateMembership createMembership,
                             @Named("cloudUserInfos") UseCase cloudUserInfos,
                             @Named("cloudGetMessages") UseCase cloudGetMessages,
                             SendOnlineNotification sendOnlineNotification) {
        super(jobManager, jobManagerDownload, diskSaveTribe, diskDeleteTribe, confirmTribe);
        this.diskUserInfosUsecase = diskUserInfos;
        this.diskGetMessageReceivedListUsecase = diskGetReceivedMessageList;
        this.diskGetPendingTribeListUsecase = diskGetPendingTribeList;
        this.leaveGroup = leaveGroup;
        this.removeGroup = removeGroup;
        this.diskUpdateFriendship = diskUpdateFriendship;
        this.doBootstrapSupport = bootstrapSupport;
        this.diskUpdateTribesReceivedToNotSeen = diskUpdateTribesReceivedToNotSeen;
        this.sendTokenUseCase = sendToken;
        this.getHeadDeepLink = getHeadDeepLink;
        this.createMembership = createMembership;
        this.cloudUserInfos = cloudUserInfos;
        this.cloudGetMessages = cloudGetMessages;
        this.sendOnlineNotification = sendOnlineNotification;
    }

    @Override
    public void onCreate() {
        jobManager.addJobInBackground(new UpdateTribeDownloadedJob());
        jobManager.addJobInBackground(new UpdateTribesErrorStatusJob());
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
        sendTokenUseCase.unsubscribe();
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
        diskGetMessageReceivedListUsecase.unsubscribe();
        diskGetPendingTribeListUsecase.unsubscribe();
        leaveGroup.unsubscribe();
        removeGroup.unsubscribe();
        diskUpdateTribesReceivedToNotSeen.unsubscribe();
        getHeadDeepLink.unsubscribe();
        createMembership.unsubscribe();
        cloudGetMessages.unsubscribe();
        cloudUserInfos.unsubscribe();
        sendOnlineNotification.unsubscribe();
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
        showViewLoading();

        if (updateTribesReceivedToNotSeenSubscriber != null) {
            updateTribesReceivedToNotSeenSubscriber.unsubscribe();
        }

        updateTribesReceivedToNotSeenSubscriber = new UpdateTribesReceivedToNotSeenSubscriber();
        diskUpdateTribesReceivedToNotSeen.execute(updateTribesReceivedToNotSeenSubscriber);
    }

    public void loadFriendList(String filter) {
        if (diskFriendListSubscriber != null) {
            diskFriendListSubscriber.unsubscribe();
        }

        diskFriendListSubscriber = new FriendListSubscriber(false);
        diskUserInfosUsecase.prepare(null, filter);
        diskUserInfosUsecase.execute(diskFriendListSubscriber);
    }

    public void syncFriendList() {
        if (cloudFriendListSubscriber != null) {
            cloudFriendListSubscriber.unsubscribe();
        }

        cloudFriendListSubscriber = new FriendListSubscriber(true);
        cloudUserInfos.execute(cloudFriendListSubscriber);
    }

    public void loadTribeList() {
        if (messageReceivedListSubscriber != null) {
            messageReceivedListSubscriber.unsubscribe();
        }

        messageReceivedListSubscriber = new MessageReceivedListSubscriber();
        diskGetMessageReceivedListUsecase.execute(messageReceivedListSubscriber);
    }

    public void syncTribeList() {
        if (cloudMessageListSubscriber != null) {
            cloudMessageListSubscriber.unsubscribe();
        }

        cloudMessageListSubscriber = new CloudMessageListSubscriber();
        cloudGetMessages.execute(cloudMessageListSubscriber);
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

    public void getHeadDeepLink(String url) {
        getHeadDeepLink.prepare(url);
        getHeadDeepLink.execute(new GetHeadDeepLinkSubscriber());
    }

    public void createMembership(String groupId) {
        createMembership.setGroupId(groupId);
        createMembership.execute(new CreateMembershipSubscriber());
    }

    public void updateScoreCamera() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CAMERA, 1));
    }

    public void sendToken(String token) {
        sendTokenUseCase.setToken(token);
        sendTokenUseCase.execute(new SendTokenSubscriber());
    }

    public void sendOnlineNotification() {
        sendOnlineNotification.execute(new DefaultSubscriber());
    }

    @Override
    protected SendTribeView getView() {
        return homeGridView;
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        private boolean cloud = false;

        public FriendListSubscriber(boolean cloud) {
            this.cloud = cloud;
        }

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            if (cloud) showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(User user) {
            if (!cloud) {
                List<Recipient> recipients = user.getFriendshipList();
                showFriendCollectionInView(recipients);
            } else {
                syncTribeList();
            }
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

    private final class CloudMessageListSubscriber extends DefaultSubscriber<List<Message>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<Message> messageList) {
            hideViewLoading();
        }
    }

    private final class TribePendingListSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<TribeMessage> tribes) {
            updatePendingTribes(tribes);
        }
    }

    private final class LeaveGroupSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {}

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
        public void onCompleted() {}

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
        public void onCompleted() {}

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
        public void onCompleted() {}

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
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            loadFriendList(null);
            syncFriendList();
        }
    }

    private final class GetHeadDeepLinkSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(String url) {
            if (!StringUtils.isEmpty(url)) homeGridView.onDeepLink(url);
        }
    }

    private final class CreateMembershipSubscriber extends DefaultSubscriber<Membership> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Membership membership) {
            homeGridView.onMembershipCreated(membership);
        }
    }

    private final class SendTokenSubscriber extends DefaultSubscriber<Installation> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(Installation installation) {
        }
    }
}
