package com.tribe.app.presentation.internal.di.modules;

import android.app.Application;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.MarvelCharacterRealm;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tiago on 30/05/2016.
 */
@Module
public class DataModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences("TRIBE", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    AccessToken provideAccessToken() {
        AccessToken accessToken = new AccessToken();

        Realm realm = Realm.getDefaultInstance();
        final RealmResults<AccessToken> results = realm.where(AccessToken.class).findAll();
        if (results != null && results.size() > 0)
            accessToken = realm.copyFromRealm(results.get(0));
        realm.close();

        return accessToken;
    }
}
