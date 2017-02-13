package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Base64;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.byteunits.DecimalByteUnit;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.authorizer.Draft_Graphql;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.deserializer.CollectionAdapter;
import com.tribe.app.data.network.deserializer.CreateFriendshipDeserializer;
import com.tribe.app.data.network.deserializer.DateDeserializer;
import com.tribe.app.data.network.deserializer.FriendshipRealmDeserializer;
import com.tribe.app.data.network.deserializer.GroupDeserializer;
import com.tribe.app.data.network.deserializer.HowManyFriendsDeserializer;
import com.tribe.app.data.network.deserializer.InstallsDeserializer;
import com.tribe.app.data.network.deserializer.InvitesListDeserializer;
import com.tribe.app.data.network.deserializer.LookupDeserializer;
import com.tribe.app.data.network.deserializer.NewInstallDeserializer;
import com.tribe.app.data.network.deserializer.NewMembershipDeserializer;
import com.tribe.app.data.network.deserializer.RoomConfigurationDeserializer;
import com.tribe.app.data.network.deserializer.SearchResultDeserializer;
import com.tribe.app.data.network.deserializer.TribeAccessTokenDeserializer;
import com.tribe.app.data.network.deserializer.TribeUserDeserializer;
import com.tribe.app.data.network.deserializer.UserListDeserializer;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.network.entity.RefreshEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import dagger.Module;
import dagger.Provides;
import io.realm.RealmObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Module(
    includes = DataModule.class) public class NetModule {

  static final int DISK_CACHE_SIZE = (int) DecimalByteUnit.MEGABYTES.toBytes(50);
  static final ConditionVariable LOCK = new ConditionVariable(true);
  static final AtomicBoolean isRefreshing = new AtomicBoolean(false);

  @Provides @PerApplication @Named("simpleGson") Gson provideSimpleGson(
      @Named("utcSimpleDate") SimpleDateFormat utcSimpleDate) {
    return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
      @Override public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(RealmObject.class);
      }

      @Override public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    })
        .registerTypeAdapter(Date.class,
            new DateDeserializer(utcSimpleDate, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")))
        .create();
  }

  @Provides @PerApplication Gson provideGson(@Named("utcSimpleDate") SimpleDateFormat utcSimpleDate,
      @Named("utcSimpleDateFull") SimpleDateFormat utcSimpleDateFull) {

    GroupDeserializer groupDeserializer = new GroupDeserializer();

    return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
      @Override public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(RealmObject.class);
      }

      @Override public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    })
        .registerTypeAdapter(new TypeToken<UserRealm>() {
        }.getType(), new TribeUserDeserializer(groupDeserializer, utcSimpleDate))
        .registerTypeAdapter(AccessToken.class, new TribeAccessTokenDeserializer())
        .registerTypeAdapter(GroupRealm.class, groupDeserializer)
        .registerTypeAdapter(Installation.class, new NewInstallDeserializer<>())
        .registerTypeAdapter(Date.class,
            new DateDeserializer(utcSimpleDateFull, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")))
        .registerTypeAdapter(new TypeToken<List<UserRealm>>() {
        }.getType(), new UserListDeserializer<>())
        .registerTypeAdapter(LookupEntity.class, new LookupDeserializer())
        .registerTypeAdapter(CreateFriendshipEntity.class, new CreateFriendshipDeserializer())
        .registerTypeAdapter(new TypeToken<List<Integer>>() {
        }.getType(), new HowManyFriendsDeserializer())
        .registerTypeAdapter(SearchResultRealm.class, new SearchResultDeserializer())
        .registerTypeAdapter(MembershipRealm.class, new NewMembershipDeserializer())
        .registerTypeAdapter(new TypeToken<List<Installation>>() {
        }.getType(), new InstallsDeserializer())
        .registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter())
        .registerTypeAdapter(RoomConfiguration.class, new RoomConfigurationDeserializer())
        .registerTypeAdapter(FriendshipRealm.class, new FriendshipRealmDeserializer())
        .registerTypeAdapter(new TypeToken<List<Invite>>() {
        }.getType(), new InvitesListDeserializer<>())
        .create();
  }

  @Provides @PerApplication @Named("tribeApiOKHttp") OkHttpClient provideOkHttpClient(
      Context context) {
    OkHttpClient.Builder okHttpClient = createOkHttpClient(context);

    if (!BuildConfig.DEBUG) {
      InputStream cert = context.getResources().openRawResource(R.raw.tribe);

      try {
        // loading CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;
        ca = cf.generateCertificate(cert);

        // creating a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // creating a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // creating an SSLSocketFactory that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        okHttpClient.sslSocketFactory(sslContext.getSocketFactory());

        String certPin = CertificatePinner.pin(ca);
        CertificatePinner certificatePinner =
            new CertificatePinner.Builder().add(BuildConfig.TRIBE_API, certPin)
                .add(BuildConfig.TRIBE_AUTH, certPin)
                .build();
        okHttpClient.certificatePinner(certificatePinner);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (CertificateException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (KeyStoreException e) {
        e.printStackTrace();
      } catch (KeyManagementException e) {
        e.printStackTrace();
      } finally {
        try {
          cert.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return okHttpClient.build();
  }

  @Provides @PerApplication @Named("fileApiOKHttp") OkHttpClient provideOkHttpClientFile(
      Context context) {
    return createOkHttpClient(context).build();
  }

  @Provides @PerApplication TribeAuthorizer provideTribeAuthorizer(AccessToken accessToken) {
    return new TribeAuthorizer(BuildConfig.TRIBE_PUBLIC_KEY, BuildConfig.TRIBE_PRIVATE_KEY,
        accessToken);
  }

  @Provides @PerApplication TribeApi provideTribeApi(Context context, Gson gson,
      @Named("tribeApiOKHttp") OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer,
      final LoginApi loginApi, final AccessToken accessToken, final UserCache userCache,
      TagManager tagManager) {
    OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

    httpClientBuilder.addInterceptor(chain -> {
      Request original = chain.request();

      Request.Builder requestBuilder =
          original.newBuilder().header("Content-type", "application/json");

      requestBuilder.header("Authorization",
          tribeAuthorizer.getAccessToken().getTokenType() + " " + tribeAuthorizer.getAccessToken()
              .getAccessToken());
      appendUserAgent(context, requestBuilder);
      requestBuilder.method(original.method(), original.body());

      Request request = requestBuilder.build();
      return chain.proceed(request);
    });

    httpClientBuilder.authenticator((route, response) -> {
      if (isRefreshing.compareAndSet(false, true)) {
        LOCK.close();

        Call<AccessToken> newAccessTokenReq =
            loginApi.refreshToken(new RefreshEntity(accessToken.getRefreshToken()));
        Response<AccessToken> responseRefresh = newAccessTokenReq.execute();

        if (responseRefresh.isSuccessful() && responseRefresh.body() != null) {
          AccessToken newAccessToken = responseRefresh.body();
          accessToken.setAccessToken(newAccessToken.getAccessToken());
          accessToken.setRefreshToken(newAccessToken.getRefreshToken());
          userCache.put(accessToken);
          tribeAuthorizer.setAccessToken(accessToken);
        } else {
          if (response != null && response.body() != null) {
            response.body().close();
          }

          if (responseRefresh != null) {
            if (!StringUtils.isEmpty(responseRefresh.message())) {
              Bundle properties = new Bundle();
              properties.putString(TagManagerConstants.ERROR, responseRefresh.message());
              tagManager.trackEvent(TagManagerConstants.TOKEN_DISCONNECT, properties);
            } else {
              tagManager.trackEvent(TagManagerConstants.TOKEN_DISCONNECT);
            }

            if (responseRefresh.errorBody() != null) {
              responseRefresh.errorBody().close();
            }
          }

          Observable.just("")
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(s -> ((AndroidApplication) context.getApplicationContext()).logoutUser());
        }

        LOCK.open();
        isRefreshing.set(false);

        return response.request()
            .newBuilder()
            .header("Authorization",
                accessToken.getTokenType() + " " + accessToken.getAccessToken())
            .build();
      } else {
        boolean conditionOpened = LOCK.block(60000);
        if (conditionOpened) {
          return response.request()
              .newBuilder()
              .header("Authorization",
                  accessToken.getTokenType() + " " + accessToken.getAccessToken())
              .build();
        }

        return null;
      }
    });

    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      httpClientBuilder.addInterceptor(loggingInterceptor);
      httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
    }

    return new Retrofit.Builder().baseUrl(BuildConfig.TRIBE_API)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
        //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .callFactory(httpClientBuilder.build())
        .build()
        .create(TribeApi.class);
  }

  @Provides @PerApplication FileApi provideFileApi(
      @Named("tribeApiOKHttp") OkHttpClient okHttpClient) {
    OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

    httpClientBuilder.connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES);

    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
      httpClientBuilder.addInterceptor(loggingInterceptor);
      httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
    }

    return new Retrofit.Builder().baseUrl(BuildConfig.TRIBE_API)
        .callFactory(httpClientBuilder.build())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
        //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .build()
        .create(FileApi.class);
  }

  @Provides @PerApplication LoginApi provideLoginApi(Gson gson,
      @Named("tribeApiOKHttp") OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer,
      Context context) {
    OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

    httpClientBuilder.connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS);

    httpClientBuilder.addInterceptor(chain -> {
      Request original = chain.request();

      Request.Builder requestBuilder =
          original.newBuilder().header("Content-type", "application/json");

      byte[] data = (tribeAuthorizer.getApiClient() + ":" + DateUtils.unifyDate(
          tribeAuthorizer.getApiSecret())).getBytes("UTF-8");
      String base64 = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "");

      requestBuilder.header("Authorization", "Basic " + base64);
      appendUserAgent(context, requestBuilder);
      requestBuilder.method(original.method(), original.body());

      Request request = requestBuilder.build();
      return chain.proceed(request);
    });

    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
      httpClientBuilder.addInterceptor(loggingInterceptor);
      httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
    }

    return new Retrofit.Builder().baseUrl(BuildConfig.TRIBE_AUTH)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
        //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .callFactory(httpClientBuilder.build())
        .build()
        .create(LoginApi.class);
  }

  static OkHttpClient.Builder createOkHttpClient(Context context) {
    File cacheDir = new File(context.getCacheDir(), "http");
    Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

    return new OkHttpClient.Builder().cache(cache)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS);
  }

  private void appendUserAgent(Context context, Request.Builder requestBuilder) {
    requestBuilder.header("User-Agent", getUserAgent(context));
  }

  private String getUserAgent(Context context) {
    String agent = FirebaseRemoteConfig.getInstance() != null ? FirebaseRemoteConfig.getInstance()
        .getString(Constants.FIREBASE_AGENT_VERSION) : "";
    return context.getPackageName()
        + "/"
        + DeviceUtils.getVersionCode(context)
        + " android/"
        + Build.VERSION.RELEASE
        + " okhttp/3.2"
        + " Agent/"
        + agent;
  }

  @Provides @Named("webSocketApi") @PerApplication WebSocketConnection provideWebSocketApi(
      Context context, TribeAuthorizer tribeAuthorizer) {
    SSLContext sslContext = null;

    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              Timber.d("getAcceptedIssuers =============");
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
              Timber.d("checkClientTrusted =============");
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
              Timber.d("checkServerTrusted =============");
            }
          }
      }, new SecureRandom());
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-type", "application/json");
    headers.put("User-Agent", getUserAgent(context));
    headers.put("Authorization",
        tribeAuthorizer.getAccessToken().getTokenType() + " " + tribeAuthorizer.getAccessToken()
            .getAccessToken());

    return new WebSocketConnection(new DefaultSSLWebSocketClientFactory(sslContext),
        new Draft_Graphql(), headers);
  }
}
