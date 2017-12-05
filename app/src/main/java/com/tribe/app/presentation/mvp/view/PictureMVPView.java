package com.tribe.app.presentation.mvp.view;

import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;

/**
 * Created by madaaflak on 05/10/2017.
 */

public interface PictureMVPView extends MVPView {

  void successGetMessageImageFromDisk(List<Message> messages);

  void errorGetMessageImageFromDisk();
}
