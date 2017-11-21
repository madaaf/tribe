package com.tribe.app.data.network;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface FileApi {

  @GET @Streaming Call<ResponseBody> downloadFileWithUrl(@Url String fileUrl);

  @GET Observable<List<String>> getDataForUrl(@Url String fileUrl);
}