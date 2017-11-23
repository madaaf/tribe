package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
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

  void renderContactsInvite(List<Contact> contactList);

  void renderContactsFBInvite(List<Contact> contactList);

  void onCreateRoom(Room room, String feature, String phone, boolean shouldOpenSMS);

  void onSyncError();

  void onBannedUser(User user);

  void onShortcutCreatedFromSuggestedFriendSuccess(Shortcut shortcut,  UserToAddAdapterDelegate.UserToAddViewHolder vh);
}
