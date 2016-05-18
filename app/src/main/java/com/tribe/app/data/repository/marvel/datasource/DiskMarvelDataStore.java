package com.tribe.app.data.repository.marvel.datasource;

import com.tribe.app.data.cache.MarvelCache;
import com.tribe.app.data.network.MarvelApi;
import com.tribe.app.data.realm.MarvelCharacterRealm;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class DiskMarvelDataStore implements MarvelDataStore {

    private final MarvelCache marvelCache;

    public DiskMarvelDataStore(MarvelCache marvelCache) {
        this.marvelCache = marvelCache;
    }

    @Override
    public Observable<List<MarvelCharacterRealm>> characters() {
        return marvelCache.characters();
    }
}
