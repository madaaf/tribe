package com.tribe.app.domain.interactor.text;

import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.TribeRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class GetDiskChatMessageList extends UseCaseDisk {

    private TribeRepository tribeRepository;

    @Inject
    public GetDiskChatMessageList(DiskTribeDataRepository tribeRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.tribeRepository = tribeRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return tribeRepository.tribes();
    }
}
