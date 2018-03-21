package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.ContactInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tiago on 17/04/2017.
 */

public class LookupHolder {

  private List<ContactInterface> contactPhoneList;
  private List<ContactInterface> contactFBList;
  private List<LookupObject> lookupObjectList;

  public void setContactFBList(List<ContactInterface> contactFBList) {
    this.contactFBList = contactFBList;
  }

  public void setContactPhoneList(List<ContactInterface> contactPhoneList) {
    this.contactPhoneList = contactPhoneList;
  }

  public List<ContactInterface> getContactFBList() {
    return contactFBList;
  }

  public List<ContactInterface> getContactPhoneList() {
    return contactPhoneList;
  }

  public void setLookupObjectList(List<LookupObject> lookupObjectList) {
    this.lookupObjectList = lookupObjectList;
  }

  public List<LookupObject> getLookupObjectList() {
    return lookupObjectList;
  }

  public List<ContactInterface> getContactAllList() {
    List<ContactInterface> contactList = new ArrayList<>();
    contactList.addAll(contactPhoneList);
    contactList.addAll(contactFBList);
    return contactList;
  }
}
