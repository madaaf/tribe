package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class SendOnlineNotification extends UseCase {

    private UserRepository userRepository;

    @Inject
    public SendOnlineNotification(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.sendOnlineNotification();
    }
}
