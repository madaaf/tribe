package com.tribe.app.domain.interactor.marvel;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.repository.marvel.CloudMarvelDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

/**
 * Created by tiago on 07/05/2016.
 */
public class GetCloudMarvelCharacterList extends UseCase {

    private MarvelRepository marvelRepository;

    @Inject
    protected GetCloudMarvelCharacterList(CloudMarvelDataRepository marvelRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.marvelRepository = marvelRepository;
    }


    @Override
    protected Observable buildUseCaseObservable() {
        return marvelRepository.characters();
    }
}
