package com.tribe.app.presentation.view.popup.listener;

import com.tribe.app.domain.entity.Recipient;

/**
 * Created by tiago on 26/02/2018.
 */

public interface PopupDigestListener extends PopupListener {

  void onClick(Recipient recipient);
  void onClickMore();
}
