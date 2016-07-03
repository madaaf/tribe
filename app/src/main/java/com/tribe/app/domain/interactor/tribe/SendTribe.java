package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class SendTribe extends UseCase {

    private Tribe tribe;
    private TribeRepository tribeRepository;

    @Inject
    public SendTribe(CloudTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.sendTribe(tribe);
    }
}
