package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 08/25/2016.
 */
public class GetPendingMessageList extends UseCaseDisk {

    private ChatRepository chatRepository;
    private String recipientId;

    @Inject
    public GetPendingMessageList(DiskChatDataRepository chatRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatRepository = chatRepository;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return chatRepository.messagesError(recipientId);
    }
}
