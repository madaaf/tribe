package com.tribe.app.data.network;

import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface OpentdbApi {

  @GET("/api.php?type=multiple") Observable<List<TriviaQuestions>> getCategory(
      @Query("category") int category, @Query("amount") int amount);
}