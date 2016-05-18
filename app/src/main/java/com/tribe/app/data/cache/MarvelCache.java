package com.tribe.app.data.cache;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MarvelCharacterRealm;

import java.util.List;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface MarvelCache {

    public boolean isExpired();
    public boolean isCached(int marvelId);
    public Observable<List<MarvelCharacterRealm>> characters();
    public void put(List<MarvelCharacterRealm> marvelCharacterListRealm);
}
