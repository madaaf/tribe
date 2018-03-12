package com.tribe.app.domain.entity;

/**
 * Created by madaaflak on 07/03/2018.
 */

public class PokeTiming {

  private String id;
  private Long creationDate;

  public PokeTiming(String id, Long creationDate) {
    this.id = id;
    this.creationDate = creationDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  @Override public String toString() {
    return "PokeTiming{" + "id='" + id + '\'' + ", creationDate=" + creationDate + '}';
  }
}
