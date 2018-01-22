package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRushObstacle {

  private String id;
  private Double nextSpawn;
  private Float start; // position Y ratio
  private Float height;
  private Float speed; // sec/
  private Translation translation; // en px
  private Rotation rotation;

  public BirdRushObstacle(String id, Double nextSpawn, Float start, Float height, Float speed,
      Translation translation, Rotation rotation) {
    this.id = id;
    this.nextSpawn = nextSpawn;
    this.start = start;
    this.height = height;
    this.speed = speed;
    this.translation = translation;
    this.rotation = rotation;
  }

  public String getId() {
    return id;
  }

  public Double getNextSpawn() {
    return nextSpawn;
  }

  public Float getStart() {
    return start;
  }

  public Float getHeight() {
    return height;
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

  @Override public String toString() {
    return "BirdRushObstacle{"
        + "id='"
        + id
        + '\''
        + ", nextSpawn="
        + nextSpawn
        + ", start="
        + start
        + ", height="
        + height
        + ", speed="
        + speed
        + ", translation="
        + translation
        + ", rotation="
        + rotation
        + '}';
  }
}
