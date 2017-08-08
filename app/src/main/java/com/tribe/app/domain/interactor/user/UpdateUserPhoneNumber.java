package com.tribe.app.domain.interactor.user;

import com.digits.sdk.android.DigitsSession;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by remy on 07/08/2017.
 */

public class UpdateUserPhoneNumber extends UseCase {

    private UserRepository userRepository;
    private String userId;
    private DigitsSession digitsSession;

    @Inject
    protected UpdateUserPhoneNumber(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String userId, DigitsSession digitsSession) {
        this.digitsSession = digitsSession;
        this.userId = userId;
    }

    @Override protected Observable buildUseCaseObservable() {
        return this.userRepository.updateUserPhoneNumber(userId, digitsSession);
    }
}
