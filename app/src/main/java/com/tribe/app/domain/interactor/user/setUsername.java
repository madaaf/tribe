package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SetUsername extends UseCase {

    private UserRepository userRepository;
    private String username;

    @Inject
    protected SetUsername(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String username) {
        this.username = username;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.setUsername(username);
    }
}
