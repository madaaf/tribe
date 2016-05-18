package com.tribe.app.domain.interactor.marvel;

import com.tribe.app.data.repository.marvel.DiskMarvelDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

/**
 * Created by tiago on 07/05/2016.
 */
public class GetDiskMarvelCharacterList extends UseCase {

    private MarvelRepository marvelRepository;

    @Inject
    protected GetDiskMarvelCharacterList(DiskMarvelDataRepository marvelRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.marvelRepository = marvelRepository;
    }


    @Override
    protected Observable buildUseCaseObservable() {
        return marvelRepository.characters();
    }
}
