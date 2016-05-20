package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class DoLoginWithUsername extends UseCase {

    private String username;
    private String password;
    private UserRepository userRepository;

    @Inject
    public DoLoginWithUsername(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.loginWithUserName(username, password);
    }
}
