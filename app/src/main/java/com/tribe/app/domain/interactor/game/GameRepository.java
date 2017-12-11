package com.tribe.app.domain.interactor.game;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.domain.entity.Score;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import rx.Observable;

public interface GameRepository {

  Observable<Void> synchronizeGamesData(String lang);

  Observable<List<Game>> getGames();

  Observable<List<Score>> getGameLeaderBoard(String gameId, boolean friendsOnly, int offset);

  Observable<List<Score>> getUserLeaderboard(String userId);

  Observable<Void> addScore(String gameId, Integer score);
}
