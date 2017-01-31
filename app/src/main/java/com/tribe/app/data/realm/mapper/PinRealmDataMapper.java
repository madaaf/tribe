package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.domain.entity.Pin;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton public class PinRealmDataMapper {

  @Inject public PinRealmDataMapper() {

  }

  /**
   * Transform a {@link com.tribe.app.data.realm.PinRealm} into an {@link
   * com.tribe.app.domain.entity.Pin}.
   *
   * @param pinRealm Object to be transformed.
   * @return {@link com.tribe.app.domain.entity.Pin} if valid {@link com.tribe.app.data.realm.PinRealm}
   * otherwise null.
   */
  public Pin transform(PinRealm pinRealm) {
    Pin pin = null;

    if (pinRealm != null) {
      pin = new Pin();
      pin.setPinId(pinRealm.getPinId());
      pin.setNcStatus(pinRealm.getNcStatus());
      pin.setSmsStatus(pinRealm.getSmsStatus());
      pin.setTo(pinRealm.getTo());
    }

    return pin;
  }
}
