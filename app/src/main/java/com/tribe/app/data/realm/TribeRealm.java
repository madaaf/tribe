package com.tribe.app.data.realm;

import com.tribe.app.presentation.view.utils.MessageStatus;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 29/06/2016.
 */
public class TribeRealm extends RealmObject {


    @PrimaryKey
    private String localId;
    private String id;
    private UserTribeRealm from;
    private String type;
    private String url;
    private UserTribeRealm user;
    private GroupRealm group;
    private Date recorded_at;
    private boolean to_group = false;
    private @MessageStatus.Status String messageStatus;
    private Date updatedAt;
    private WeatherRealm weatherRealm;
    private LocationRealm locationRealm;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public UserTribeRealm getFrom() {
        return from;
    }

    public void setFrom(UserTribeRealm from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isToGroup() {
        return to_group;
    }

    public void setToGroup(boolean to_group) {
        this.to_group = to_group;
    }

    public Date getRecordedAt() {
        return recorded_at;
    }

    public void setRecordedAt(Date recorded_at) {
        this.recorded_at = recorded_at;
    }

    public GroupRealm getGroup() {
        return group;
    }

    public void setGroup(GroupRealm group) {
        this.group = group;
    }

    public UserTribeRealm getUser() {
        return user;
    }

    public void setUser(UserTribeRealm user) {
        this.user = user;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public WeatherRealm getWeatherRealm() {
        return weatherRealm;
    }

    public void setWeatherRealm(WeatherRealm weatherRealm) {
        this.weatherRealm = weatherRealm;
    }

    public LocationRealm getLocationRealm() {
        return locationRealm;
    }

    public void setLocationRealm(LocationRealm locationRealm) {
        this.locationRealm = locationRealm;
    }

    public TribeRealm cloneTribeRealm(TribeRealm fromRealm) {
        TribeRealm tribeRealm = new TribeRealm();
        tribeRealm.setId(fromRealm.getId());
        tribeRealm.setLocalId(fromRealm.getLocalId());
        tribeRealm.setGroup(fromRealm.getGroup());
        tribeRealm.setUser(fromRealm.getUser());
        tribeRealm.setType(fromRealm.getType());
        tribeRealm.setRecordedAt(fromRealm.getRecordedAt());
        tribeRealm.setUpdatedAt(fromRealm.getUpdatedAt());
        tribeRealm.setFrom(fromRealm.getFrom());
        tribeRealm.setLocationRealm(fromRealm.getLocationRealm());
        tribeRealm.setWeatherRealm(fromRealm.getWeatherRealm());
        tribeRealm.setUrl(fromRealm.getUrl());
        tribeRealm.setMessageStatus(fromRealm.getMessageStatus());

        return tribeRealm;
    }
}
