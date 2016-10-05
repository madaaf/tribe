package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 10/3/16.
 */
public class ModifyPrivateGroupLink extends UseCase {

    private String membershipId;
    private boolean create;
    private UserRepository userRepository;

    @Inject
    public ModifyPrivateGroupLink(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String membershipId, boolean create) {
        this.membershipId = membershipId;
        this.create = create;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.modifyPrivateGroupLink(membershipId, create);
    }
}
