package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import com.tribe.app.presentation.view.component.live.game.aliensattack.GameAliensAttackAlienView;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import java.util.Random;
import rx.Subscription;
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
  private PublishSubject<GameAliensAttackAlienView> onAlien = PublishSubject.create();

  public GameBirdRushEngine(Context context) {
    super(context);
  }

  public enum Level {
    MEDIUM(0, 3f, 0.3f), HARD(20, 2f, 0.2f), EXTREME(50, 1.5f, 0.15f), ALIEN(90, 1f, 0.1f);

    Level(int count, float speed, float popInterval) {

    }

  }

  private void translation() {

  }

  private void id() {

  }

  private Float startYPos() {
    return randFloat(0.15f, 0.95f);
  }

  private BirdRushObstacle.Rotation k() {

    switch (level) {
      case MEDIUM:
        return new BirdRushObstacle.Rotation(5f, randDouble(0.5d, 0.6d));
      case HARD:
        return new BirdRushObstacle.Rotation(5f, randDouble(0.4d, 0.5d));
      case EXTREME:
        return new BirdRushObstacle.Rotation(5f, randDouble(0.4d, 0.4d));
      case ALIEN:
        return new BirdRushObstacle.Rotation(5f, randDouble(0.2d, 0.3d));
    }

    return null;
  }

  private Float heightRatio() {
    switch (level) {
      case MEDIUM:
        return randFloat(0.25f, 0.3f);
      case HARD:
        return randFloat(0.25f, 0.35f);
      case EXTREME:
        return randFloat(0.25f, 0.4f);
      case ALIEN:
        return randFloat(0.25f, 0.45f);
    }
    return randFloat(0.25f, 0.3f);
  }

  private Float obstacleSpeed() {
    switch (level) {
      case MEDIUM:
        return 0.008f;
      case HARD:
        return 0.007f;
      case EXTREME:
        return 0.006f;
      case ALIEN:
        return 0.005f;
    }
    return 0.005f;
  }

  public static Double randDouble(double min, double max) {
    Random r = new Random();
    return min + r.nextDouble() * (max - min);
  }

  public static float randFloat(float min, float max) {
    Random r = new Random();
    return min + r.nextFloat() * (max - min);
  }

  @Override public void start() {
    Timber.d("Start game engine");
    super.start();
  }

  @Override public void stop() {
    super.stop();
  }

  /**
   * OBSERVABLES
   */

}
