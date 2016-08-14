package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class SaveChat extends UseCaseDisk {

    private ChatRepository chatDataRepository;
    private ChatMessage chatMessage;

    @Inject
    public SaveChat(DiskChatDataRepository chatDataRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatDataRepository = chatDataRepository;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return chatDataRepository.sendMessage(chatMessage);
    }
}
