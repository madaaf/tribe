package com.tribe.app.presentation.internal.di.modules;

import android.app.Application;
import android.content.Context;
import android.util.Base64;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.byteunits.DecimalByteUnit;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.tribe.app.BuildConfig;
import com.tribe.app.data.network.MarvelApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.authorizer.MarvelAuthorizer;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.deserializer.MarvelResultsDeserializer;
import com.tribe.app.data.network.interceptor.MarvelSigningInterceptor;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.presentation.internal.di.PerApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmObject;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

@Module
public class NetModule {


    static final int DISK_CACHE_SIZE = (int) DecimalByteUnit.MEGABYTES.toBytes(50);

    @Provides
    @PerApplication
    Gson provideGson() {
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
    OkHttpClient provideOkHttpClient(Context context) {
        return createOkHttpClient(context).build();
    }

    @Provides
    @PerApplication
    MarvelAuthorizer provideMarvelAuthorizer() {
        return new MarvelAuthorizer(BuildConfig.MARVEL_PUBLIC_KEY, BuildConfig.MARVEL_PRIVATE_KEY);
    }

    @Provides
    @PerApplication
    TribeAuthorizer provideTribeAuthorizer() {
        return new TribeAuthorizer(BuildConfig.TRIBE_PUBLIC_KEY, BuildConfig.TRIBE_PRIVATE_KEY);
    }

    @Provides
    @PerApplication
    TribeApi provideTribeApi(Gson gson, OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                byte[] data = (tribeAuthorizer.getApiClient() + ":" + tribeAuthorizer.getApiSecret()).getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "");

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Basic " + base64)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
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
    MarvelApi provideMarvelApi(Gson gson, OkHttpClient okHttpClient, MarvelAuthorizer marvelAuthorizer) {
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

    @Provides
    @Singleton
    Picasso providePicasso(Context context, OkHttpClient client) {
        return new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
    }

    static OkHttpClient.Builder createOkHttpClient(Context context) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(context.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache);
    }
}
