package com.tribe.app.data.network;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface FileApi {

    @Streaming
    @GET
    Observable<ResponseBody> downloadFileWithUrl(@Url String fileUrl);
}