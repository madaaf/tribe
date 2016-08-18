package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class DeleteDiskConversation extends UseCaseDisk {

    private ChatRepository chatDataRepository;
    private String friendshipId;

    @Inject
    public DeleteDiskConversation(DiskChatDataRepository chatDataRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatDataRepository = chatDataRepository;
    }

    public void setFriendshipId(String friendshipId) {
        this.friendshipId = friendshipId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return chatDataRepository.deleteConversation(friendshipId);
    }
}
