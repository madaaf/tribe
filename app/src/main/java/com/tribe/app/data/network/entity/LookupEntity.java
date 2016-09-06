package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.UserRealm;

import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class LookupEntity {

    private List<UserRealm> lookupByPhone;
    private List<UserRealm> lookupByFbid;

    public List<UserRealm> getLookupByFbid() {
        return lookupByFbid;
    }

    public List<UserRealm> getLookupByPhone() {
        return lookupByPhone;
    }

    public void setLookupByFbid(List<UserRealm> lookupByFbid) {
        this.lookupByFbid = lookupByFbid;
    }

    public void setLookupByPhone(List<UserRealm> lookupByPhone) {
        this.lookupByPhone = lookupByPhone;
    }
}
