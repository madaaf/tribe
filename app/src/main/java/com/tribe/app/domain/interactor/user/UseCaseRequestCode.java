package com.tribe.app.domain.interactor.user;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class UseCaseRequestCode extends UseCase {

    private final String phoneNumber;
    private UserRepository userRepository;

    @Inject
    public UseCaseRequestCode(String phoneNumber, UserRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.phoneNumber = phoneNumber;
        this.userRepository = userRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.requestCode(phoneNumber);
    }
}
