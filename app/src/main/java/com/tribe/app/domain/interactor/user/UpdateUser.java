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
public class UpdateUser extends UseCase {

    private UserRepository userRepository;
    private String key;
    private String value;

    @Inject
    protected UpdateUser(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.updateUser(key, value);
    }
}
