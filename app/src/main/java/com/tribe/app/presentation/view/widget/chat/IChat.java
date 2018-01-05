package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Subscription;

/**
 * Created by madaaflak on 30/11/2017.
 */

public abstract class IChat extends ChatMVPView {

  static Shortcut fromShortcut = null;
  static ChatUserAdapter chatUserAdapter;
  static List<User> members = new ArrayList<>();
  static Map<String, Subscription> subscriptionList = new HashMap<>();

  public IChat(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
}
