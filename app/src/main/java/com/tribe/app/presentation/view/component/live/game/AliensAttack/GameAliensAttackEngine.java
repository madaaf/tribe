package com.tribe.app.presentation.view.component.live.game.AliensAttack;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameAliensAttackEngine {

  public List<Level> levels;

  // VARIABLES
  private Context context;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Level> onLevelChange = PublishSubject.create();
  private PublishSubject<GameAliensAttackAlienView> onAlien = PublishSubject.create();

  public GameAliensAttackEngine(Context context) {
    this.context = context;
  }

  public enum Level {
    MEDIUM(0, 3f, 0.3f), HARD(20, 2f, 0.2f), EXTREME(50, 1.5f, 0.15f), ALIEN(90, 1f, 0.1f);

    private final int count;
    private final float popInterval;
    private final float speed;
    private Random rand;

    Level(int count, float popInterval, float speed) {
      this.count = count;
      this.popInterval = popInterval;
      this.speed = speed;
      this.rand = new Random();
    }

    public int getCount() {
      return count;
    }

    public int getPopInterval() {
      return (int) (popInterval + (rand.nextFloat() * 0.2f));
    }

    public @GameAliensAttackAlienView.AlienType int getType() {
      return rand.nextInt() * (1 - 0) + 0;
    }

    public int speed() {
      return (int) (speed + (rand.nextFloat() * 2));
    }

    public float startX() {
      return rand.nextFloat() * (0.95f - 0.15f) + 0.15f;
    }

    public float rotation() {
      return (float) ((rand.nextFloat() * (3 + 3) + 3) * Math.PI / 180);
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
    return levels;
  }

  public Level levelForCount(int points) {
    List<Level> levelList = getAllLevels();
    Collections.reverse(getAllLevels());
    for (Level level : levelList) {
      if (points > level.count) {
        return level;
      }
    }

    return levels.get(0);
  }

  public void popAlien(int count) {
    Level level = levelForCount(count);
    onLevelChange.onNext(level);

    subscriptions.add(Observable.timer(level.getPopInterval(), TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          GameAliensAttackAlienView viewAlien = new GameAliensAttackAlienView(context, level);
        }));
  }

  /**
   * OBSERVABLES
   */

  public Observable<Level> onLevelChange() {
    return onLevelChange;
  }

  public Observable<GameAliensAttackAlienView> onAlien() {
    return onAlien;
  }
}
