package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class CloudMarkTribeAsSave extends UseCase {

    private TribeMessage tribe;
    private TribeRepository tribeRepository;

    @Inject
    public CloudMarkTribeAsSave(CloudTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribe(TribeMessage tribe) {
        this.tribe = tribe;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.markTribeAsSave(tribe);
    }
}
