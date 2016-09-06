package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 05/09/2016.
 */
public class ContactFBRealm extends RealmObject implements ContactInterface {

    @PrimaryKey
    private String id;

    private String name;
    private String profilePicture;
    private RealmList<UserRealm> userList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public RealmList<UserRealm> getUserList() {
        return userList;
    }

    public void setUserList(RealmList<UserRealm> userList) {
        this.userList = userList;
    }
}
