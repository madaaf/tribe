package com.tribe.app.data.network.job.marvel;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.network.job.Priority;
import com.tribe.app.data.repository.marvel.datasource.CloudMarvelDataStore;
import com.tribe.app.data.repository.marvel.datasource.MarvelDataStore;

/**
 * Created by tiago on 10/05/2016.
 */
public class GetMarvelCharacterListJob extends Job {

    private final MarvelDataStore marvelDataStore;

    public GetMarvelCharacterListJob(MarvelDataStore marvelDataStore) {
        super(new Params(Priority.HIGH));
        this.marvelDataStore = marvelDataStore;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        marvelDataStore.characters();
    }

    @Override
    protected void onCancel(int cancelReason) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
