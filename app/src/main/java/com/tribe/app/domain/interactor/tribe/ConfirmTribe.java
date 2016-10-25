package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 10/24/2016.
 */
public class ConfirmTribe extends UseCaseDisk {

    private String tribeId;
    private TribeRepository tribeRepository;

    @Inject
    public ConfirmTribe(DiskTribeDataRepository tribeRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribeId(String tribeId) {
        this.tribeId = tribeId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.confirmTribe(tribeId);
    }
}
