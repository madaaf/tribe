package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetDiskChatMessageList extends UseCaseDisk {

    private ChatRepository chatDataRepository;
    private String recipientId;

    @Inject
    public GetDiskChatMessageList(DiskChatDataRepository chatDataRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatDataRepository = chatDataRepository;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return chatDataRepository.messages(recipientId);
    }
}
