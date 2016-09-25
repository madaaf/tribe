package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class LeaveGroup extends UseCase {

    private UserRepository userRepository;
    private String membershipId;

    @Inject
    LeaveGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String membershipId) {
        this.membershipId = membershipId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.leaveGroup(membershipId);
    }
}
