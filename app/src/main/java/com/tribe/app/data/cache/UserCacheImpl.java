package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class UserCacheImpl implements UserCache {

    private Context context;

    @Inject
    public UserCacheImpl(Context context) {
        this.context = context;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int userId) {
        return false;
    }

    public void put(UserRealm userRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(userRealm);
        realm.commitTransaction();
        realm.close();
    }

    public void put(AccessToken accessToken) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(AccessToken.class);
        realm.copyToRealm(accessToken);
        realm.commitTransaction();
        realm.close();
    }

    public void put(Installation installation) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Installation.class);
        realm.copyToRealm(installation);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return Observable.create(new Observable.OnSubscribe<UserRealm>() {
            @Override
            public void call(final Subscriber<? super UserRealm> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                final RealmResults<UserRealm> results = obsRealm.where(UserRealm.class).findAll();
                if (results != null && results.size() > 0)
                    subscriber.onNext(obsRealm.copyFromRealm(results).get(0));
                else
                    subscriber.onCompleted();
                obsRealm.close();
            }
        });
    }
}
