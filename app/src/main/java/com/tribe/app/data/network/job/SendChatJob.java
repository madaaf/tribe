package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.DeleteChat;
import com.tribe.app.domain.interactor.text.SendChat;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class SendChatJob extends BaseJob {

    @Inject
    @Named("cloudSendChat")
    SendChat cloudSendChat;

    @Inject
    @Named("diskDeleteChat")
    DeleteChat diskDeleteChat;

    @Inject
    ChatCache chatCache;

    @Inject
    ChatRealmDataMapper chatRealmDataMapper;

    // VARIABLES
    private ChatMessage chatMessage;

    public SendChatJob(ChatMessage chatMessage) {
        super(new Params(Priority.HIGH).groupBy(chatMessage.getTo().getId()).setSingleId(chatMessage.getLocalId()));
        this.chatMessage = chatMessage;
    }

    @Override
    public void onAdded() {
        ChatRealm chatRealm = chatRealmDataMapper.transform(chatMessage);
        chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_PENDING);
        chatCache.update(chatRealm);
    }

    @Override
    public void onRun() throws Throwable {
        cloudSendChat.setChatMessage(chatMessage);
        cloudSendChat.execute(new ChatSendSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class ChatSendSubscriber extends DefaultSubscriber<ChatMessage> {

        @Override
        public void onCompleted() {
            cloudSendChat.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            ChatRealm chatRealm = chatRealmDataMapper.transform(chatMessage);
            chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_ERROR);
            chatCache.update(chatRealm);
        }

        @Override
        public void onNext(ChatMessage chatMessage) {
            ChatRealm chatRealm = chatRealmDataMapper.transform(chatMessage);
            chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_SENT);
            chatCache.update(chatRealm);
        }
    }
}
