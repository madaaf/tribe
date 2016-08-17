package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 05/07/2016.
 */
public class DeleteChat extends UseCaseDisk {

    private ChatMessage chatMessage;
    private ChatRepository chatRepository;

    @Inject
    public DeleteChat(DiskChatDataRepository chatRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatRepository = chatRepository;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.chatRepository.deleteMessage(chatMessage);
    }
}
