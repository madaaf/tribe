package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 12/09/2016.
 */
public class FacebookEntity implements Serializable {

  private String id;
  private String profilePicture;
  private String email;
  private String name;

  public Integer getAgeRangeMin() {
    return ageRangeMin;
  }

  public void setAgeRangeMin(Integer ageRangeMin) {
    this.ageRangeMin = ageRangeMin;
  }

  public Integer getAgeRangeMax() {
    return ageRangeMax;
  }

  public void setAgeRangeMax(Integer ageRangeMax) {
    this.ageRangeMax = ageRangeMax;
  }

  private Integer ageRangeMin;
  private Integer ageRangeMax;

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
