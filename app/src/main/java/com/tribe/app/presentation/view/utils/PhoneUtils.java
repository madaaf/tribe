package com.tribe.app.presentation.view.utils;

import android.content.Context;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 10/06/2016.
 */
@Singleton
public class PhoneUtils {

    private Context context;
    private PhoneNumberUtil phoneUtil;

    @Inject
    public PhoneUtils(Context context) {
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

    public String formatMobileNumber(String number, String countryCode) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, countryCode);
            PhoneNumberUtil.PhoneNumberType type = phoneUtil.getNumberType(phoneNumber);
            boolean isMobile = type == PhoneNumberUtil.PhoneNumberType.MOBILE || type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE;

            if (phoneUtil.isValidNumber(phoneNumber) && (isMobile || type == PhoneNumberUtil.PhoneNumberType.UNKNOWN)) {
                return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    public String prepareForScope(String phoneNumber) {
        Phonenumber.PhoneNumber numberProto = null;

        try {
            numberProto = phoneUtil.parse(phoneNumber, "");
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return "+" + numberProto.getCountryCode() + " " + numberProto.getNationalNumber();
    }

    public int getCountryCodeForRegion(String codeCountry) {
        return phoneUtil.getCountryCodeForRegion(codeCountry);
    }

    public LinkedList<String> getSupportedRegions() {
        return new LinkedList<>(phoneUtil.getSupportedRegions());
    }
}
