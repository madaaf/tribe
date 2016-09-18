package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetDiskUserInfos extends UseCaseDisk {

    private String userId;
    private String filterRecipient;
    private UserRepository userRepository;

    @Inject
    public GetDiskUserInfos(DiskUserDataRepository userRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String userId, String filterRecipient) {
        this.userId = userId;
        this.filterRecipient = filterRecipient;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.userInfos(userId, filterRecipient);
    }
}
