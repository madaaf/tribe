package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 10/22/2016.
 */
public class DiskUpdateMessagesReceivedToNotSeen extends UseCaseDisk {

    private UserRepository userDataRepository;

    @Inject
    public DiskUpdateMessagesReceivedToNotSeen(DiskUserDataRepository userDataRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.userDataRepository = userDataRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userDataRepository.updateMessagesReceivedToNotSeen();
    }
}
