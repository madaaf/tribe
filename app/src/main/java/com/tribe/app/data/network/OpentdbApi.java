package com.tribe.app.data.network;

import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface OpentdbApi {

  @GET("/api.php?type=multiple") Observable<List<TriviaQuestion>> getCategory(
      @Query("category") int category, @Query("amount") int amount);
}