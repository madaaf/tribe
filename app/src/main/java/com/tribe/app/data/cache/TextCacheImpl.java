package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.TextRealm;
import com.tribe.app.data.realm.UserRealm;

import javax.inject.Inject;

/**
 * Created by tiago on 06/05/2016.
 */
public class TextCacheImpl implements TextCache {

    private Context context;

    @Inject
    public TextCacheImpl(Context context) {
        this.context = context;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int userId) {
        return false;
    }

    public void put(TextRealm textRealm) {

    }
}
