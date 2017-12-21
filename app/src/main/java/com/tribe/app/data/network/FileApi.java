package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.GameDataEntity;
import com.tribe.app.data.network.entity.TriviaCategoriesHolder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface FileApi {

  @GET @Streaming Call<ResponseBody> downloadFileWithUrl(@Url String fileUrl);

  @GET Observable<GameDataEntity> getDataForUrl(@Url String fileUrl);

  @GET("https://static.tribe.pm/games/trivia_fr.json")
  Observable<TriviaCategoriesHolder> getTriviaData();
}