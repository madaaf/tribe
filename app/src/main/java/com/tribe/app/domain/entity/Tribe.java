package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.MessageStatus;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a Tribe in the domain layer.
 */
public class Tribe implements Serializable {

    public Tribe() {

    }

    private String id;
    private String localId;
    private User from;
    private String type;
    private Friendship to;
    private boolean toGroup;
    private Date recordedAt;
    private double lat;
    private double lng;
    private String url;
    private @MessageStatus.Status String messageStatus;

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Friendship getTo() {
        return to;
    }

    public void setTo(Friendship to) {
        this.to = to;
    }

    public boolean isToGroup() {
        return toGroup;
    }

    public void setToGroup(boolean toGroup) {
        this.toGroup = toGroup;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public static Tribe createTribe(User user, Friendship friendship, @CameraWrapper.TribeMode String mode) {
        Tribe tribe = new Tribe();
        tribe.setLocalId(FileUtils.generateIdForTribe());
        tribe.setRecordedAt(new Date(System.currentTimeMillis()));
        tribe.setFrom(user);
        tribe.setTo(friendship);
        tribe.setType(mode);
        tribe.setMessageStatus(MessageStatus.STATUS_PENDING);
        return tribe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Tribe that = (Tribe) o;

        return localId != null ? localId.equals(that.localId) : that.localId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (localId != null ? localId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** Tribe Details *****\n");
        stringBuilder.append("\nid = " + id);
        stringBuilder.append("\nlocalId = " + localId);
        stringBuilder.append("\nrecordedAt = " + recordedAt);
        stringBuilder.append("\n*******************************");

        return stringBuilder.toString();
    }

    public static int nullSafeComparator(final Tribe one, final Tribe two) {
        if (one == null ^ two == null) {
            return (one == null) ? 1 : -1;
        }

        if (one == null && two == null) {
            return 0;
        }

        return one.getRecordedAt().compareTo(two.getRecordedAt());
    }
}
