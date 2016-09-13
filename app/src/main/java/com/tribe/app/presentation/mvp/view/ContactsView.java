package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;

import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public interface ContactsView extends LoadDataView {

    void renderContactList(List<Contact> contactList);
    void renderSearchResult(SearchResult searchResult);
    void renderSearchContacts(List<Contact> contactList);
    void onAddSuccess(Friendship friendship);
    void notifySuccess();
    void onAddError();
    void successFacebookLogin();
    void errorFacebookLogin();
}
