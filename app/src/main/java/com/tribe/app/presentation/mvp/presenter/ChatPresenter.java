package com.tribe.app.presentation.mvp.presenter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.SendChatJob;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.ConnectAndSubscribeMQTT;
import com.tribe.app.domain.interactor.text.DisconnectMQTT;
import com.tribe.app.domain.interactor.text.GetDiskChatMessageList;
import com.tribe.app.domain.interactor.text.SaveChat;
import com.tribe.app.domain.interactor.text.SubscribingMQTT;
import com.tribe.app.domain.interactor.text.UnsubscribeMQTT;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatPresenter implements Presenter {

    private final JobManager jobManager;
    private final ConnectAndSubscribeMQTT connectAndSubscribeMQTT;
    private final SubscribingMQTT subscribingMQTT;
    private final DisconnectMQTT disconnectMQTT;
    private final UnsubscribeMQTT unsubscribeMQTT;
    private final GetDiskChatMessageList diskGetChatMessages;
    private final SaveChat saveChat;

    private MessageView messageView;

    private String friendId;

    @Inject
    public ChatPresenter(JobManager jobManager,
                         @Named("diskGetChatMessages") GetDiskChatMessageList diskGetChatMessages,
                         @Named("saveChat") SaveChat saveChat,
                         @Named("connectAndSubscribe") ConnectAndSubscribeMQTT connectAndSubscribeMQTT,
                         @Named("subscribing") SubscribingMQTT subscribingMQTT,
                         @Named("disconnect") DisconnectMQTT disconnectMQTT,
                         @Named("unsubscribe") UnsubscribeMQTT unsubscribeMQTT) {
        this.jobManager = jobManager;
        this.connectAndSubscribeMQTT = connectAndSubscribeMQTT;
        this.subscribingMQTT = subscribingMQTT;
        this.unsubscribeMQTT = unsubscribeMQTT;
        this.disconnectMQTT = disconnectMQTT;
        this.diskGetChatMessages = diskGetChatMessages;
        this.saveChat = saveChat;
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
        diskGetChatMessages.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        messageView = (MessageView) v;
    }

    public void loadChatMessages(String friendshipId) {
        diskGetChatMessages.setFriendshipId(friendshipId);
        diskGetChatMessages.execute(new ChatMessageListSubscriber());
    }

    public void loadThumbnail(int radius) {
        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                final String[] columns = { MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media._ID };
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
                });
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

    public void sendTypingEvent() {
        System.out.println("Typing !");
    }

    private final class ConnectAndSubscribeMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED SUBSCRIBE");
            subscribingMQTT.setTopic("^chats/" + friendId + ".*");
            subscribingMQTT.execute(new ListenSubscribingMQTTSubscriber());
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
            System.out.println("ON NEXT SUBSCRIBE");
        }
    }

    private final class ListenSubscribingMQTTSubscriber extends DefaultSubscriber<List<ChatMessage>> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED MESSAGE");
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
            System.out.println("ON NEXT DISCONNECT");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
            System.out.println("ON NEXT DISCONNECT");
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

    private final class SaveChatSubscriber extends DefaultSubscriber<ChatMessage> {
        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(ChatMessage chatMessage) {
            System.out.println("Chat Message saved ! : " + chatMessage.getLocalId());
        }
    }
}
