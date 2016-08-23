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
public class DoLoginWithPhoneNumber extends UseCase {

    private String phoneNumber;
    private String code;
    private String pinId;
    private UserRepository userRepository;

    @Inject
    public DoLoginWithPhoneNumber(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String phoneNumber, String code, String pinId) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.pinId = pinId;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.loginWithPhoneNumber(phoneNumber, code, pinId);
    }
}
