package com.tribe.app.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMember implements Parcelable {

    private String userId;
    private String displayName;
    private String username;
    private String profilePicture;
    private boolean isFriend;
    private boolean isAdmin;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    private GroupMember(String userId) {
        this.userId = userId;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    protected GroupMember(Parcel in) {
        userId = in.readString();
        displayName = in.readString();
        username = in.readString();
        isFriend = in.readByte() != 0x00;
        isAdmin = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(displayName);
        dest.writeString(username);
        dest.writeString(profilePicture);
        dest.writeByte((byte) (isFriend ? 0x01 : 0x00));
        dest.writeByte((byte) (isAdmin ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GroupMember> CREATOR = new Parcelable.Creator<GroupMember>() {
        @Override
        public GroupMember createFromParcel(Parcel in) {
            return new GroupMember(in);
        }

        @Override
        public GroupMember[] newArray(int size) {
            return new GroupMember[size];
        }
    };
}