package com.tribe.app.data.repository.marvel.datasource;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.MarvelCache;
import com.tribe.app.data.network.MarvelApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MarvelCharacterRealm;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class CloudMarvelDataStore implements MarvelDataStore {

    private final MarvelApi marvelApi;
    private final MarvelCache marvelCache;

    private final Action1<List<MarvelCharacterRealm>> saveToCacheAction = marvelCharacterListRealm -> {
        if (marvelCharacterListRealm != null && marvelCharacterListRealm.size() > 0) {
            CloudMarvelDataStore.this.marvelCache.put(marvelCharacterListRealm);
        }
    };

    public CloudMarvelDataStore(MarvelCache marvelCache, MarvelApi marvelApi) {
        this.marvelCache = marvelCache;
        this.marvelApi = marvelApi;
    }

    @Override
    public Observable<List<MarvelCharacterRealm>> characters() {
        return this.marvelApi.getCharacters(0).doOnNext(saveToCacheAction);
    }
}
