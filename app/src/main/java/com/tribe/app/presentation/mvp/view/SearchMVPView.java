package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import java.util.List;

/**
 * Created by tiago on 12/02/2016.
 */
public interface SearchMVPView extends UpdateUserMVPView {

  void renderSearchResult(SearchResult searchResult);

  void renderContactList(List<Object> contactList);

  void onAddSuccess(Friendship friendship);

  void onAddError();

  void syncDone();
}
