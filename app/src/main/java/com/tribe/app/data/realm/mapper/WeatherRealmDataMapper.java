package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.WeatherRealm;
import com.tribe.app.domain.entity.Weather;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.WeatherRealm} (in the data layer)
 * to {@link com.tribe.app.domain.entity.Weather} in the
 * domain layer.
 */
@Singleton public class WeatherRealmDataMapper {

  @Inject public WeatherRealmDataMapper() {
  }

  /**
   * Transform a {@link com.tribe.app.data.realm.WeatherRealm} into an {@link
   * com.tribe.app.domain.entity.Weather}.
   *
   * @param weatherRealm Object to be transformed.
   * @return {@link com.tribe.app.domain.entity.Weather} if valid {@link
   * com.tribe.app.data.realm.WeatherRealm} otherwise null.
   */
  public Weather transform(WeatherRealm weatherRealm) {
    Weather weather = null;

    if (weatherRealm != null) {
      weather = new Weather();
      weather.setIcon(weatherRealm.getIcon());
      weather.setTempC(weatherRealm.getTempC());
      weather.setTempF(weatherRealm.getTempF());
    }

    return weather;
  }

  /**
   * Transform a {@link Weather} into an {@link WeatherRealm}.
   *
   * @param weather Object to be transformed.
   * @return {@link WeatherRealm} if valid {@link Weather} otherwise null.
   */
  public WeatherRealm transform(Weather weather) {
    WeatherRealm weatherRealm = null;

    if (weather != null) {
      weatherRealm = new WeatherRealm();
      weatherRealm.setTempF(weather.getTempF());
      weatherRealm.setTempC(weather.getTempC());
      weatherRealm.setIcon(weather.getIcon());
    }

    return weatherRealm;
  }
}
