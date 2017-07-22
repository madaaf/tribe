package com.tribe.app.domain.interactor.user;

import android.util.Pair;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by remy on 21/07/2017.
 */

public class IncrUserTimeInCall extends UseCase {

    private UserRepository userRepository;
    private String userId;
    private Long timeInCall;

    @Inject
    protected IncrUserTimeInCall(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String userId, Long timeInCall) {
        this.timeInCall = timeInCall;
        this.userId = userId;
    }

    @Override protected Observable buildUseCaseObservable() {
        return this.userRepository.incrUserTimeInCall(userId, timeInCall);
    }
}
