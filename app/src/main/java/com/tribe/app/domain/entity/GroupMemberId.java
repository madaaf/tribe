package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 23/11/2016.
 */
public class GroupMemberId implements Serializable {

  private String id;
  private String groupId;

  public GroupMemberId(String id, String groupId) {
    this.id = id;
    this.groupId = groupId;
  }

  public GroupMemberId() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupId() {
    return groupId;
  }
}
