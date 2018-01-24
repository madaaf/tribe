package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameBirdRushEngine extends GameEngine {

  public Level level;

  // VARIABLES

  // OBSERVABLES
  private Subscription popIntervalSubscription;
  private PublishSubject<Level> onLevelChange = PublishSubject.create();
  private PublishSubject<List<BirdRushObstacle>> onObstacle = PublishSubject.create();

  private List<BirdRushObstacle> obstacleList = new ArrayList<>();

  public enum Level {
    MEDIUM, HARD, EXTREME, ALIEN

  }

  public GameBirdRushEngine(Context context, Level level) {
    super(context);
    this.level = level;
  }

  public void popObstcale(int count) {
    if (popIntervalSubscription != null) popIntervalSubscription.unsubscribe();
    popIntervalSubscription = Observable.interval((2000), TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Timber.e("SOEF pop obstacle");
          generateObstacle(6);
          onObstacle.onNext(obstacleList);
          // popAlien(count + 1);
        });
  }

  public void generateObstacle(int nbr) {
    Timber.e("SOEF generate obstacle");
    if (!obstacleList.isEmpty()) {
      obstacleList.clear();
    }
    for (int i = 0; i < nbr; i++) {
      obstacleList.add(new BirdRushObstacle(context, level));
    }
  }

  @Override public void start() {
    Timber.d("Start game engine");
    super.start();
    popObstcale(0);
  }

  @Override public void stop() {
    super.stop();
  }

  /**
   * OBSERVABLES
   */

  public Observable<List<BirdRushObstacle>> onObstacle() {
    return onObstacle;
  }
}
