package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 08/28/2016.
 */
public class CloudUpdateStatuses extends UseCase {

    private ChatRepository chatDataRepository;
    private String recipientId;

    @Inject
    public CloudUpdateStatuses(CloudChatDataRepository chatDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.chatDataRepository = chatDataRepository;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return chatDataRepository.updateStatuses(recipientId);
    }
}
