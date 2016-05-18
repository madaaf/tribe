package com.tribe.app.data.realm;

import com.tribe.app.domain.entity.Location;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 04/05/2016.
 */
public class UserRealm extends RealmObject {

    @PrimaryKey
    private int id;

    private String displayName;
    private String pinCode;
    private String countryCode;
    private String phoneNumber;
    private String profilePicture;
    private String score;
    private String email;
    private boolean emailVerified;
    private boolean isReal;
    private boolean isInvited;
    private LocationRealm location;
    private boolean disableSaveTribe;
    private boolean shouldSync;
    private boolean hidePinCode;
    private Date createdAt;
    private Date updatedAt;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isReal() {
        return isReal;
    }

    public void setReal(boolean real) {
        isReal = real;
    }

    public LocationRealm getLocation() {
        return location;
    }

    public void setLocation(LocationRealm location) {
        this.location = location;
    }

    public boolean isInvited() {
        return isInvited;
    }

    public void setInvited(boolean invited) {
        isInvited = invited;
    }

    public boolean isDisableSaveTribe() {
        return disableSaveTribe;
    }

    public void setDisableSaveTribe(boolean disableSaveTribe) {
        this.disableSaveTribe = disableSaveTribe;
    }

    public boolean isShouldSync() {
        return shouldSync;
    }

    public void setShouldSync(boolean shouldSync) {
        this.shouldSync = shouldSync;
    }

    public boolean isHidePinCode() {
        return hidePinCode;
    }

    public void setHidePinCode(boolean hidePinCode) {
        this.hidePinCode = hidePinCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
