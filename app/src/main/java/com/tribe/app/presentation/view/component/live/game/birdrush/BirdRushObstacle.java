package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRushObstacle {

  private final String ID_KEY = "id";
  private final String NEXT_SPAWN_KEY = "nextSpawn";
  private final String START_RATIO_KEY = "start";
  private final String HEIGHT_RATIO_KEY = "height";
  private final String SPEED_KEY = "speed";
  private final String TRANSLATION_KEY = "translation";
  private final String ROTATION_KEY = "rotation";

  private String id;
  private Double nextSpawnDelay;
  private Float start; // position Y ratio
  private Float heightRatio;
  private Float speed; // sec/
  private Translation translation; // en px
  private Rotation rotation;

  public BirdRushObstacle(String id, Double nextSpawnDelay, Float start, Float heightRatio,
      Float speed, Translation translation, Rotation rotation) {
    this.id = id;
    this.nextSpawnDelay = nextSpawnDelay;
    this.start = start;
    this.heightRatio = heightRatio;
    this.speed = speed;
    this.translation = translation;
    this.rotation = rotation;
  }

  public String getId() {
    return id;
  }

  public Double getNextSpawnDelay() {
    return nextSpawnDelay;
  }

  public Float getStart() {
    return start;
  }

  public Float getHeightRatio() {
    return heightRatio;
  }

  public Float getSpeed() {
    return speed;
  }

  public Translation getTranslation() {
    return translation;
  }

  public Rotation getRotation() {
    return rotation;
  }

  public static class Translation {
    private Float x;
    private Float y;
    private Double Duration;

    public Translation(Float x, Float y, Double duration) {
      this.x = x;
      this.y = y;
      Duration = duration;
    }

    public Float getX() {
      return x;
    }

    public Float getY() {
      return y;
    }

    public Double getDuration() {
      return Duration;
    }
  }

  public static class Rotation {
    private Float angle;
    private Double Duration;

    public Rotation(Float angle, Double duration) {
      this.angle = angle;
      Duration = duration;
    }

    public Float getAngle() {
      return angle;
    }

    public Double getDuration() {
      return Duration;
    }
  }

  public void init() {

  }
}
