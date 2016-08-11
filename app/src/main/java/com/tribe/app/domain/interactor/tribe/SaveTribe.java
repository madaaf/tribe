package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class SaveTribe extends UseCaseDisk {

    private TribeMessage tribe;
    private TribeRepository tribeRepository;

    @Inject
    public SaveTribe(DiskTribeDataRepository tribeRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribe(TribeMessage tribe) {
        this.tribe = tribe;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.sendTribe(tribe);
    }
}
