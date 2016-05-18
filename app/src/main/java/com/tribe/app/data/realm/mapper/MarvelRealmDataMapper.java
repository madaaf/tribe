package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.MarvelCharacter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MarvelRealmDataMapper {

    @Inject
    public MarvelRealmDataMapper() {
    }

    public MarvelCharacter transform(MarvelCharacterRealm marvelCharacterRealm) {
        MarvelCharacter marvelCharacter = null;

        if (marvelCharacterRealm != null) {
            marvelCharacter = new MarvelCharacter(marvelCharacterRealm.getName(), marvelCharacterRealm.getId());
            marvelCharacter.setDescription(marvelCharacterRealm.getDescription());
        }

        return marvelCharacter;
    }

    public List<MarvelCharacter> transform(Collection<MarvelCharacterRealm> marvelCharacterRealmCollection) {
        List<MarvelCharacter> marvelCharacterList = new ArrayList<>();
        MarvelCharacter marvelCharacter;

        for (MarvelCharacterRealm marvelCharacterRealm : marvelCharacterRealmCollection) {
            marvelCharacter = transform(marvelCharacterRealm);
            if (marvelCharacter != null) {
                marvelCharacterList.add(marvelCharacter);
            }
        }

        return marvelCharacterList;
    }
}
