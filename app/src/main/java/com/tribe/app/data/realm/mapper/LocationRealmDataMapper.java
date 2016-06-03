package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.domain.entity.Location;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.LocationRealm} (in the data layer) to {@link com.tribe.app.domain.entity.Location} in the
 * domain layer.
 */
@Singleton
public class LocationRealmDataMapper {

    @Inject
    public LocationRealmDataMapper() {}

    /**
     * Transform a {@link LocationRealm} into an {@link Location}.
     *
     * @param locationRealm Object to be transformed.
     * @return {@link Location} if valid {@link LocationRealm} otherwise null.
     */
    public Location transform(LocationRealm locationRealm) {
        Location location = null;

        if (locationRealm != null) {
            location = new Location(locationRealm.getLongitude(), locationRealm.getLatitude());
        }

        return location;
    }
}
