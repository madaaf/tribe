package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.view.utils.PhoneUtils;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class DoLoginWithPhoneNumber extends UseCase {

    private String phoneNumber;
    private String code;
    private String scope;
    private UserRepository userRepository;
    private PhoneUtils phoneUtils;

    @Inject
    public DoLoginWithPhoneNumber(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, PhoneUtils phoneUtils) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
        this.phoneUtils = phoneUtils;
    }

    public void prepare(String phoneNumber, String code, String pinId) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.scope = pinId + " " + phoneUtils.prepareForScope(phoneNumber);
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.loginWithPhoneNumber(phoneNumber, code, scope);
    }
}
