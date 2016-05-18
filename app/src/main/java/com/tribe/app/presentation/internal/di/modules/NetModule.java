package com.tribe.app.presentation.internal.di.modules;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.BuildConfig;
import com.tribe.app.data.network.MarvelApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.authorizer.MarvelAuthorizer;
import com.tribe.app.data.network.deserializer.MarvelResultsDeserializer;
import com.tribe.app.data.network.interceptor.MarvelSigningInterceptor;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.internal.di.PerApplication;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

@Module
public class NetModule {

    @Provides
    @PerApplication
    static Gson provideGson() {
        return new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(new TypeToken<List<MarvelCharacterRealm>>() {}.getType(),
                        new MarvelResultsDeserializer<MarvelCharacterRealm>())
                .create();
    }

    @Provides
    @PerApplication
    static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @PerApplication
    MarvelAuthorizer provideMarvelAuthorizer() {
        return new MarvelAuthorizer(BuildConfig.MARVEL_PUBLIC_KEY, BuildConfig.MARVEL_PRIVATE_KEY);
    }

    @Provides
    @PerApplication
    static TribeApi provideTribeApi(Gson gson, OkHttpClient okHttpClient) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        return new Retrofit.Builder()
                .baseUrl("http://104.196.20.232:3000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .callFactory(httpClientBuilder.build())
                .build().create(TribeApi.class);
    }

    @Provides
    @PerApplication
    static MarvelApi provideMarvelApi(Gson gson, OkHttpClient okHttpClient, MarvelAuthorizer marvelAuthorizer) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        MarvelSigningInterceptor signingIterceptor =
                new MarvelSigningInterceptor(
                        marvelAuthorizer.getApiClient(),
                        marvelAuthorizer.getApiSecret());

        httpClientBuilder.addInterceptor(signingIterceptor);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        return new Retrofit.Builder()
                .baseUrl("http://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .callFactory(httpClientBuilder.build())
                .build().create(MarvelApi.class);
    }
}
