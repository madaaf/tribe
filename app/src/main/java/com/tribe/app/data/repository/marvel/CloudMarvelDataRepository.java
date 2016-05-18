package com.tribe.app.data.repository.marvel;

import com.tribe.app.data.realm.mapper.MarvelRealmDataMapper;
import com.tribe.app.data.repository.marvel.datasource.MarvelDataStore;
import com.tribe.app.data.repository.marvel.datasource.MarvelDataStoreFactory;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.domain.interactor.marvel.MarvelRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

@Singleton
public class CloudMarvelDataRepository implements MarvelRepository {

    private final MarvelDataStoreFactory marvelDataStoreFactory;
    private final MarvelRealmDataMapper marvelRealmDataMapper;

    @Inject
    public CloudMarvelDataRepository(MarvelDataStoreFactory marvelDataStoreFactory,
                                     MarvelRealmDataMapper marvelRealmDataMapper) {
        this.marvelDataStoreFactory = marvelDataStoreFactory;
        this.marvelRealmDataMapper = marvelRealmDataMapper;
    }

    @Override
    public Observable<List<MarvelCharacter>> characters() {
        final MarvelDataStore marvelCloudDataStore = this.marvelDataStoreFactory.createCloudDataStore();

        return marvelCloudDataStore.characters()
            .filter(marvelCharacterRealms -> marvelCharacterRealms != null && marvelCharacterRealms.size() > 0)
                .map(characterRealmCollection -> this.marvelRealmDataMapper.transform(characterRealmCollection));
    }
}
