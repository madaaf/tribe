package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import java.util.List;

public interface HomeGridMVPView extends LoadDataMVPView {

  void onDeepLink(String url);

  void renderRecipientList(List<Recipient> recipientCollection);

  void refreshGrid();

  void successFacebookLogin();

  void errorFacebookLogin();

  void onSyncDone();

  void onSyncStart();

  void renderContactsOnApp(List<Contact> contactList);

  void onCreateRoom(Room room, String feature, String phone, boolean shouldOpenSMS);

  void onSyncError();
}
