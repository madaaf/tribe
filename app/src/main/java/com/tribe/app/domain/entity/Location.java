package com.tribe.app.domain.entity;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.Distance;

import java.io.Serializable;

/**
 * Created by tiago on 04/05/2016.
 *
 * Class that represents a Location in the domain layer.
 *
 */
public class Location implements Serializable {

    private String id;
    private double longitude;
    private double latitude;
    private String city;
    private boolean hasLocation;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    /***
     *
     * @param distanceUnits the type of distance to display
     * @param locationDestDomain the location destination
     * @return formatted string to display
     */
    public String distanceTo(Context context, String distanceUnits, Location locationDestDomain) {
        String result = "";

        if (hasLocation && !locationDestDomain.hasLocation()) {
            result = context.getString(R.string.tribe_distance_enable);
        } else if (hasLocation && locationDestDomain.hasLocation()) {
            android.location.Location locationFrom = new android.location.Location("");
            locationFrom.setLongitude(this.getLongitude());
            locationFrom.setLatitude(this.getLatitude());

            android.location.Location locationDest = new android.location.Location("");
            locationDest.setLatitude(locationDestDomain.getLatitude());
            locationDest.setLongitude(locationDestDomain.getLongitude());

            float distance = locationFrom.distanceTo(locationDest); // IN METERS

            if (distanceUnits.equals(Distance.MILES)) {
                distance = (float) (distance * 0.000621371192);
                result = distance > 0.1 ? ((int) (distance * 0.000621371192)) + " mi" : context.getString(R.string.tribe_distance_nearby);
            } else {
                int km = (int) (distance / 1000);

                result = distance < 1000 && distance > 100 ? distance + " m" : (km >= 1 ? km + " km" : context.getString(R.string.tribe_distance_nearby));
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** User Details *****\n");
        stringBuilder.append("longitude = " + longitude);
        stringBuilder.append("latitude = " + latitude);
        stringBuilder.append("city = " + city);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
