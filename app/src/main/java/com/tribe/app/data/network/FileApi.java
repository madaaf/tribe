package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.GameDataEntity;
import com.tribe.app.data.network.entity.TriviaCategoriesHolder;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface FileApi {

  @GET @Streaming Call<ResponseBody> downloadFileWithUrl(@Url String fileUrl);

  @GET("/assets/support.json") Observable<List<Conversation>> getMessageSupport();

  @GET Observable<GameDataEntity> getDataForUrl(@Url String fileUrl);

  @GET("https://static.tribe.pm/games/trivia_fr.json")
  Observable<TriviaCategoriesHolder> getTriviaData();

  @GET("https://static.tribe.pm/games/song-pop.json")
  Observable<List<BattleMusicPlaylist>> getBattleMusicData();
}


