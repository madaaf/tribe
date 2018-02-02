package com.tribe.app.presentation.view.component.live.game.birdrush;

import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Random;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRushObstacle {

  public static final String BIRD_OBSTACLE_TAG = "BIRD_OBSTACLE_TAG_";
  public static final int wiewWidth = 75;

  private final static String ID_KEY = "id";
  private final static String NEXT_SPAWN_KEY = "nextSpawn";
  private final static String START_RATIO_KEY = "start";
  private final static String HEIGHT_RATIO_KEY = "height";
  private final static String SPEED_KEY = "speed";
  private final static String TRANSLATION_KEY = "translation";
  private final static String TRANSLATION_X = "x";
  private final static String TRANSLATION_Y = "y";
  private final static String TRANSLATION_DURATION = "duration";
  private final static String ROTATION_KEY = "rotation";
  private final static String ROTATION_DURATION = "duration";
  private final static String ROTATION_ANGLE = "angle";

  private String id;
  private int index;
  private Double nextSpawn;
  private Float start; // position Y ratio
  private Float height;
  private Float speed; //
  private Translation translation; // en px
  private Rotation rotation;
  private int viewHeight = 0;

  private int x;
  private int y;

  public BirdRushObstacle() {
  }

  public BirdRushObstacle(GameBirdRushEngine.Level level, int widthScreen, int heightScreen) {
    this.id = id();
    this.nextSpawn = nextSpawnDelay(level);
    this.start = startYPos();
    this.height = heightRatio(level);
    this.speed = obstacleSpeed(level);
    this.translation = translation(level);
    this.viewHeight = Math.round(height * heightScreen);
    this.rotation = rotation(level);

    this.x = widthScreen + wiewWidth; // intial position: end of the screen
    this.y = heightScreen - Math.round((startYPos() * heightScreen) + (viewHeight / 2));
    // float height = (model.getStart() * screenUtils.getHeightPx() - model.getView().getHeight() / 2);
    init();
  }

  public void initParam(int widthScreen, int heightScreen) {
    this.x = widthScreen + wiewWidth;
    this.y = Math.round(startYPos() * heightScreen - height / 2);
    this.viewHeight = Math.round(height * heightScreen);
  }

  private void init() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setNextSpawn(Double nextSpawn) {
    this.nextSpawn = nextSpawn;
  }

  public void setStart(Float start) {
    this.start = start;
  }

  public void setHeight(Float height) {
    this.height = height;
  }

  public void setSpeed(Float speed) {
    this.speed = speed;
  }

  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  public void setRotation(Rotation rotation) {
    this.rotation = rotation;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setViewHeight(int viewHeight) {
    this.viewHeight = viewHeight;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
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

  public String getBirdId() {
    return id;
  }

  public Double getNextSpawn() {
    return nextSpawn;
  }

  public Float getStart() {
    return start;
  }

  public int getBirdHeight() {
    return viewHeight;
  }

  public int getBirdWidth() {
    return wiewWidth;
  }

  public Float getRelativeHeight() {
    return height;
  }

  public Float getSpeed() {
    return speed;
  }

  public Translation getBirdTranslation() {
    return translation;
  }

  public Rotation getBirdRotation() {
    return rotation;
  }

  public static class Translation {
    private Float x;
    private Float y;
    private Double duration;
    private Float currentTransflation;
    private int coef;

    public Translation(Float x, Float y, Double duration) {
      this.x = x;
      this.y = y;
      this.duration = duration;
      this.currentTransflation = 0f;
      this.coef = 1;
    }

    public int getCoef() {
      return coef;
    }

    public void setCoef(int coef) {
      this.coef = coef;
    }

    public Float getX() {
      return x;
    }

    public Float getY() {
      return y;
    }

    public Float getCurrentTransflation() {
      return currentTransflation;
    }

    public void setCurrentTransflation(Float currentTransflation) {
      this.currentTransflation = currentTransflation;
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
    private Float currentRotation;
    private int rotationSens;

    public Rotation(Float angle, Double duration) {
      this.duration = duration;
      this.angle = angle;
      this.currentRotation = -angle;
      this.rotationSens = 1;
    }

    public Float getAngle() {
      return angle;
    }

    public int getRotationSens() {
      return rotationSens;
    }

    public void setRotationSens(int rotationSens) {
      this.rotationSens = rotationSens;
    }

    public Float getCurrentRotation() {
      return currentRotation;
    }

    public void setCurrentRotation(Float currentRotation) {
      this.currentRotation = currentRotation;
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
        + ", getRelativeHeight="
        + height
        + ", speed="
        + speed
        + ", translation="
        + translation
        + ", rotation="
        + rotation
        + '}';
  }

  public static BirdRushObstacle ok(JSONObject json) {
    BirdRushObstacle obstacle = new BirdRushObstacle();
    try {
      obstacle.setId(json.getString(ID_KEY));
      obstacle.setNextSpawn(json.getDouble(NEXT_SPAWN_KEY));
      obstacle.setHeight(Float.parseFloat(json.getString(HEIGHT_RATIO_KEY)));
      obstacle.setStart(Float.parseFloat(json.getString(START_RATIO_KEY)));
      obstacle.setSpeed(Float.parseFloat(json.getString(SPEED_KEY)));
      json.getJSONObject(TRANSLATION_KEY);
      if (json.has(TRANSLATION_KEY)) {
        Float x = Float.parseFloat(json.getJSONObject(TRANSLATION_KEY).getString(TRANSLATION_X));
        Float y = Float.parseFloat(json.getJSONObject(TRANSLATION_KEY).getString(TRANSLATION_Y));
        Double duration = json.getJSONObject(TRANSLATION_KEY).getDouble(TRANSLATION_DURATION);
        Translation t = new Translation(x, y, duration);
        obstacle.setTranslation(t);
      }
      if (json.has(ROTATION_KEY)) {
        Float angle = Float.parseFloat(json.getJSONObject(ROTATION_KEY).getString(ROTATION_ANGLE));
        Double duration = json.getJSONObject(ROTATION_KEY).getDouble(ROTATION_DURATION);
        Rotation r = new Rotation(angle, duration);
        obstacle.setRotation(r);
      }
    } catch (JSONException ex) {
      ex.printStackTrace();
    }

    return obstacle;
  }

  public JSONObject obstacleAsJSON() {
    JSONObject obstacle = new JSONObject();
    JsonUtils.jsonPut(obstacle, ID_KEY, id);
    JsonUtils.jsonPut(obstacle, NEXT_SPAWN_KEY, nextSpawn);
    JsonUtils.jsonPut(obstacle, HEIGHT_RATIO_KEY, height);
    JsonUtils.jsonPut(obstacle, START_RATIO_KEY, start);
    JsonUtils.jsonPut(obstacle, SPEED_KEY, speed);

    if (translation != null) {
      JSONObject trans = new JSONObject();
      JsonUtils.jsonPut(trans, TRANSLATION_X, translation.getX());
      JsonUtils.jsonPut(trans, TRANSLATION_Y, translation.getY());
      JsonUtils.jsonPut(trans, TRANSLATION_DURATION, translation.getDuration());
      JsonUtils.jsonPut(obstacle, TRANSLATION_KEY, trans);
    }
    if (rotation != null) {
      JSONObject rot = new JSONObject();
      JsonUtils.jsonPut(rot, ROTATION_ANGLE, rotation.getAngle());
      JsonUtils.jsonPut(rot, ROTATION_DURATION, rotation.getDuration());
      JsonUtils.jsonPut(obstacle, ROTATION_KEY, rot);
    }

    return obstacle;
  }
}
