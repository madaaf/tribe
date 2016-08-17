package com.tribe.app.data.cache;

import com.tribe.app.data.realm.ChatRealm;

import java.util.List;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface ChatCache {

    public boolean isExpired();
    public boolean isCached(int messageId);
    public Observable<ChatRealm> put(ChatRealm chatRealm);
    public void update(ChatRealm chatRealm);
    public void put(List<ChatRealm> messageListRealm);
    public Observable<List<ChatRealm>> messages();
    public Observable<List<ChatRealm>> messages(String friendshipId);
    public Observable<Void> delete(ChatRealm chatRealm);
    public ChatRealm updateLocalWithServerRealm(ChatRealm local, ChatRealm server);
}
