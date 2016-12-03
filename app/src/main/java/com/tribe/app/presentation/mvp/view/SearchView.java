package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;

/**
 * Created by tiago on 12/02/2016.
 */
public interface SearchView extends LoadDataView {

    void renderSearchResult(SearchResult searchResult);
    void onAddSuccess(Friendship friendship);
    void onAddError();
}
