package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.domain.entity.SearchResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class SearchResultRealmDataMapper {

    FriendshipRealmDataMapper friendshipRealmDataMapper;

    @Inject
    public SearchResultRealmDataMapper(FriendshipRealmDataMapper friendshipRealmDataMapper) {
        this.friendshipRealmDataMapper = friendshipRealmDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.SearchResultRealm} into an {@link com.tribe.app.domain.entity.SearchResult}.
     *
     * @param searchResultRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.SearchResult} if valid {@link com.tribe.app.data.realm.SearchResultRealm} otherwise null.
     */
    public SearchResult transform(SearchResultRealm searchResultRealm) {
        SearchResult searchResult = null;

        if (searchResultRealm != null) {
            searchResult = new SearchResult();
            searchResult.setUsername(searchResultRealm.getUsername());
            searchResult.setDisplayName(searchResultRealm.getDisplayName());
            searchResult.setPicture(searchResultRealm.getPicture());
            searchResult.setFriendship(friendshipRealmDataMapper.transform(searchResultRealm.getFriendshipRealm()));
        }

        return searchResult;
    }
}
