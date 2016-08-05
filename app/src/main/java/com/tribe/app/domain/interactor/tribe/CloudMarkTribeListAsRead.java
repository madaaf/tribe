package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class CloudMarkTribeListAsRead extends UseCase {

    private List<Tribe> tribeList;
    private TribeRepository tribeRepository;

    @Inject
    public CloudMarkTribeListAsRead(CloudTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribeList(List<Tribe> tribeList) {
        this.tribeList = tribeList;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.markTribeListAsRead(tribeList);
    }
}
