package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;

import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public interface ContactsView extends LoadDataView {

    void renderContactList(List<Contact> contactList);
    void successFacebookLogin();
    void errorFacebookLogin();
}
