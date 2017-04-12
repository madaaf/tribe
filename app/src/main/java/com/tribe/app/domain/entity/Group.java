package com.tribe.app.domain.entity;

import android.text.TextUtils;
import com.tribe.app.presentation.utils.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 30/05/2016.
 */
public class Group implements Serializable {

  public Group(String id) {
    this.id = id;
  }

  private String id;
  private String picture;
  private String name;
  private List<User> members;
  private boolean is_live;
  private Date last_seen_at;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<User> getMembers() {
    return members;
  }

  public void setMembers(List<User> members) {
    this.members = members;
  }

  public void setIsLive(boolean isLive) {
    this.is_live = isLive;
  }

  public boolean isLive() {
    return is_live;
  }

  public Date getLastSeenAt() {
    return last_seen_at;
  }

  public void setLastSeenAt(Date lastSeenAt) {
    this.last_seen_at = lastSeenAt;
  }

  public List<String> getMembersPics() {
    List<String> pics = new ArrayList<>();

    if (members != null) {
      List<User> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

      if (subMembers != null) {
        for (User user : subMembers) {
          pics.add(user.getProfilePicture());
        }
      }
    }

    return pics;
  }

  public String getMembersNames() {
    List<String> names = new ArrayList<>();

    if (members != null) {
      List<User> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

      if (subMembers != null) {
        for (User user : subMembers) {
          String url = user.getDisplayName();
          if (!StringUtils.isEmpty(url)) names.add(url);
        }
      }
    }

    return TextUtils.join(", ", names);
  }

  public void computeGroupMembers(List<GroupMember> groupMemberList) {
    if (groupMemberList != null) {
      for (GroupMember groupMember : groupMemberList) {
        if (members != null) {
          for (User member : members) {
            if (groupMember.getUser().getId().equals(member.getId())) {
              groupMember.setMember(true);
              groupMember.setOgMember(true);
              break;
            }
          }
        }
      }
    }
  }

  public List<GroupMember> getGroupMembers() {
    List<GroupMember> groupMemberList = new ArrayList<>();

    for (User member : members) {
      GroupMember groupMember = new GroupMember(member);
      groupMember.setMember(true);
      groupMember.setOgMember(true);
      groupMemberList.add(groupMember);
    }

    //Collections.sort(groupMemberList, (o1, o2) -> GroupMember.nullSafeComparator(o1, o2));

    return groupMemberList;
  }

  public boolean isGroupMember(String userId) {
    if (members != null) {
      for (User member : members) {
        if (member.getId().equals(userId)) {
          return true;
        }
      }
    }

    return false;
  }
}
