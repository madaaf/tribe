package com.tribe.app.domain.interactor.friendship;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class UseCaseGetFriendshipList extends UseCase {

    private int userId;
    private FriendshipRepository friendshipRepository;

    @Inject
    protected UseCaseGetFriendshipList(int userId, FriendshipRepository friendshipRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userId = userId;
        this.friendshipRepository = friendshipRepository;
    }


    @Override
    protected Observable buildUseCaseObservable() {
        return friendshipRepository.friendships(userId);
    }
}
