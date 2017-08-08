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
 * Created by remy on 07/08/2017.
 */

public class UpdateUserFacebook extends UseCase {

    private UserRepository userRepository;
    private String userId;
    private String accessToken;

    @Inject
    protected UpdateUserFacebook(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    @Override protected Observable buildUseCaseObservable() {
        return this.userRepository.updateUserFacebook(userId, accessToken);
    }
}
