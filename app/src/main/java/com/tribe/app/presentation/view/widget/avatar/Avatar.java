package com.tribe.app.presentation.view.widget.avatar;

import com.tribe.app.domain.entity.Recipient;
import java.util.List;

/**
 * Created by tiago on 06/01/2017.
 */

public interface Avatar {

  void load(Recipient recipient);

  void load(String url);

  void load(int drawableId);

  void loadGroupAvatar(String url, String previousUrl, String groupId, List<String> membersPic);
}
