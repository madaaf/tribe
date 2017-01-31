package com.tribe.app.data.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileApi {

  @GET @Streaming Call<ResponseBody> downloadFileWithUrl(@Url String fileUrl);
}