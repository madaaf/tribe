package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Contact;
import java.util.ArrayList;
import java.util.List;

public interface NewChatMVPView extends MVPView {

  void onLoadFBContactsInvite(List<Contact> contactList);
  void onLoadFBContactsInviteFailed();
}
