package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.MarvelCharacter;

import java.util.Collection;
import java.util.List;

public interface HomeGridView extends LoadDataView {

    void renderFriendList(List<MarvelCharacter> friendCollection);
    void onTextClicked(MarvelCharacter friend);
}
