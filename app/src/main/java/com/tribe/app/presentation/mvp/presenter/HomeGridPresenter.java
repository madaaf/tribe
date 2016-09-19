package com.tribe.app.presentation.mvp.presenter;

import android.Manifest;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.TagConstraint;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.data.network.job.UpdateTribeDownloadedJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.DiskMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveGroup;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeGridPresenter extends SendTribePresenter implements Presenter {

    private static final int PRELOAD_MAX = 5;

    // VIEW ATTACHED
    private HomeGridView homeGridView;

    // USECASES
    private GetDiskUserInfos diskUserInfosUsecase;
    private UseCaseDisk diskGetMessageReceivedListUsecase;
    private UseCaseDisk diskGetPendingTribeListUsecase;
    private DiskMarkTribeListAsRead diskMarkTribeListAsRead;
    private final LeaveGroup leaveGroup;
    private final RemoveGroup removeGroup;

    // SUBSCRIBERS
    private TribePendingListSubscriber tribePendingListSubscriber;
    private FriendListSubscriber friendListSubscriber;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("diskGetReceivedMessages") UseCaseDisk diskGetReceivedMessageList,
                             @Named("diskGetPendingTribes") UseCaseDisk diskGetPendingTribeList,
                             @Named("diskMarkTribeListAsRead") DiskMarkTribeListAsRead diskMarkTribeListAsRead,
                             LeaveGroup leaveGroup,
                             RemoveGroup removeGroup) {
        super(jobManager, diskSaveTribe, diskDeleteTribe);
        this.diskUserInfosUsecase = diskUserInfos;
        this.diskGetMessageReceivedListUsecase = diskGetReceivedMessageList;
        this.diskGetPendingTribeListUsecase = diskGetPendingTribeList;
        this.diskMarkTribeListAsRead = diskMarkTribeListAsRead;
        this.leaveGroup = leaveGroup;
        this.removeGroup = removeGroup;
    }

    @Override
    public void onCreate() {
        jobManager.addJobInBackground(new UpdateTribeDownloadedJob());
        jobManager.addJobInBackground(new UpdateTribesErrorStatusJob());
        jobManager.addJobInBackground(new UpdateUserJob());
        loadFriendList(null);
        loadTribeList();
        loadPendingTribeList();
    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
        diskGetMessageReceivedListUsecase.unsubscribe();
        diskGetPendingTribeListUsecase.unsubscribe();
        diskMarkTribeListAsRead.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
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
        diskGetMessageReceivedListUsecase.execute(new MessageReceivedListSubscriber());
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
        downloadMessages(messageList);
        this.homeGridView.updateReceivedMessages(messageList);
    }

    private void updatePendingTribes(List<TribeMessage> pendingTribes) {
        this.homeGridView.updatePendingTribes(pendingTribes);
    }

    public void downloadMessages(List<Message> messageList) {
        if (RxPermissions.getInstance(homeGridView.context()).isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Observable
                .just("")
                .doOnNext(o -> {
                    for (Message message : messageList) {
                        if (message instanceof TribeMessage) {
                            boolean shouldDownload = false;

                            JobStatus jobStatus = jobManager.getJobStatus(message.getLocalId());
                            File file = FileUtils.getFileEnd(message.getId());

                            if (jobStatus.equals(JobStatus.UNKNOWN) && (!file.exists() || file.length() == 0)
                                    && (message.getMessageDownloadingStatus() == null || message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD))) {
                                shouldDownload = true;
                                message.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                                jobManager.cancelJobsInBackground(null, TagConstraint.ALL, message.getId());
                            }

                            if (shouldDownload
                                    && message.getMessageDownloadingStatus() != null
                                    && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
                                    && message.getFrom() != null) {
                                jobManager.addJobInBackground(new DownloadTribeJob((TribeMessage) message));
                            }
                        }
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        }
    }

    public void markTribeListAsRead(Recipient recipient) {
        diskMarkTribeListAsRead.setTribeList(recipient.getReceivedTribes());
        diskMarkTribeListAsRead.execute(new DefaultSubscriber<>());
        jobManager.addJobInBackground(new MarkTribeListAsReadJob(recipient, recipient.getReceivedTribes()));
    }


    public void leaveGroup(String groupId) {
        leaveGroup.prepare(groupId);
        leaveGroup.execute(new LeaveGroupSubscriber());
    }

    public void removeGroup(String groupId) {
        removeGroup.prepare(groupId);
        removeGroup.execute(new RemoveGroupSubscriber());
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
}
