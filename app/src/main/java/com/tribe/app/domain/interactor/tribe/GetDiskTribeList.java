package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetDiskTribeList extends UseCase {

    private TribeRepository tribeRepository;

    @Inject
    public GetDiskTribeList(TribeRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return tribeRepository.tribes();
    }
}
