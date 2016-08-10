package com.tribe.app.presentation.mvp.view;

import android.graphics.Bitmap;

import com.tribe.app.domain.entity.Message;

import java.util.List;

public interface MessageView extends LoadDataView {

    void renderMessageList(List<Message> messageList);
    void renderMessage(Message message);
    void showGalleryImage(Bitmap bitmap);
    boolean isLastItemDisplayed();
}
