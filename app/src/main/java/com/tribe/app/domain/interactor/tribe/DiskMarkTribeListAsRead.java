package com.tribe.app.domain.interactor.tribe;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class DiskMarkTribeListAsRead extends UseCaseDisk {

    private List<TribeMessage> tribeList;
    private TribeRepository tribeRepository;

    @Inject
    public DiskMarkTribeListAsRead(DiskTribeDataRepository tribeRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    public void setTribeList(List<TribeMessage> tribeList) {
        this.tribeList = tribeList;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.tribeRepository.markTribeListAsRead(tribeList);
    }
}
