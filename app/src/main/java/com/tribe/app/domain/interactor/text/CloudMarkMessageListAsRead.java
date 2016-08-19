package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class CloudMarkMessageListAsRead extends UseCase {

    private List<ChatMessage> chatList;
    private ChatRepository chatRepository;

    @Inject
    public CloudMarkMessageListAsRead(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.chatRepository = chatRepository;
    }

    public void setChatList(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.chatRepository.markMessageListAsRead(chatList);
    }
}
