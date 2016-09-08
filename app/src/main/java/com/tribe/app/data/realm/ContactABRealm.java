package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 05/09/2016.
 */
public class ContactABRealm extends RealmObject implements ContactInterface {

    @PrimaryKey
    private String id;

    private String name;
    private RealmList<PhoneRealm> phones;
    private long lastTimeContacted;
    private int version;
    private RealmList<UserRealm> userList;
    private int howManyFriends;

    public ContactABRealm() {

    }

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

    public RealmList<PhoneRealm> getPhones() {
        return phones;
    }

    public void setPhones(RealmList<PhoneRealm> phones) {
        this.phones = phones;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public RealmList<UserRealm> getUserList() {
        return userList;
    }

    public void setUserList(RealmList<UserRealm> userList) {
        this.userList = userList;
    }

    @Override
    public void setHowManyFriends(int howManyFriends) {
        this.howManyFriends = howManyFriends;
    }

    @Override
    public int getHowManyFriends() {
        return howManyFriends;
    }

    @Override
    public void addUser(UserRealm userRealm) {
        if (this.userList == null) this.userList = new RealmList<>();

        this.userList.add(userRealm);
    }
}