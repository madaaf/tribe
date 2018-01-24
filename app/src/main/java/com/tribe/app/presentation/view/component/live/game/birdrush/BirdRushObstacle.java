package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;
import org.json.JSONObject;

import static com.tribe.app.presentation.view.component.live.game.birdrush.GameBirdRushView.SPEED_BACK_SCROLL;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRushObstacle extends FrameLayout {

  public static final String BIRD_OBSTACLE_TAG = "BIRD_OBSTACLE_TAG_";

  private final String ID_KEY = "id";
  private final String NEXT_SPAWN_KEY = "nextSpawn";
  private final String START_RATIO_KEY = "start";
  private final String HEIGHT_RATIO_KEY = "getHeightOb";
  private final String SPEED_KEY = "speed";
  private final String TRANSLATION_KEY = "translation";
  private final String ROTATION_KEY = "rotation";

  private Context context;
  private String id;
  private Double nextSpawn;
  private Float start; // position Y ratio
  private Float height;
  private Float speed; //
  private Translation translation; // en px
  private Rotation rotation;

  private ImageView view;
  private int viewHeight = 0;
  private int wiewWidth = 75;

  @Inject ScreenUtils screenUtils;

  public BirdRushObstacle(@NonNull Context context) {
    super(context);
    this.context = context;
    init();
  }

  public BirdRushObstacle(@NonNull Context context, GameBirdRushEngine.Level level) {
    super(context);
    this.context = context;
    this.id = id();
    this.nextSpawn = nextSpawnDelay(level);
    this.start = startYPos();
    this.height = heightRatio(level);
    this.speed = obstacleSpeed(level);
    this.translation = translation(level);
    this.rotation = rotation(level);
    init();
  }

  private void init() {
    initDependencyInjector();
    initView();
  }

  int index = 0;

  public void initView() {
    view = new ImageView(context);
    view.setTag(BIRD_OBSTACLE_TAG + index);
    index++;
    view.setScaleType(ImageView.ScaleType.FIT_XY);
    view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle));
    viewHeight = Math.round(height * screenUtils.getHeightPx());

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.height = 1000;
    params.width = 75;
    view.setLayoutParams(params);
  }

  public void animateObstacle() {
    view.animate()
        .translationX(-screenUtils.getWidthPx() - 2 * wiewWidth)
        .setDuration(SPEED_BACK_SCROLL)
        .start();

    // TRANSLATION
    if (translation != null) {
      ValueAnimator trans = ValueAnimator.ofFloat(view.getY(), view.getY() - translation.getY(),
          view.getY() + translation.getY());
      trans.setDuration(1000);
      trans.addUpdateListener(animation -> {
        Float value = (float) animation.getAnimatedValue();
        view.setY(value);
      });
      trans.setRepeatCount(Animation.INFINITE);
      trans.setRepeatMode(ValueAnimator.REVERSE);
      trans.start();
    }

    // ROTATION
    if (rotation != null) {
      ValueAnimator rotationAnim =
          ValueAnimator.ofFloat(0f, -rotation.getAngle(), 0f, rotation.getAngle());
      rotationAnim.setDuration(5000);
      rotationAnim.addUpdateListener(animation -> {
        Float value = (float) animation.getAnimatedValue();
        view.setPivotX(wiewWidth / 2);
        view.setPivotY(viewHeight / 2);
        view.setRotation(value);
      });
      rotationAnim.setInterpolator(new LinearInterpolator());
      rotationAnim.setRepeatCount(Animation.INFINITE);
      rotationAnim.setRepeatMode(ValueAnimator.REVERSE);
      rotationAnim.start();
    }
  }

  public void stopAnimation() {
    view.animate().cancel();
  }

  public ImageView getView() {
    return view;
  }

  /**
   * PUBLIC
   */

  private String id() {
    return UUID.randomUUID().toString();
  }

  private Float startYPos() {
    return randFloat(0.15f, 0.95f);
  }

  private Double nextSpawnDelay(GameBirdRushEngine.Level level) {
    switch (level) {
      case MEDIUM:
        return 1.5D;
      case HARD:
        return 1.4D;
      case EXTREME:
        return 1.3D;
      case ALIEN:
        return 1.2D;
    }

    return null;
  }

  private BirdRushObstacle.Translation translation(GameBirdRushEngine.Level level) {
    switch (level) {
      case MEDIUM:
        return new BirdRushObstacle.Translation(0f, randFloat(20f, 25f), randDouble(0.9d, 1.1d));
      case HARD:
        return new BirdRushObstacle.Translation(0f, randFloat(25f, 30f), randDouble(0.7d, 0.9d));
      case EXTREME:
        return new BirdRushObstacle.Translation(0f, randFloat(30f, 35f), randDouble(0.6d, 0.7d));
      case ALIEN:
        return new BirdRushObstacle.Translation(0f, randFloat(35f, 40f), randDouble(0.5d, 0.6d));
    }

    return null;
  }

  private BirdRushObstacle.Rotation rotation(GameBirdRushEngine.Level level) {

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

  private Float heightRatio(GameBirdRushEngine.Level level) {
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

  private Float obstacleSpeed(GameBirdRushEngine.Level level) {
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

  /**
   * PUBLIC
   */

  public String getIdOb() {
    return id;
  }

  public Double getNextSpawn() {
    return nextSpawn;
  }

  public Float getStart() {
    return start;
  }

  public int getViewHeight() {
    return viewHeight;
  }

  public int getWiewWidth() {
    return wiewWidth;
  }

  public Float getHeightOb() {
    return height;
  }

  public Float getSpeed() {
    return speed;
  }

  public Translation getTranslationObs() {
    return translation;
  }

  public Rotation getRotationObs() {
    return rotation;
  }

  public static class Translation {
    private Float x;
    private Float y;
    private Double duration;

    public Translation(Float x, Float y, Double duration) {
      this.x = x;
      this.y = y;
      this.duration = duration;
    }

    public Float getX() {
      return x;
    }

    public Float getY() {
      return y;
    }

    public Double getDuration() {
      return duration;
    }

    @Override public String toString() {
      return "Translation{" + "x=" + x + ", y=" + y + ", duration=" + duration + '}';
    }
  }

  public static class Rotation {
    private Double duration;
    private Float angle;

    public Rotation(Float angle, Double duration) {
      this.duration = duration;
      this.angle = angle;
    }

    public Float getAngle() {
      return angle;
    }

    public Double getDuration() {
      return duration;
    }

    @Override public String toString() {
      return "Rotation{" + "angle=" + angle + ", duration=" + duration + '}';
    }
  }

  /**
   * OVERRIDE
   */

  @Override public String toString() {
    return "BirdRushObstacle{"
        + "id='"
        + id
        + '\''
        + ", nextSpawn="
        + nextSpawn
        + ", start="
        + start
        + ", getHeightOb="
        + height
        + ", speed="
        + speed
        + ", translation="
        + translation
        + ", rotation="
        + rotation
        + '}';
  }

  public JSONObject asJSON() {
    JSONObject obstacle = new JSONObject();
    JsonUtils.jsonPut(obstacle, ID_KEY, id);
    JsonUtils.jsonPut(obstacle, NEXT_SPAWN_KEY, nextSpawn);
    JsonUtils.jsonPut(obstacle, ROTATION_KEY, rotation);
    JsonUtils.jsonPut(obstacle, HEIGHT_RATIO_KEY, height);
    JsonUtils.jsonPut(obstacle, START_RATIO_KEY, start);
    JsonUtils.jsonPut(obstacle, SPEED_KEY, speed);
    JsonUtils.jsonPut(obstacle, TRANSLATION_KEY, translation);
    return obstacle;
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
