package com.tribe.app.presentation.view.popup.listener;

/**
 * Created by tiago on 03/08/2018.
 */

public interface PopupAskToJoinListener extends PopupListener {

  void accept();

  void decline();

  void later();
}
