package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.tribe.app.data.network.entity.LoginEntity;
import java.util.LinkedList;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 10/06/2016.
 */
@Singleton public class PhoneUtils {

  public final static String COUNTRY_CODE_DEV = "KP";
  public final static String COUNTRY_PREFIX_DEV = "850";
  public final static String PHONE_PREFIX_DEV = "2121";

  private Context context;
  private PhoneNumberUtil phoneUtil;

  @Inject public PhoneUtils(Context context) {
    this.context = context;
    phoneUtil = PhoneNumberUtil.getInstance();
  }

  public boolean checkValidNumber(String phoneNumber, String countryCode) {
    String currentPhoneNumber = phoneNumber;

    int code = phoneUtil.getCountryCodeForRegion(countryCode);
    String tmpNum = "+" + code + " " + currentPhoneNumber;
    currentPhoneNumber = formatMobileNumber(tmpNum, countryCode);

    if (currentPhoneNumber == null) {
      currentPhoneNumber = formatMobileNumber(currentPhoneNumber, countryCode);
    }

    return currentPhoneNumber != null;
  }

  private boolean validate(String number, String countryCode) {
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
    Phonenumber.PhoneNumber phoneNumber = null;
    try {
      //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
      phoneNumber = phoneNumberUtil.parse(number, isoCode);
    } catch (NumberParseException e) {
      System.err.println(e);
    }

    boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
    if (isValid) {
      String internationalFormat =
          phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
      return true;
    } else {
      return false;
    }
  }

  public String formatMobileNumber(String number, String countryCode) {
    try {
      Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, countryCode);
      PhoneNumberUtil.PhoneNumberType type = phoneUtil.getNumberType(phoneNumber);
      boolean isMobile = type == PhoneNumberUtil.PhoneNumberType.MOBILE
          || type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE;

      if (phoneUtil.isValidNumber(phoneNumber) && isMobile) {
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
      }
    } catch (Exception e) {

    }

    return null;
  }

  public String formatMobileNumberForAddressBook(String number, String countryCode) {
    try {
      Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, countryCode);
      return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    } catch (Exception e) {

    }

    return null;
  }

  public String formatPhoneNumberForView(String number, String countryCode) {
    return PhoneNumberUtils.formatNumber(number, countryCode);
  }

  public String formatNumber(String number, int countryCode) {
    Phonenumber.PhoneNumber numberProto = getPhoneNumber(number, countryCode);
    String numberBis = null;

    if (numberProto != null) {
      numberBis = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    return numberBis;
  }

  public Phonenumber.PhoneNumber getPhoneNumber(String number, int codeCountry) {
    try {
      Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "");

      if (phoneUtil.isPossibleNumber(numberProto)) {
        return numberProto;
      }
    } catch (NumberParseException e) {
      try {
        Phonenumber.PhoneNumber numberProto =
            phoneUtil.parse(number, phoneUtil.getRegionCodeForCountryCode(codeCountry));

        if (phoneUtil.isPossibleNumber(numberProto)) {
          return numberProto;
        }
      } catch (NumberParseException e2) {
      }
    }

    return null;
  }

  public LoginEntity prepareLoginForRegister(LoginEntity loginEntity) {
    Phonenumber.PhoneNumber numberProto = null;

    try {
      numberProto = phoneUtil.parse(loginEntity.getUsername(), "");

      loginEntity.setCountryCode("+" + numberProto.getCountryCode());
      loginEntity.setNationalNumber("" + numberProto.getNationalNumber());

    } catch (NumberParseException e) {
      System.err.println("NumberParseException was thrown: " + e.toString());
    }

    return loginEntity;
  }

  public int getCountryCode(String phoneNumber) {
    Phonenumber.PhoneNumber numberProto = null;

    try {
      numberProto = phoneUtil.parse(phoneNumber, "");
    } catch (NumberParseException e) {
      System.err.println("NumberParseException was thrown: " + e.toString());
    }

    if (numberProto != null) return numberProto.getCountryCode();

    return 0;
  }

  public int getCountryCodeForRegion(String codeCountry) {
    return phoneUtil.getCountryCodeForRegion(codeCountry);
  }

  public String getRegionCodeForNumber(String phoneNumber) {
    Phonenumber.PhoneNumber numberProto = null;

    try {
      numberProto = phoneUtil.parse(phoneNumber, "");
    } catch (NumberParseException e) {
      System.err.println("NumberParseException was thrown: " + e.toString());
    }

    if (numberProto != null) {
      return phoneUtil.getRegionCodeForNumber(numberProto);
    } else {
      return "";
    }
  }

  public LinkedList<String> getSupportedRegions() {
    return new LinkedList<>(phoneUtil.getSupportedRegions());
  }

  public static boolean isDebugPhone(String countryCode, String phone) {
    return countryCode.equals(COUNTRY_CODE_DEV) && phone != null && phone.contains(
        PHONE_PREFIX_DEV);
  }
}
