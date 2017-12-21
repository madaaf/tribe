package com.tribe.app.data.network;

import com.tribe.tribelivesdk.game.trivia.TriviaQuestions;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public interface OpentdbApi {

  @GET("/api.php?type=multiple&encode=url3986") Observable<TriviaQuestions> getCategory(
      @Query("category") int category);
}