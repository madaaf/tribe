package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by tiago on 06/05/2016.
 */
public class LocationRealm extends RealmObject {

    private double longitude;
    private double latitude;

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
}
