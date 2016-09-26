package com.tribe.app.data.realm;

import java.util.Date;

/**
 * Created by tiago on 12/08/2016.
 */
public interface MessageRealmInterface {

    UserRealm getFrom();
    RecipientRealmInterface getRecipient();
    void setFrom(UserRealm userRealm);
    boolean isToGroup();
    Date getUpdatedAt();
    Date getRecordedAt();
}
