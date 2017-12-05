package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.List;

/**
 * Created by tiago on 11/04/2016.
 */
public interface ShortcutMVPView extends MVPView {

  void onShortcutCreatedSuccess(Shortcut shortcut);

  void onShortcutCreatedError();

  void onShortcutRemovedSuccess();

  void onShortcutRemovedError();

  void onShortcutUpdatedSuccess(Shortcut shortcutn, BaseListViewHolder viewHolder);

  void onShortcutUpdatedError();

  void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList);

  void onShortcut(Shortcut shortcut);
}
