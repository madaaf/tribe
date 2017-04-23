package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Base64;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.byteunits.DecimalByteUnit;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.deserializer.CollectionAdapter;
import com.tribe.app.data.network.deserializer.CreateFriendshipDeserializer;
import com.tribe.app.data.network.deserializer.DateDeserializer;
import com.tribe.app.data.network.deserializer.FriendshipRealmDeserializer;
import com.tribe.app.data.network.deserializer.GroupDeserializer;
import com.tribe.app.data.network.deserializer.HowManyFriendsDeserializer;
import com.tribe.app.data.network.deserializer.InstallsDeserializer;
import com.tribe.app.data.network.deserializer.InvitesListDeserializer;
import com.tribe.app.data.network.deserializer.LookupFBDeserializer;
import com.tribe.app.data.network.deserializer.NewInstallDeserializer;
import com.tribe.app.data.network.deserializer.NewMembershipDeserializer;
import com.tribe.app.data.network.deserializer.RoomConfigurationDeserializer;
import com.tribe.app.data.network.deserializer.SearchResultDeserializer;
import com.tribe.app.data.network.deserializer.TribeAccessTokenDeserializer;
import com.tribe.app.data.network.deserializer.TribeUserDeserializer;
import com.tribe.app.data.network.deserializer.UserListDeserializer;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LookupFBResult;
import com.tribe.app.data.network.entity.RefreshEntity;
import com.tribe.app.data.network.interceptor.TribeInterceptor;
import com.tribe.app.data.network.util.LookupApi;
import com.tribe.app.data.network.util.TribeApiUtils;
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
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import dagger.Module;
import dagger.Provides;
import io.realm.RealmObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
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
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Module(includes = DataModule.class) public class NetModule {

  static final int DISK_CACHE_SIZE = (int) DecimalByteUnit.MEGABYTES.toBytes(50);
  static final ConditionVariable LOCK = new ConditionVariable(true);
  static final AtomicBoolean isRefreshing = new AtomicBoolean(false);

  @Provides @PerApplication @Named("simpleGson") Gson provideSimpleGson(
      @Named("utcSimpleDate") SimpleDateFormat utcSimpleDate) {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
      @Override public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(RealmObject.class);
      }

      @Override public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    }).registerTypeAdapter(Date.class, new DateDeserializer(utcSimpleDate, sdf)).create();
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
        .registerTypeAdapter(LookupFBResult.class, new LookupFBDeserializer())
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

    httpClientBuilder.addInterceptor(new TribeInterceptor(context, tribeAuthorizer));

    httpClientBuilder.authenticator(
        new TribeAuthenticator(context, accessToken, loginApi, userCache, tribeAuthorizer,
            tagManager));

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

  @Provides @PerApplication LookupApi provideLookupApi(Context context, Gson gson,
      @Named("tribeApiOKHttp") OkHttpClient okHttpClient, TribeAuthorizer tribeAuthorizer,
      final LoginApi loginApi, final AccessToken accessToken, final UserCache userCache,
      TagManager tagManager) {
    OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

    httpClientBuilder.addInterceptor(new TribeInterceptor(context, tribeAuthorizer));

    httpClientBuilder.authenticator(
        new TribeAuthenticator(context, accessToken, loginApi, userCache, tribeAuthorizer,
            tagManager));

    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      httpClientBuilder.addInterceptor(loggingInterceptor);
      httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
    }

    return new Retrofit.Builder().baseUrl(BuildConfig.TRIBE_LOOKUP)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
        //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .callFactory(httpClientBuilder.build())
        .build()
        .create(LookupApi.class);
  }

  private class TribeAuthenticator implements Authenticator {

    private Context context;
    private AccessToken accessToken;
    private LoginApi loginApi;
    private UserCache userCache;
    private TribeAuthorizer tribeAuthorizer;
    private TagManager tagManager;

    public TribeAuthenticator(Context context, AccessToken accessToken, LoginApi loginApi,
        UserCache userCache, TribeAuthorizer tribeAuthorizer, TagManager tagManager) {
      this.context = context;
      this.accessToken = accessToken;
      this.loginApi = loginApi;
      this.userCache = userCache;
      this.tribeAuthorizer = tribeAuthorizer;
      this.tagManager = tagManager;
    }

    @Override public Request authenticate(Route route, okhttp3.Response response)
        throws IOException {
      if (accessToken == null || accessToken.getRefreshToken() == null) return null;

      if (isRefreshing.compareAndSet(false, true)) {
        LOCK.close();

        Response<AccessToken> responseRefresh = null;

        try {
          Call<AccessToken> newAccessTokenReq =
              loginApi.refreshToken(new RefreshEntity(accessToken.getRefreshToken()));
          responseRefresh = newAccessTokenReq.execute();
        } catch (SocketTimeoutException ex) {
          Timber.d("SocketTimeOutException on refresh token");
          clearLock();
        } catch (IOException ex) {
          Timber.d("IOException on refresh token");
          clearLock();
        } catch (Exception ex) {
          Timber.d("Exception on refresh token");
          clearLock();
        }

        if (responseRefresh != null
            && responseRefresh.isSuccessful()
            && responseRefresh.body() != null) {
          AccessToken newAccessToken = responseRefresh.body();
          Timber.d("New access_token : " + newAccessToken.getAccessToken());
          Timber.d("New refresh_token : " + newAccessToken.getRefreshToken());
          accessToken.setAccessToken(newAccessToken.getAccessToken());
          accessToken.setRefreshToken(newAccessToken.getRefreshToken());
          accessToken.setTokenType("Bearer");
          userCache.put(accessToken);
          tribeAuthorizer.setAccessToken(accessToken);

          clearLock();

          return response.request()
              .newBuilder()
              .header("Authorization",
                  accessToken.getTokenType() + " " + accessToken.getAccessToken())
              .build();
        } else {
          Timber.d("Error in refresh");
          if (response != null && response.body() != null) {
            response.body().close();
          }

          if (responseRefresh != null && responseRefresh.code() == 401) {
            if (responseRefresh != null) {
              if (!StringUtils.isEmpty(responseRefresh.message())) {
                Timber.d("Response refresh message : " + responseRefresh.message());
                Bundle properties = new Bundle();
                properties.putString(TagManagerUtils.ERROR, responseRefresh.message());
                tagManager.trackEvent(TagManagerUtils.TOKEN_DISCONNECT, properties);
              } else {
                tagManager.trackEvent(TagManagerUtils.TOKEN_DISCONNECT);
              }

              if (responseRefresh.errorBody() != null) {
                responseRefresh.errorBody().close();
              }
            }

            Timber.d("Got a 401 on refresh token disconnecting the user");

            Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    s -> ((AndroidApplication) context.getApplicationContext()).logoutUser());

            clearLock();

            return null;
          } else {
            Timber.d("Request failed but for another reason than 401");

            clearLock();

            return null;
          }
        }
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
    }
  }

  private void clearLock() {
    Timber.d("Opening LOCK");
    LOCK.open();
    isRefreshing.set(false);
    Timber.d("Retrying request");
  }

  @Provides @PerApplication FileApi provideFileApi(
      @Named("tribeApiOKHttp") OkHttpClient okHttpClient) {
    OkHttpClient.Builder httpClientBuilder = okHttpClient.newBuilder();

    httpClientBuilder.connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS);

    //if (BuildConfig.DEBUG) {
    //  HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    //  loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    //  httpClientBuilder.addInterceptor(loggingInterceptor);
    //  httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
    //}

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

    httpClientBuilder.connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS);

    httpClientBuilder.addInterceptor(chain -> {
      Request original = chain.request();

      Request.Builder requestBuilder =
          original.newBuilder().header("Content-type", "application/json");

      byte[] data = (tribeAuthorizer.getApiClient() + ":" + DateUtils.unifyDate(
          tribeAuthorizer.getApiSecret())).getBytes("UTF-8");
      String base64 = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "");

      requestBuilder.header("Authorization", "Basic " + base64);
      TribeApiUtils.appendUserAgent(context, requestBuilder);
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
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS);
  }

  @Provides @Named("webSocketApi") @PerApplication WebSocketConnection provideWebSocketApi() {
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

    WebSocketFactory socketFactory = new WebSocketFactory();
    socketFactory.setSSLContext(sslContext);
    return new WebSocketConnection(socketFactory);
  }
}
