package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class DiskGetMembership extends UseCase {

    private UserRepository userRepository;
    private String membershipId;

    @Inject
    public DiskGetMembership(DiskUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String membershipId) {
        this.membershipId = membershipId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.getMembershipInfos(membershipId);
    }
}
