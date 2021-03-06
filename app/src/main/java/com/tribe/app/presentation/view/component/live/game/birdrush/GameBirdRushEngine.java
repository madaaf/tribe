package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.utils.ScreenUtils;
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

  private ScreenUtils screenUtils;
  private Double spanDelay;

  // OBSERVABLES
  private Subscription popIntervalSubscription;
  private PublishSubject<Level> onLevelChange = PublishSubject.create();
  private PublishSubject<List<BirdRushObstacle>> onObstacle = PublishSubject.create();
  private PublishSubject<List<BirdRushObstacle>> onPlayerTap = PublishSubject.create();

  private List<BirdRushObstacle> obstacleList = new ArrayList<>();

  public enum Level {
    MEDIUM, HARD, EXTREME, ALIEN

  }

  public GameBirdRushEngine(Context context, Level level, ScreenUtils screenUtils) {
    super(context);
    this.level = level;
    this.screenUtils = screenUtils;
  }

  public void popObstcale() {
    if (popIntervalSubscription == null) {
      popIntervalSubscription = Observable.interval((5000), TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            generateObstacle(6);
            onObstacle.onNext(obstacleList);
          });
    }
  }

  public void generateObstacle(int nbr) {
    if (!obstacleList.isEmpty()) {
      obstacleList.clear();
    }
    for (int i = 0; i < nbr; i++) {
      obstacleList.add(
          new BirdRushObstacle(level, screenUtils.getWidthPx(), screenUtils.getHeightPx(), i));
    }
  }

  @Override public void start() {
    super.start();
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  @Override public void stop() {
    super.stop();
    if (popIntervalSubscription != null) popIntervalSubscription.unsubscribe();
  }

  /**
   * OBSERVABLES
   */

  public Observable<List<BirdRushObstacle>> onObstacle() {
    return onObstacle;
  }
}
