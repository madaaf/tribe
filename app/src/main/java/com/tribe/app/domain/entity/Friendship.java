package com.tribe.app.domain.entity;

import java.text.Normalizer;
import java.util.Date;

/**
 * Created by tiago on 05/08/2016.
 */
public class Friendship extends Recipient implements Comparable<Friendship> {

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
    public String getId() {
        return id;
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
    public String getSubId() {
        if (id.equals(Recipient.ID_EMPTY)) return id;

        return friend.getId();
    }

    @Override
    public String getUsername() {
        return friend.getUsername();
    }

    @Override
    public String getUsernameDisplay() {
        return friend.getUsernameDisplay();
    }

    @Override
    public Date getUpdatedAt() {
        return friend.getUpdatedAt();
    }

    @Override
    public void setScore(int score) {
        friend.setScore(score);
    }

    @Override
    public int compareTo(Friendship other) {
        if (other == null) return 1;

        String name1 = Normalizer.normalize(this.getDisplayName(), Normalizer.Form.NFD);
        name1 = name1.replaceAll("[^\\p{ASCII}]", "");
        String name2 = Normalizer.normalize(other.getDisplayName(), Normalizer.Form.NFD);
        name2 = name2.replaceAll("[^\\p{ASCII}]", "");

        return name1.compareTo(name2);
    }
}
