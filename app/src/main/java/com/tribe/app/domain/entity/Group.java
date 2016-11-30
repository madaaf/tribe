package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
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
    private String groupLink;
    private List<User> members;
    private List<User> admins;

    private List<GroupMemberId> memberIdList;
    private List<GroupMemberId> adminIdList;

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

    public String getGroupLink() {
        return groupLink;
    }

    public void setGroupLink(String groupLink) {
        this.groupLink = groupLink;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<User> getAdmins() {
        return admins;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }

    public List<GroupMemberId> getAdminIdList() {
        return adminIdList;
    }

    public List<GroupMemberId> getMemberIdList() {
        return memberIdList;
    }

    public void setAdminIdList(List<GroupMemberId> adminIdList) {
        this.adminIdList = adminIdList;
    }

    public void setMemberIdList(List<GroupMemberId> memberIdList) {
        this.memberIdList = memberIdList;
    }

    public List<String> getMembersPics() {
        List<String> pics = new ArrayList<>();

        if (members != null) {
            List<User> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

            if (subMembers != null) {
                for (User user : subMembers) {
                    String url = user.getProfilePicture();
                    if (!StringUtils.isEmpty(url))
                        pics.add(url);
                }
            }
        }

        return pics;
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

                if (admins != null) {
                    for (User admin : admins) {
                        if (groupMember.getUser().getId().equals(admin.getId())) {
                            groupMember.setMember(true);
                            groupMember.setOgMember(true);
                            groupMember.setAdmin(true);
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

            for (User admin : admins) {
                if (member.equals(admin)) groupMember.setAdmin(true);
            }

            groupMemberList.add(groupMember);
        }

        return groupMemberList;
    }

    public boolean isUserAdmin(User user) {
        for (User admin : admins) {
            if (admin.equals(user)) {
                return true;
            }
        }

        return false;
    }
}
