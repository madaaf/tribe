package com.tribe.app.domain.entity;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class Friendship implements Serializable {

    public static final String ID_EMPTY = "EMPTY";

    private String id;

    public Friendship(String id) {
        this.id = id;
    }

    private Date createdAt;
    private Date updatedAt;

    private int position;
    protected Tribe tribe;
    protected List<Tribe> receivedTribes = new ArrayList<>();
    protected List<Tribe> sentTribes = new ArrayList<>();
    protected List<Tribe> errorTribes = new ArrayList<>();
    protected String profilePicture;
    protected String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public Tribe getTribe() {
        return tribe;
    }

    public boolean hasLoadedTribes() {
        if (!(receivedTribes != null && receivedTribes.size() > 0)) return false;

        for (Tribe tribe : receivedTribes) {
            if (tribe.getMessageStatus() != null && tribe.getMessageStatus().equals(MessageStatus.STATUS_READY)) return true;
        }

        return false;
    }

    public List<Tribe> getReceivedTribes() {
        return receivedTribes;
    }

    public void setReceivedTribes(List<Tribe> tribes) {
        this.receivedTribes.clear();
        this.receivedTribes.addAll(tribes);
    }

    public List<Tribe> getErrorTribes() {
        return errorTribes;
    }

    public void setErrorTribes(List<Tribe> tribes) {
        this.errorTribes.clear();
        this.errorTribes.addAll(tribes);
    }

    public List<Tribe> getSentTribes() {
        return sentTribes;
    }

    public void setSentTribes(List<Tribe> tribes) {
        this.sentTribes.clear();
        this.sentTribes.addAll(tribes);
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Tribe getMostRecentTribe() {
        return receivedTribes != null && receivedTribes.size() > 0 ? receivedTribes.get(receivedTribes.size() - 1) : null;
    }

    public static int nullSafeComparator(final Friendship one, final Friendship two) {
        if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
            return (one.getUpdatedAt() == null) ? -1 : 1;
        }

        if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
            return 0;
        }

        return one.getUpdatedAt().compareTo(two.getUpdatedAt());
    }

    public static Tribe getMostRecentTribe(List<Friendship> friendships) {
        Tribe tribe = null;

        for (Friendship friendship : friendships) {
            if (tribe == null) tribe = friendship.getMostRecentTribe();
            else if (friendship.getMostRecentTribe() != null) {
                if (friendship.getMostRecentTribe().getRecordedAt().after(tribe.getRecordedAt())) tribe = friendship.getMostRecentTribe();
            }
        }

        return tribe;
    }

    public List<PendingType> createPendingTribeItems(Context context) {
        List<PendingType> pendingList = new ArrayList<>();
        pendingList.add(new PendingType(context.getString(R.string.grid_unsent_tribes_action_resend, errorTribes.size()), PendingType.RESEND));
        pendingList.add(new PendingType(context.getString(R.string.grid_unsent_tribes_action_delete, errorTribes.size()), PendingType.DELETE));
        return pendingList;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
