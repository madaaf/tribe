package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.UserRealm;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 19/05/2016.
 */
public class LookupFBResult implements Serializable {

  private List<UserRealm> lookupList;

  public List<UserRealm> getLookup() {
    return lookupList;
  }

  public void setLookup(List<UserRealm> lookupList) {
    this.lookupList = lookupList;
  }
}
