package com.tribe.app.domain.entity;

/**
 * Created by tiago on 05/08/2016.
 */
public class Friendship extends Recipient {

    private String id;
    private String tag;
    private boolean blocked;
    private String category;
    private User friend;

    public Friendship(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    @Override
    public String getDisplayName() {
        return friend.getDisplayName();
    }

    @Override
    public String getProfilePicture() {
        return friend.getProfilePicture();
    }

    @Override
    public String getId() {
        if (id.equals(Recipient.ID_EMPTY)) return id;

        return friend.getId();
    }

    @Override
    public String getUsername() {
        return friend.getUsername();
    }
}
