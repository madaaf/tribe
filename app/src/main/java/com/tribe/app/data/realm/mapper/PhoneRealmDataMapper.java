package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.presentation.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton public class PhoneRealmDataMapper {

  @Inject public PhoneRealmDataMapper() {

  }

  /**
   * Transform a {@link com.tribe.app.data.realm.PhoneRealm} into an {@link String}.
   *
   * @param phoneRealm Object to be transformed.
   * @return {@link String} if valid {@link PhoneRealm} otherwise null.
   */
  public String transform(PhoneRealm phoneRealm) {
    String phone = null;

    if (phoneRealm != null) {
      phone = phoneRealm.getPhone();
    }

    return phone;
  }

  /**
   * Transform a {String} into an {@link PhoneRealm}.
   *
   * @param phone Object to be transformed.
   * @return {@link String} if valid {@link PhoneRealm} otherwise null.
   */
  public PhoneRealm transform(String phone) {
    PhoneRealm phoneRealm = null;

    if (!StringUtils.isEmpty(phone)) {
      phoneRealm = new PhoneRealm();
      phoneRealm.setPhone(phone);
    }

    return phoneRealm;
  }

  /**
   * Transform a List of {@link PhoneRealm} into a Collection of {@link String}.
   *
   * @param phoneCollection Object Collection to be transformed.
   * @return {@link List <String>} if valid {@link Collection <PhoneRealm>} otherwise null.
   */
  public List<String> transform(List<PhoneRealm> phoneCollection) {
    List<String> phoneList = new ArrayList<>();
    String phone;
    for (PhoneRealm phoneRealm : phoneCollection) {
      phone = transform(phoneRealm);
      if (phone != null) {
        phoneList.add(phone);
      }
    }

    return phoneList;
  }

  /**
   * Transform a List of {@link String} into a Collection of {@link PhoneRealm}.
   *
   * @param phoneCollection Object Collection to be transformed.
   * @return {@link List <String>} if valid {@link Collection <PhoneRealm>} otherwise null.
   */
  public RealmList<PhoneRealm> transform(Collection<String> phoneCollection) {
    RealmList<PhoneRealm> phoneList = new RealmList<>();
    PhoneRealm phoneRealm;
    for (String phone : phoneCollection) {
      phoneRealm = transform(phone);
      if (phoneRealm != null) {
        phoneList.add(phoneRealm);
      }
    }

    return phoneList;
  }
}
