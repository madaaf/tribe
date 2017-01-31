package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class CloudUpdateFriendship extends UseCase {

    private String friendshipId;
    private
    @FriendshipRealm.FriendshipStatus
    String status;
    private UserRepository userRepository;

    @Inject
    public CloudUpdateFriendship(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void prepare(String friendshipId, @FriendshipRealm.FriendshipStatus String status) {
        this.friendshipId = friendshipId;
        this.status = status;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.updateFriendship(friendshipId, status);
    }
}
