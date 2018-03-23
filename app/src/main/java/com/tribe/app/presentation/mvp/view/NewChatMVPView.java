package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.ContactFB;
import java.util.List;

public interface NewChatMVPView extends MVPView {

  void onLoadFBContactsFbInvite(List<ContactFB> contactList);

  void onLoadFBContactsInviteFailed();
}
