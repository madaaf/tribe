package com.tribe.app.data.cache;

import com.tribe.app.data.realm.TextRealm;
import com.tribe.app.data.realm.UserRealm;

import javax.inject.Singleton;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface TextCache {

    public boolean isExpired();
    public boolean isCached(int userId);
    public void put(TextRealm textRealm);
}
