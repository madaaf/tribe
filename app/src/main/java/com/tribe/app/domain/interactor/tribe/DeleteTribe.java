package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 05/07/2016.
 */
public class DeleteTribe extends UseCase {

    private Tribe tribe;
    private TribeRepository tribeRepository;

    @Inject
    public DeleteTribe(DiskTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.deleteTribe(tribe);
    }
}
