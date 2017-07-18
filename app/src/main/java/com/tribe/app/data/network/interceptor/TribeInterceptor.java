package com.tribe.app.data.network.interceptor;

import android.content.Context;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.util.TribeApiUtils;
import com.tribe.app.presentation.utils.StringUtils;
import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tiago on 12/04/2017.
 */

public class TribeInterceptor implements Interceptor {

  private TribeAuthorizer tribeAuthorizer;
  private Context context;

  public TribeInterceptor(Context context, TribeAuthorizer tribeAuthorizer) {
    this.context = context;
    this.tribeAuthorizer = tribeAuthorizer;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    if (tribeAuthorizer == null || tribeAuthorizer.getAccessToken() == null || StringUtils.isEmpty(
        tribeAuthorizer.getAccessToken().getAccessToken())) {
      return new okhttp3.Response.Builder().code(600).request(chain.request()).build();
    }

    Request original = chain.request();
    List<String> customAnnotations = original.headers("@");

    // Avoid anonymous requests, excepted on getRoomParameters.
    if (tribeAuthorizer.getAccessToken().isAnonymous() &&
            !customAnnotations.contains("CanBeAnonymous")) {

      return new okhttp3.Response.Builder().code(600).request(chain.request()).build();
    }

    Request.Builder requestBuilder =
        original.newBuilder().header("Content-type", "application/json").removeHeader("@");

    requestBuilder.header("Authorization",
        tribeAuthorizer.getAccessToken().getTokenType() + " " + tribeAuthorizer.getAccessToken()
            .getAccessToken());
    TribeApiUtils.appendUserAgent(context, requestBuilder);
    requestBuilder.method(original.method(), original.body());

    Request request = requestBuilder.build();
    return chain.proceed(request);
  }
}
