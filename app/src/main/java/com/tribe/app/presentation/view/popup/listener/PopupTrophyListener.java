package com.tribe.app.presentation.view.popup.listener;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TrophyEnum;

/**
 * Created by tiago on 26/02/2018.
 */

public interface PopupTrophyListener extends PopupListener {

  void onClick(TrophyEnum trophyEnum);
}
