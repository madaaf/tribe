package com.tribe.app.domain.interactor.user;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class UseCaseLogin extends UseCase {

    private final String phoneNumber;
    private final String code;
    private UserRepository userRepository;

    @Inject
    public UseCaseLogin(String phoneNumber, String code, UserRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.userRepository = userRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.login(phoneNumber, code);
    }
}
