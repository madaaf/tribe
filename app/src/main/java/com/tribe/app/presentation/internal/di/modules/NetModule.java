package com.tribe.app.presentation.internal.di.modules;

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
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.deserializer.DateDeserializer;
import com.tribe.app.data.network.deserializer.NewInstallDeserializer;
import com.tribe.app.data.network.deserializer.NewTribeDeserializer;
import com.tribe.app.data.network.deserializer.TribeAccessTokenDeserializer;
import com.tribe.app.data.network.deserializer.TribeUserDeserializer;
import com.tribe.app.data.network.deserializer.UserTribeListDeserializer;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.internal.di.scope.PerApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmObject;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

@Module(
    includes = DataModule.class
)
public class NetModule {

    static final int DISK_CACHE_SIZE = (int) DecimalByteUnit.MEGABYTES.toBytes(50);

    @Provides
    @PerApplication
    Gson provideGson(@Named("utcSimpleDate") SimpleDateFormat utcSimpleDate, @Named("utcSimpleDateFull") SimpleDateFormat utcSimpleDateFull) {
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
                .registerTypeAdapter(new TypeToken<UserRealm>() {}.getType(), new TribeUserDeserializer<>())
                .registerTypeAdapter(AccessToken.class, new TribeAccessTokenDeserializer())
                .registerTypeAdapter(TribeRealm.class, new NewTribeDeserializer<>())
                .registerTypeAdapter(new TypeToken<List<TribeRealm>>() {}.getType(), new UserTribeListDeserializer<>(utcSimpleDate))
                .registerTypeAdapter(Installation.class, new NewInstallDeserializer<>())
                .registerTypeAdapter(Date.class, new DateDeserializer(utcSimpleDateFull))
                .create();
    }

    @Provides
    @PerApplication
    OkHttpClient provideOkHttpClient(Context context) {
        return createOkHttpClient(context).build();
    }

    @Provides
    @Named("picassoOkHttp")
    @PerApplication
    OkHttpClient provideOkHttpClientPicasso(Context context) {
        return createOkHttpClient(context).build();
    }

    @Provides
    @PerApplication
    TribeAuthorizer provideTribeAuthorizer(AccessToken accessToken) {
        return new TribeAuthorizer(BuildConfig.TRIBE_PUBLIC_KEY, BuildConfig.TRIBE_PRIVATE_KEY, accessToken);
    }

    @Provides
    @PerApplication
    TribeApi provideTribeApi(Gson gson, OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();

            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-type", "application/json");

            if (tribeAuthorizer.getAccessToken() != null && tribeAuthorizer.getAccessToken().getAccessToken() != null) {
                        requestBuilder.header("Authorization", tribeAuthorizer.getAccessToken().getTokenType()
                                + " " + tribeAuthorizer.getAccessToken().getAccessToken());
            } else {
                byte[] data = (tribeAuthorizer.getApiClient() + ":" + tribeAuthorizer.getApiSecret()).getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "");

                requestBuilder.header("Authorization", "Basic " + base64);
            }

            requestBuilder.method(original.method(), original.body());

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        if (BuildConfig.DEBUG) {
            //HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            //loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        }

        return new Retrofit.Builder()
                .baseUrl("http://api.tribe.pm/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .callFactory(httpClientBuilder.build())
                .build().create(TribeApi.class);
    }

    @Provides
    @PerApplication
    FileApi provideFileApi(OkHttpClient okHttpClient) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        httpClientBuilder
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        }

        return new Retrofit.Builder()
            .baseUrl("http://api.tribe.pm")
            .callFactory(httpClientBuilder.build())
            .build().create(FileApi.class);
    }

    @Provides
    @PerApplication
    LoginApi provideLoginApi(Gson gson, OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer) {
        OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

        httpClientBuilder
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);

        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();

            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-type", "application/json");

            if (tribeAuthorizer.getAccessToken() != null && tribeAuthorizer.getAccessToken().getAccessToken() != null) {
                requestBuilder.header("Authorization", tribeAuthorizer.getAccessToken().getTokenType()
                        + " " + tribeAuthorizer.getAccessToken().getAccessToken());
            } else {
                byte[] data = (tribeAuthorizer.getApiClient() + ":" + tribeAuthorizer.getApiSecret()).getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "");

                requestBuilder.header("Authorization", "Basic " + base64);
            }

            requestBuilder.method(original.method(), original.body());

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        }

        return new Retrofit.Builder()
                .baseUrl("http://login.tribe.pm/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .callFactory(httpClientBuilder.build())
                .build().create(LoginApi.class);
    }

    @Provides
    @Singleton
    Picasso providePicasso(Context context, @Named("picassoOkHttp") OkHttpClient client) {
        return new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .indicatorsEnabled(false)
                .build();
    }

    static OkHttpClient.Builder createOkHttpClient(Context context) {
        File cacheDir = new File(context.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS);
    }
}
