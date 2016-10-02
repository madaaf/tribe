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
public class RemoveGroup extends UseCase {

    private UserRepository userRepository;
    private String groupId;

    @Inject
    RemoveGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String groupId) {
        this.groupId = groupId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.removeGroup(groupId);
    }
}
