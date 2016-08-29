package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.UpdateMessagesJob;
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
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

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
    private UseCaseDisk diskUserInfosUsecase;
    private UseCaseDisk diskGetMessageReceivedListUsecase;
    private UseCaseDisk diskGetPendingTribeListUsecase;
    private DiskMarkTribeListAsRead diskMarkTribeListAsRead;

    // SUBSCRIBERS
    private TribePendingListSubscriber tribePendingListSubscriber;
    private FriendListSubscriber friendListSubscriber;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("diskUserInfos") UseCaseDisk diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("diskGetReceivedMessages") UseCaseDisk diskGetReceivedMessageList,
                             @Named("diskGetPendingTribes") UseCaseDisk diskGetPendingTribeList,
                             @Named("diskMarkTribeListAsRead") DiskMarkTribeListAsRead diskMarkTribeListAsRead) {
        super(jobManager, diskSaveTribe, diskDeleteTribe);
        this.diskUserInfosUsecase = diskUserInfos;
        this.diskGetMessageReceivedListUsecase = diskGetReceivedMessageList;
        this.diskGetPendingTribeListUsecase = diskGetPendingTribeList;
        this.diskMarkTribeListAsRead = diskMarkTribeListAsRead;
    }

    @Override
    public void onCreate() {
        jobManager.addJobInBackground(new UpdateTribesErrorStatusJob());
        jobManager.addJobInBackground(new UpdateUserJob());
        loadFriendList();
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
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
    }

    public void loadFriendList() {
        showViewLoading();

        if (friendListSubscriber != null) {
            friendListSubscriber.unsubscribe();
        }

        friendListSubscriber = new FriendListSubscriber();
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
        Observable
            .just("")
            .doOnNext(o -> {
                int countPreload = 0;

                for (Message message : messageList) {
                    if (message instanceof TribeMessage && message.getMessageDownloadingStatus() != null
                            && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_DOWNLOADING)
                            && jobManager.getJobStatus(message.getLocalId()).equals(JobStatus.UNKNOWN)) {
                        message.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                    }

                    if (message instanceof TribeMessage && message.getMessageDownloadingStatus() != null
                            && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
                            && message.getFrom() != null) {
                        countPreload++;
                        jobManager.addJobInBackground(new DownloadTribeJob((TribeMessage) message));
                    }

                    if (countPreload == PRELOAD_MAX) break;
                }
            }).subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    public void markTribeListAsRead(Recipient recipient) {
        diskMarkTribeListAsRead.setTribeList(recipient.getReceivedTribes());
        diskMarkTribeListAsRead.execute(new DefaultSubscriber<>());
        jobManager.addJobInBackground(new MarkTribeListAsReadJob(recipient, recipient.getReceivedTribes()));
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
}
