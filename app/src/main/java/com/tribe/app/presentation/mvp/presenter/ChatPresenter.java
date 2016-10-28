package com.tribe.app.presentation.mvp.presenter;

import android.Manifest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.TagConstraint;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.data.network.job.DeleteMessageJob;
import com.tribe.app.data.network.job.DownloadChatVideoJob;
import com.tribe.app.data.network.job.MarkMessageListAsReadJob;
import com.tribe.app.data.network.job.SendChatJob;
import com.tribe.app.data.network.job.UpdateChatHistoryJob;
import com.tribe.app.data.network.job.UpdateChatMessagesJob;
import com.tribe.app.data.network.job.UpdateMessagesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateMessagesVideoErrorStatusJob;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.ConnectAndSubscribeMQTT;
import com.tribe.app.domain.interactor.text.DeleteDiskConversation;
import com.tribe.app.domain.interactor.text.DisconnectMQTT;
import com.tribe.app.domain.interactor.text.DiskMarkMessageListAsRead;
import com.tribe.app.domain.interactor.text.GetDiskChatMessageList;
import com.tribe.app.domain.interactor.text.GetPendingMessageList;
import com.tribe.app.domain.interactor.text.SubscribingMQTT;
import com.tribe.app.domain.interactor.text.UnsubscribeMQTT;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChatPresenter implements Presenter {

    private final User currentUser;
    private final JobManager jobManager;
    private final ConnectAndSubscribeMQTT connectAndSubscribeMQTT;
    private final SubscribingMQTT subscribingMQTT;
    private final DisconnectMQTT disconnectMQTT;
    private final UnsubscribeMQTT unsubscribeMQTT;
    private final GetDiskChatMessageList diskGetChatMessages;
    private final DeleteDiskConversation deleteDiskConversation;
    private final DiskMarkMessageListAsRead diskMarkMessageListAsRead;
    private final GetPendingMessageList getPendingMessageList;
    private final GetRecipientInfos getRecipientInfos;

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private DeleteMessageSubscriber deleteMessageSubscriber;

    private MessageView messageView;

    private String friendId;

    @Inject
    public ChatPresenter(User currentUser,
                         JobManager jobManager,
                         @Named("getPendingMessageList") GetPendingMessageList getPendingMessageList,
                         @Named("diskGetChatMessages") GetDiskChatMessageList diskGetChatMessages,
                         @Named("deleteDiskConversation") DeleteDiskConversation deleteDiskConversation,
                         @Named("diskMarkMessageListAsRead") DiskMarkMessageListAsRead diskMarkMessageListAsRead,
                         @Named("connectAndSubscribe") ConnectAndSubscribeMQTT connectAndSubscribeMQTT,
                         @Named("subscribing") SubscribingMQTT subscribingMQTT,
                         @Named("disconnect") DisconnectMQTT disconnectMQTT,
                         @Named("unsubscribe") UnsubscribeMQTT unsubscribeMQTT,
                         GetRecipientInfos getRecipientInfos) {
        this.currentUser = currentUser;
        this.jobManager = jobManager;
        this.connectAndSubscribeMQTT = connectAndSubscribeMQTT;
        this.subscribingMQTT = subscribingMQTT;
        this.unsubscribeMQTT = unsubscribeMQTT;
        this.disconnectMQTT = disconnectMQTT;
        this.diskGetChatMessages = diskGetChatMessages;
        this.deleteDiskConversation = deleteDiskConversation;
        this.diskMarkMessageListAsRead = diskMarkMessageListAsRead;
        this.getPendingMessageList = getPendingMessageList;
        this.getRecipientInfos = getRecipientInfos;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        //disconnectMQTT.execute(new DisconnectMQTTSubscriber());
    }

    @Override
    public void onPause() {
        // Unused
    }

    @Override
    public void onDestroy() {
//        connectAndSubscribeMQTT.unsubscribe();
//        subscribingMQTT.unsubscribe();
//        disconnectMQTT.unsubscribe();
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        diskGetChatMessages.unsubscribe();
        deleteDiskConversation.unsubscribe();
        diskMarkMessageListAsRead.unsubscribe();
        getPendingMessageList.unsubscribe();
        getRecipientInfos.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        messageView = (MessageView) v;
    }

    public void updateErrorMessages(String recipientId) {
        jobManager.addJobInBackground(new UpdateMessagesErrorStatusJob(recipientId));
        jobManager.addJobInBackground(new UpdateMessagesVideoErrorStatusJob(recipientId));
    }

    public void getRecipient(String recipientId, boolean isToGroup) {
        getRecipientInfos.prepare(recipientId, isToGroup);
        getRecipientInfos.execute(new GetRecipientInfosSubscriber());
    }

    public void loadChatMessages(Recipient recipient) {
        jobManager.addJobInBackground(new UpdateChatHistoryJob(recipient));
        jobManager.addJobInBackground(new UpdateChatMessagesJob(recipient.getSubId()));
        diskGetChatMessages.setRecipientId(recipient.getSubId());
        diskGetChatMessages.execute(new ChatMessageListSubscriber());
        getPendingMessageList.setRecipientId(recipient.getSubId());
        getPendingMessageList.execute(new PendingChatMessageListSubscriber());
    }

    public void loadThumbnail(int radius) {
        subscriptions.add(
                Observable.create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(final Subscriber<? super Bitmap> subscriber) {
                        final String[] columns = {MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media._ID};
                        final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC limit 1";

                        Cursor imageCursor = messageView.context().getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                                null, orderBy);

                        int image_column_index = imageCursor
                                .getColumnIndex(MediaStore.Images.Media._ID);

                        Bitmap thumbnail = null;

                        if (imageCursor != null && imageCursor.moveToFirst()) {
                            int id = imageCursor.getInt(image_column_index);
                            thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                                    messageView.context().getApplicationContext().getContentResolver(), id,
                                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
                        }

                        imageCursor.close();

                        if (thumbnail != null) {
                            RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL);
                            subscriber.onNext(roundedCornersTransformation.transform(thumbnail));
                        }

                        subscriber.onCompleted();
                    }
                }).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            messageView.showGalleryImage(bitmap);
                        }));
    }

    public void deleteConversation(String friendshipId) {
        deleteDiskConversation.setFriendshipId(friendshipId);
        deleteDiskConversation.execute(new DeleteConversationSubscriber());
    }

    public void loadVideo(ChatMessage message) {
        if (RxPermissions.getInstance(messageView.context()).isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Observable
                    .just("")
                    .doOnNext(o -> {
                        boolean shouldDownload = false;

                        JobStatus jobStatus = jobManager.getJobStatus(message.getLocalId());

                        if (jobStatus.equals(JobStatus.UNKNOWN)) {
                            shouldDownload = true;
                            message.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                            jobManager.cancelJobsInBackground(null, TagConstraint.ALL, message.getId());
                        }

                        if (shouldDownload
                                && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
                                && message.getFrom() != null) {
                            jobManager.addJobInBackground(new DownloadChatVideoJob(message));
                        }
                    }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    public void subscribe(String id) {
        friendId = id;
        connectAndSubscribeMQTT.setTopic("chats/" + id + "/#");
        connectAndSubscribeMQTT.execute(new ConnectAndSubscribeMQTTSubscriber());
    }

    public void sendMessage(ChatMessage... messageList) {
        for (ChatMessage chatMessage : messageList)
            jobManager.addJobInBackground(new SendChatJob(chatMessage));
    }

    public void deleteMessage(ChatMessage... messageList) {
        jobManager.addJobInBackground(new DeleteMessageJob(messageList));
    }

    public void sendTypingEvent() {
    }

    public void markMessageListAsRead(Recipient recipient, List<ChatMessage> messageList) {
        List<ChatMessage> onlyNonRead = new ArrayList<>();

        for (ChatMessage message : messageList) {
            if (!message.getFrom().equals(currentUser) && message.getMessageReceivingStatus() != null
                    && !message.getMessageReceivingStatus().equals(MessageReceivingStatus.STATUS_SEEN)) {
                onlyNonRead.add(message);
            }
        }

        if (onlyNonRead.size() > 0) {
            diskMarkMessageListAsRead.setMessageList(onlyNonRead);
            diskMarkMessageListAsRead.execute(new DefaultSubscriber<>());
            jobManager.addJobInBackground(new MarkMessageListAsReadJob(recipient, onlyNonRead));
        }
    }

    private final class ConnectAndSubscribeMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
            subscribingMQTT.setTopic("^chats/" + friendId + ".*");
            subscribingMQTT.execute(new ListenSubscribingMQTTSubscriber());
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
        }
    }

    private final class ListenSubscribingMQTTSubscriber extends DefaultSubscriber<List<ChatMessage>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<com.tribe.app.domain.entity.ChatMessage> chatMessageList) {
            messageView.renderMessageList(chatMessageList);
        }
    }

    private final class DisconnectMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
        }
    }

    private final class ChatMessageListSubscriber extends DefaultSubscriber<List<ChatMessage>> {
        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<ChatMessage> chatMessageList) {
            messageView.renderMessageList(chatMessageList);
        }
    }

    private final class PendingChatMessageListSubscriber extends DefaultSubscriber<List<ChatMessage>> {
        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<ChatMessage> chatMessagePendingList) {
            messageView.renderPendingMessages(chatMessagePendingList);
            //sendMessage(chatMessageList.toArray(new ChatMessage[chatMessageList.size()]));
        }
    }

    private final class DeleteConversationSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }
    }

    private final class GetRecipientInfosSubscriber extends DefaultSubscriber<Recipient> {
        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Recipient recipient) {
            messageView.onRecipientLoaded(recipient);
        }
    }

    private final class DeleteMessageSubscriber extends DefaultSubscriber<Void> {
        @Override
        public void onCompleted() {
            super.onCompleted();
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
