package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 06/05/2016.
 */
public class LocationRealm extends RealmObject {

  @PrimaryKey private String id;

  private double longitude;
  private double latitude;
  private String city;
  private String countryCode;
  private boolean hasLocation = false;

  public LocationRealm() {

  }

  public LocationRealm(double longitude, double latitude) {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public boolean hasLocation() {
    return hasLocation;
  }

  public void setHasLocation(boolean hasLocation) {
    this.hasLocation = hasLocation;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
