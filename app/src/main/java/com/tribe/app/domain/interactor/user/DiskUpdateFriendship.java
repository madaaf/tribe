package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 29/06/2016.
 */
public class DiskUpdateFriendship extends UseCaseDisk {

    private String friendshipId;
    private @FriendshipRealm.FriendshipStatus String status;
    private UserRepository userRepository;

    @Inject
    public DiskUpdateFriendship(DiskUserDataRepository userRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
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
