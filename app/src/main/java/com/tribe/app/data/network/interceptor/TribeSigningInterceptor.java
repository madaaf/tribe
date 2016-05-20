package com.tribe.app.data.network.interceptor;

import com.tribe.app.data.network.Constant;
import com.tribe.app.data.network.util.TribeApiUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TribeSigningInterceptor implements Interceptor {
    private final String mApiKey;
    private final String mApiSecret;

    public TribeSigningInterceptor(String apiKey, String apiSecret) {
        mApiKey = apiKey;
        mApiSecret = apiSecret;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String tribeHash = TribeApiUtils.generateTribeHash(mApiKey, mApiSecret);
        Request oldRequest = chain.request();

        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url().newBuilder()
                .scheme(oldRequest.url().scheme())
                .host(oldRequest.url().host());

        authorizedUrlBuilder.addQueryParameter(Constant.PARAM_API_KEY, mApiKey)
                .addQueryParameter(Constant.PARAM_TIMESTAMP, TribeApiUtils.getUnixTimeStamp())
                .addQueryParameter(Constant.PARAM_HASH, tribeHash);

        Request newRequest = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body())
                .url(authorizedUrlBuilder.build())
                .build();

        return chain.proceed(newRequest);
    }
}

