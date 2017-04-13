package com.tribe.app.data.network.entity;

import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class LookupEntity {

  private List<LookupObject> lookupList;

  public List<LookupObject> getLookupList() {
    return lookupList;
  }

  public void setLookupList(List<LookupObject> lookupList) {
    this.lookupList = lookupList;
  }
}
