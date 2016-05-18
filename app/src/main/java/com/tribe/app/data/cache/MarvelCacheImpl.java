package com.tribe.app.data.cache;

import android.content.Context;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.domain.interactor.marvel.MarvelRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by tiago on 06/05/2016.
 */
public class MarvelCacheImpl implements MarvelCache {

    private Context context;

    @Inject
    public MarvelCacheImpl(Context context) {
        this.context = context;
    }

    @Override
    public boolean isExpired() {
        return true;
    }

    @Override
    public boolean isCached(int marvelId) {
        return false;
    }

    @RxLogObservable
    @Override
    public Observable<List<MarvelCharacterRealm>> characters() {
        return Observable.create(new Observable.OnSubscribe<List<MarvelCharacterRealm>>() {
            @Override
            public void call(final Subscriber<? super List<MarvelCharacterRealm>> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                final RealmResults<MarvelCharacterRealm> results = obsRealm.where(MarvelCharacterRealm.class).findAll();
                subscriber.onNext(results);
            }
        }).map(marvelCharacterResultsRealm -> {
            final List<MarvelCharacterRealm> marvelCharacterListRealm = new ArrayList<MarvelCharacterRealm>(marvelCharacterResultsRealm.size());

            for (MarvelCharacterRealm marvelCharacterRealm : marvelCharacterResultsRealm) {
                marvelCharacterListRealm.add(marvelCharacterRealm);
            }

            return marvelCharacterListRealm;
        });
    }

    @Override
    public void put(List<MarvelCharacterRealm> marvelCharacterListRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(marvelCharacterListRealm);
        realm.commitTransaction();
    }
}
