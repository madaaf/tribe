package com.tribe.app.domain.entity;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 05/08/2016.
 */
public abstract class Recipient implements Serializable {

    public static final String ID_EMPTY = "EMPTY";

    protected Date created_at;
    protected Date updated_at;

    protected int position;
    protected TribeMessage tribe;
    protected List<TribeMessage> receivedTribes = new ArrayList<>();
    protected List<TribeMessage> sentTribes = new ArrayList<>();
    protected List<TribeMessage> errorTribes = new ArrayList<>();
    protected List<ChatMessage> receivedMessages = new ArrayList<>();

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setTribe(TribeMessage tribe) {
        this.tribe = tribe;
    }

    public TribeMessage getTribe() {
        return tribe;
    }

    public boolean hasLoadedTribes() {
        if (!(receivedTribes != null && receivedTribes.size() > 0)) return false;

        return receivedTribes.get(0).getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_DOWNLOADED);
    }

    public List<TribeMessage> getReceivedTribes() {
        return receivedTribes;
    }

    public void setReceivedTribes(List<TribeMessage> tribes) {
        this.receivedTribes.clear();
        this.receivedTribes.addAll(tribes);
    }

    public List<TribeMessage> getErrorTribes() {
        return errorTribes;
    }

    public void setErrorTribes(List<TribeMessage> tribes) {
        this.errorTribes.clear();
        this.errorTribes.addAll(tribes);
    }

    public List<TribeMessage> getSentTribes() {
        return sentTribes;
    }

    public void setSentTribes(List<TribeMessage> tribes) {
        this.sentTribes.clear();
        this.sentTribes.addAll(tribes);
    }

    public List<ChatMessage> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<ChatMessage> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public static int nullSafeComparator(final Recipient one, final Recipient two) {
        if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
            return (one.getUpdatedAt() == null) ? 1 : -1;
        }

        if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
            return 0;
        }

        return two.getUpdatedAt().compareTo(one.getUpdatedAt());
    }

    public List<PendingType> createPendingTribeItems(Context context, boolean withName) {
        List<PendingType> pendingList = new ArrayList<>();
        String label = (withName ? getDisplayName() + " " : "") + " (" + errorTribes.size() + ")" ;
        pendingList.add(new PendingType(errorTribes, context.getString(R.string.grid_unsent_tribes_action_resend, label), PendingType.RESEND));
        pendingList.add(new PendingType(errorTribes, context.getString(R.string.grid_unsent_tribes_action_delete, label), PendingType.DELETE));
        return pendingList;
    }

    public abstract String getDisplayName();
    public abstract String getUsername();
    public abstract String getUsernameDisplay();
    public abstract String getProfilePicture();
    public abstract String getSubId();
    public abstract String getId();
    public abstract Date getUpdatedAt();
    public abstract void setScore(int score);

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
