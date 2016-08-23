package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetCloudTribeList extends UseCase {

    private TribeRepository tribeRepository;

    @Inject
    public GetCloudTribeList(CloudTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return tribeRepository.tribes(null);
    }
}
