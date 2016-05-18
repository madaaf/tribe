package com.tribe.app.domain.entity;

/**
 * Created by tiago on 04/05/2016.
 *
 * Class that represents a Location in the domain layer.
 *
 */
public class Location {

    private double longitude;
    private double latitude;

    public Location(double longitude, double latitude) {
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** User Details *****\n");
        stringBuilder.append("longitude = " + longitude);
        stringBuilder.append("latitude = " + latitude);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
