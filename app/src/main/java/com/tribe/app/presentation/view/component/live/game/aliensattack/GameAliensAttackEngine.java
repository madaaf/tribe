package com.tribe.app.presentation.view.component.live.game.aliensattack;

import android.content.Context;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameAliensAttackEngine extends GameEngine {

  public List<Level> levels;

  // VARIABLES

  // OBSERVABLES
  private Subscription popIntervalSubscription;
  private PublishSubject<Level> onLevelChange = PublishSubject.create();
  private PublishSubject<GameAliensAttackAlienView> onAlien = PublishSubject.create();

  public GameAliensAttackEngine(Context context) {
    super(context);
  }


  public enum Level {
    MEDIUM(0, 3f, 0.3f), HARD(20, 2f, 0.2f), EXTREME(50, 1.5f, 0.15f), ALIEN(90, 1f, 0.1f);

    private final int count;
    private final float popInterval;
    private final float speed;
    private Random rand;

    Level(int count, float speed, float popInterval) {
      this.count = count;
      this.popInterval = popInterval;
      this.speed = speed;
      this.rand = new Random();
    }

    public int getCount() {
      return count;
    }

    public float getPopInterval() {
      return popInterval + (rand.nextFloat() * 0.2f);
    }

    public @GameAliensAttackAlienView.AlienType int getType() {
      // generates random number between 0 and 1
      return (int) Math.round(Math.random());
    }

    public float speed() {
      return speed + (rand.nextFloat() * 2);
    }

    public float startX() {
      return rand.nextFloat() * (0.85f - 0.15f) + 0.15f;
    }

    public float rotation() {
      return rand.nextFloat() * 3;
    }

    public float scale() {
      return 1.2f;
    }
  }

  public List<Level> getAllLevels() {
    if (levels != null) return levels;
    levels = new ArrayList<>();
    levels.add(Level.MEDIUM);
    levels.add(Level.HARD);
    levels.add(Level.EXTREME);
    levels.add(Level.ALIEN);
    Collections.reverse(levels);
    return levels;
  }

  public Level levelForCount(int points) {
    List<Level> levelList = getAllLevels();

    for (Level level : levelList) {
      if (points > level.count) {
        return level;
      }
    }

    return levels.get(0);
  }

  public void popAlien(int count) {
    Timber.d("Pop alien : " + count);
    Level level = levelForCount(count);
    onLevelChange.onNext(level);

    if (popIntervalSubscription != null) popIntervalSubscription.unsubscribe();
    popIntervalSubscription =
        Observable.timer((int) (level.getPopInterval() * 1000), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              GameAliensAttackAlienView viewAlien = new GameAliensAttackAlienView(context, level);
              onAlien.onNext(viewAlien);
              popAlien(count + 1);
            });
  }

  @Override public void start() {
    Timber.d("Start game engine");
    super.start();
  }

  @Override public void stop() {
    super.stop();
    if (popIntervalSubscription != null) popIntervalSubscription.unsubscribe();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Level> onLevelChange() {
    return onLevelChange.distinctUntilChanged();
  }

  public Observable<GameAliensAttackAlienView> onAlien() {
    return onAlien;
  }
}
