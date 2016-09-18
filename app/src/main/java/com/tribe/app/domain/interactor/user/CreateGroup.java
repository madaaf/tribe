package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/17/16.
 */
public class CreateGroup extends UseCase {

    private UserRepository userRepository;
    private String groupName;
    private List<String> memberIds;
    private boolean isPrivate;
    private String pictureUri;

    @Inject
    CreateGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public void prepare(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri) {
        this.groupName = groupName;
        this.memberIds = memberIds;
        this.isPrivate = isPrivate;
        this.pictureUri = pictureUri;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.createGroup(groupName, memberIds, isPrivate, pictureUri);
    }
}
