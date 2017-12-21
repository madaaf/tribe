package com.tribe.app.data.network;

import com.tribe.tribelivesdk.game.trivia.TriviaCategoryEnum;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface OpentdbApi {

  @GET("/api.php?type=multiple&encode=url3986") Observable<TriviaCategoryEnum> getCategory(@Query("category") int category);

  @GET Observable<List<String>> getDataForUrl(@Url String fileUrl);
}