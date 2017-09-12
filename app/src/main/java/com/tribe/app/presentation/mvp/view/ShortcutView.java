package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Shortcut;

/**
 * Created by tiago on 11/04/2016.
 */
public interface ShortcutView extends MVPView {

  void onShortcutCreate(Shortcut shortcut);
  void onShortcutDeleted();
  void onShortcutUpdated(Shortcut shortcut);
}
