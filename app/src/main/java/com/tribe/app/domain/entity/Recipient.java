package com.tribe.app.domain.entity;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.MessageStatus;

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
    protected Tribe tribe;
    protected List<Tribe> receivedTribes = new ArrayList<>();
    protected List<Tribe> sentTribes = new ArrayList<>();
    protected List<Tribe> errorTribes = new ArrayList<>();

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public Date getUpdatedAt() {
        return updated_at;
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

    public Tribe getMostRecentTribe() {
        return receivedTribes != null && receivedTribes.size() > 0 ? receivedTribes.get(receivedTribes.size() - 1) : null;
    }

    public static int nullSafeComparator(final Recipient one, final Recipient two) {
        if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
            return (one.getUpdatedAt() == null) ? -1 : 1;
        }

        if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
            return 0;
        }

        return one.getUpdatedAt().compareTo(two.getUpdatedAt());
    }

    public static Tribe getMostRecentTribe(List<Recipient> recipientList) {
        Tribe tribe = null;

        for (Recipient recipient : recipientList) {
            if (tribe == null) tribe = recipient.getMostRecentTribe();
            else if (recipient.getMostRecentTribe() != null) {
                if (recipient.getMostRecentTribe().getRecordedAt().after(tribe.getRecordedAt())) tribe = recipient.getMostRecentTribe();
            }
        }

        return tribe;
    }

    public List<PendingType> createPendingTribeItems(Context context, boolean withName) {
        List<PendingType> pendingList = new ArrayList<>();
        pendingList.add(new PendingType(errorTribes,
                withName ? context.getString(R.string.grid_unsent_tribes_action_resend_name, getDisplayName(), errorTribes.size()) : context.getString(R.string.grid_unsent_tribes_action_resend, errorTribes.size()),
                PendingType.RESEND));
        pendingList.add(new PendingType(errorTribes,
                withName ? context.getString(R.string.grid_unsent_tribes_action_delete_name, getDisplayName(), errorTribes.size()) : context.getString(R.string.grid_unsent_tribes_action_delete, errorTribes.size())
                , PendingType.DELETE));
        return pendingList;
    }

    public abstract String getDisplayName();
    public abstract String getProfilePicture();
    public abstract String getId();

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
