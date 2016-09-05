package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a TribeMessage in the domain layer.
 */
public class TribeMessage extends Message {

    public TribeMessage() {

    }

    private @CameraWrapper.TribeMode String type;
    private Location location;
    private Weather weather;
    private String transcript;

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

    public Recipient getTo() {
        return to;
    }

    public void setTo(Recipient to) {
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getTranscript() {
        return transcript;
    }

    public static TribeMessage createTribe(User user, Recipient recipient, @CameraWrapper.TribeMode String mode) {
        TribeMessage tribe = new TribeMessage();
        tribe.setLocalId(FileUtils.generateIdForMessage());
        tribe.setRecordedAt(new Date(System.currentTimeMillis()));
        tribe.setFrom(user);
        tribe.setTo(recipient);
        tribe.setToGroup(recipient instanceof Group);
        tribe.setType(mode);
        tribe.setMessageSendingStatus(MessageSendingStatus.STATUS_PENDING);
        tribe.setLocation(user.getLocation());
        return tribe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        TribeMessage that = (TribeMessage) o;

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

        stringBuilder.append("***** TribeMessage Details *****\n");
        stringBuilder.append("\nid = " + id);
        stringBuilder.append("\nfrom.id = " + from.getId());
        stringBuilder.append("\nupdatedAt = " + updatedAt);
        stringBuilder.append("\n*******************************");

        return stringBuilder.toString();
    }

    public static int nullSafeComparator(final TribeMessage one, final TribeMessage two) {
        if (one == null ^ two == null) {
            return (one == null) ? 1 : -1;
        }

        if (one == null && two == null) {
            return 0;
        }

        return one.getRecordedAt().compareTo(two.getRecordedAt());
    }

    public static TribeMessage getMostRecentTribe(final TribeMessage... tribes) {
        List<TribeMessage> tribeList = Arrays.asList(tribes);

        Collections.sort(tribeList, (one, two) -> {
            if (one == null ^ two == null) {
                return (one == null) ? -1 : 1;
            }

            if (one == null && two == null) return 0;

            if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
                return (one.getUpdatedAt() == null) ? -1 : 1;
            }

            if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
                return one.getRecordedAt().before(two.getRecordedAt()) ? -1 : 1;
            }

            return one.getUpdatedAt().before(two.getUpdatedAt()) ? -1 : 1;
        });

        return tribeList.get(tribeList.size() - 1);
    }
}
