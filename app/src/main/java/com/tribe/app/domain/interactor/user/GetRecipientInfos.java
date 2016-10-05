package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetRecipientInfos extends UseCaseDisk {

    private String recipientId;
    private boolean isToGroup;
    private UserRepository userRepository;

    @Inject
    public GetRecipientInfos(DiskUserDataRepository userRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String recipientId, boolean isToGroup) {
        this.recipientId = recipientId;
        this.isToGroup = isToGroup;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.getRecipientInfos(recipientId, isToGroup);
    }
}
