package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetReceivedDiskMessageList extends UseCaseDisk {

    private UserRepository userRepository;
    private String recipientId;

    @Inject
    public GetReceivedDiskMessageList(DiskUserDataRepository userRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return userRepository.messagesReceived(recipientId);
    }
}
