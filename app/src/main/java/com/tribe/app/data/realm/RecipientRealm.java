package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by tiago on 04/08/2016.
 */
public class RecipientRealm extends RealmObject {

    private UserTribeRealm to;
    private boolean isSeen;

    public UserTribeRealm getTo() {
        return to;
    }

    public void setTo(UserTribeRealm to) {
        this.to = to;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
