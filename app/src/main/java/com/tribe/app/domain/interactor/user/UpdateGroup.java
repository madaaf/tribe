package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/18/16.
 */
public class UpdateGroup extends UseCase {

    private UserRepository userRepository;
    private String groupId;
    private String groupName;
    private String pictureUri;

    @Inject
    UpdateGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String groupId, String groupName, String pictureUri) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.pictureUri = pictureUri;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.updateGroup(groupId, groupName, pictureUri);
    }
}
