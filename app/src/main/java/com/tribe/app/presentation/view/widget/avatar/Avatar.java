package com.tribe.app.presentation.view.widget.avatar;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import java.util.List;

/**
 * Created by tiago on 06/01/2017.
 */

public interface Avatar {

  void load(Recipient recipient);

  void load(User user);

  void load(String url);

  void load(int drawableId);

  void loadColorPlaceholder(int color);

  void loadGroupAvatar(String url, String previousUrl, String groupId, List<String> membersPic);
}
