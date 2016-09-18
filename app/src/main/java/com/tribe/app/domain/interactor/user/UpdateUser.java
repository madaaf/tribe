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
    private String username;
    private String displayName;
    private String pictureUri;
    private String fbid;

    @Inject
    protected UpdateUser(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String username, String displayName, String pictureUri, String fbid) {
        this.username = username;
        this.displayName = displayName;
        this.pictureUri = pictureUri;
        this.fbid = fbid;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.updateUser(username, displayName, pictureUri, fbid);
    }
}
