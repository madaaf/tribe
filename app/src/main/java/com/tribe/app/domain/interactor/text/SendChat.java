package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class SendChat extends UseCase {

    private ChatMessage chatMessage;
    private ChatRepository chatRepository;

    @Inject
    public SendChat(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.chatRepository = chatRepository;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.chatRepository.sendMessage(chatMessage);
    }
}
