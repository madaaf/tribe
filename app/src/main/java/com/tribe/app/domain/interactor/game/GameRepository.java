package com.tribe.app.domain.interactor.game;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import rx.Observable;

public interface GameRepository {

  Observable<Void> synchronizeGamesData(String lang);

  Observable<List<Game>> getGames();
}
