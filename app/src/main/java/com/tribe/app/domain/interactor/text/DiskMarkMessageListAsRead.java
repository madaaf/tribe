package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class DiskMarkMessageListAsRead extends UseCaseDisk {

    private List<ChatMessage> messageList;
    private ChatRepository chatRepository;

    @Inject
    public DiskMarkMessageListAsRead(DiskChatDataRepository chatRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.chatRepository = chatRepository;
    }

    public void setMessageList(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.chatRepository.markMessageListAsRead(messageList);
    }
}
