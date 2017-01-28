package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.UserRealm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 27/01/2017.
 */

public class SubscriptionResponse {

    private List<UserRealm> userUpdatedList;
    private List<GroupRealm> groupUpdatedList;
    private Map<String, Boolean> onlineMap;
    private Map<String, Boolean> liveMap;

    public SubscriptionResponse() {
        userUpdatedList = new ArrayList<>();
        groupUpdatedList = new ArrayList<>();
        onlineMap = new HashMap<>();
        liveMap = new HashMap<>();
    }

    public List<GroupRealm> getGroupUpdatedList() {
        return groupUpdatedList;
    }

    public void setGroupUpdatedList(List<GroupRealm> groupUpdatedList) {
        this.groupUpdatedList.clear();
        this.groupUpdatedList.addAll(groupUpdatedList);
    }

    public List<UserRealm> getUserUpdatedList() {
        return userUpdatedList;
    }

    public void setUserUpdatedList(List<UserRealm> userUpdatedList) {
        this.userUpdatedList.clear();
        this.userUpdatedList.addAll(userUpdatedList);
    }

    public void setLiveMap(Map<String, Boolean> liveMap) {
        this.liveMap.clear();
        this.liveMap.putAll(liveMap);
    }

    public Map<String, Boolean> getLiveMap() {
        return liveMap;
    }

    public void setOnlineMap(Map<String, Boolean> onlineMap) {
        this.onlineMap.clear();
        this.onlineMap.putAll(onlineMap);
    }

    public Map<String, Boolean> getOnlineMap() {
        return onlineMap;
    }
}
