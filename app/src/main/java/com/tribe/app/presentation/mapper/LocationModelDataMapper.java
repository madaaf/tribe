package com.tribe.app.presentation.mapper;

import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.presentation.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * Mapper class used to transform {@link com.tribe.app.domain.entity.Location} (in the domain layer) to {@link com.tribe.app.data.realm.LocationRealm} in the
 * presentation layer.
 */
@PerActivity
public class LocationModelDataMapper {

    @Inject
    public LocationModelDataMapper() {}

    /**
     * Transform a {@link com.tribe.app.domain.entity.Location} into an {@link com.tribe.app.data.realm.LocationRealm}.
     *
     * @param location Object to be transformed.
     * @return {@link LocationRealm}.
     */
    public LocationRealm transform(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Cannot transform a null value");
        }

        return new LocationRealm(location.getLongitude(), location.getLatitude());
    }
}
