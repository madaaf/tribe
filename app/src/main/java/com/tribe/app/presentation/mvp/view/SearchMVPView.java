package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.Shortcut;
import java.util.List;

/**
 * Created by tiago on 12/02/2016.
 */
public interface SearchMVPView extends UpdateUserMVPView {

  void renderSearchResult(SearchResult searchResult);

  void renderContactList(List<Shortcut> contactList);

  void renderContactListOnApp(List<Contact> contactListOnApp);

  void renderContactListInvite(List<Contact> contactListInvite);

  void renderContactListInviteFB(List<Contact> contactListInviteFB);

  void syncDone();
}
