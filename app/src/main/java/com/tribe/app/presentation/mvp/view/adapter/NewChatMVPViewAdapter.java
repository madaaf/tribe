package com.tribe.app.presentation.mvp.view.adapter;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 01/31/2018.
 */

public class NewChatMVPViewAdapter implements NewChatMVPView, ShortcutMVPView {

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override
  public void onShortcutUpdatedSuccess(Shortcut shortcutn, BaseListViewHolder viewHolder) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  @Override public void onLoadFBContactsInvite(List<Contact> contactList) {

  }
}
