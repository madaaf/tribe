package com.tribe.app.domain.entity;

import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public class Contact implements Comparable<Contact> {

    protected String id;
    protected String name;
    protected List<User> userList;
    protected int howManyFriends;

    public Contact(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Contact another) {
        return name != null && another.name != null ? name.compareToIgnoreCase(another.name) : -1;
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

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public int getHowManyFriends() {
        return howManyFriends;
    }

    public void setHowManyFriends(int howManyFriends) {
        this.howManyFriends = howManyFriends;
    }
}
