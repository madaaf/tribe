package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Shortcut;

/**
 * Created by madaaflak on 06/09/2017.
 */

public abstract class TypingMVPView implements MVPView {

  abstract void isTypingEvent(String userId);

  abstract void successShortcutUpdate(Shortcut shortcut);

  abstract void errorShortcutUpdate();


}
