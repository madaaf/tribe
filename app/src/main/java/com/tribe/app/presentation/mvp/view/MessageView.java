package com.tribe.app.presentation.mvp.view;

import android.graphics.Bitmap;

import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;

import java.util.List;

public interface MessageView extends LoadDataView {

    void renderMessageList(List<ChatMessage> chatMessageList);
    void renderMessage(ChatMessage chatMessage);
    void showGalleryImage(Bitmap bitmap);
    boolean isLastItemDisplayed();
    void onRecipientLoaded(Recipient recipient);
}
