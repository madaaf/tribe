package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.UserRealm;

import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class LookupEntity {

    private List<UserRealm> lookupList;

    public List<UserRealm> getLookup() {
        return lookupList;
    }

    public void setLookup(List<UserRealm> lookupList) {
        this.lookupList = lookupList;
    }
}
