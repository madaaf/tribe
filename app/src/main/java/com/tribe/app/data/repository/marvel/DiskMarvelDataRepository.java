package com.tribe.app.data.repository.marvel;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.data.network.job.marvel.GetMarvelCharacterListJob;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.data.realm.mapper.FriendshipRealmDataMapper;
import com.tribe.app.data.realm.mapper.MarvelRealmDataMapper;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStoreFactory;
import com.tribe.app.data.repository.marvel.datasource.MarvelDataStore;
import com.tribe.app.data.repository.marvel.datasource.MarvelDataStoreFactory;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.marvel.MarvelRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func1;

@Singleton
public class DiskMarvelDataRepository implements MarvelRepository {

    private final MarvelDataStoreFactory marvelDataStoreFactory;
    private final MarvelRealmDataMapper marvelRealmDataMapper;

    @Inject
    public DiskMarvelDataRepository(MarvelDataStoreFactory marvelDataStoreFactory,
                                    MarvelRealmDataMapper marvelRealmDataMapper) {
        this.marvelDataStoreFactory = marvelDataStoreFactory;
        this.marvelRealmDataMapper = marvelRealmDataMapper;
    }

    @RxLogObservable
    @Override
    public Observable<List<MarvelCharacter>> characters() {
        final MarvelDataStore marvelDiskDataStore = this.marvelDataStoreFactory.createDiskDataStore();

        return marvelDiskDataStore.characters()
            .filter(marvelCharacterRealms -> marvelCharacterRealms != null && marvelCharacterRealms.size() > 0)
                .map(characterRealmCollection -> this.marvelRealmDataMapper.transform(characterRealmCollection));
    }
}
