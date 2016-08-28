package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetNotSeenDiskTribeList extends UseCaseDisk {

    private TribeRepository tribeRepository;
    private String recipientId;

    @Inject
    public GetNotSeenDiskTribeList(DiskTribeDataRepository tribeRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return tribeRepository.tribesNotSeen(recipientId);
    }
}
