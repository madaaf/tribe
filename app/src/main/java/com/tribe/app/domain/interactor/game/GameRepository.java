package com.tribe.app.domain.interactor.game;

/**
 * Created by tiago on 04/05/2016.
 */

import java.util.List;
import rx.Observable;

public interface GameRepository {

  Observable<List<String>> getNamesForPostItGame(String lang);

  Observable<List<String>> getDataForChallengeGame(String lang);
}
