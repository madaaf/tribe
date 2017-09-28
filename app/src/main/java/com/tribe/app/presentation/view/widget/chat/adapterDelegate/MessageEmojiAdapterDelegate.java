package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import com.tribe.app.R;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageEmojiAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageEmojiAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageEmoji;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessageEmojiViewHolder vh = (MessageEmojiViewHolder) holder;
    MessageEmoji m = (MessageEmoji) items.get(position);

    vh.emoji.setText(m.getEmoji());

    setPendingBehavior(m, vh.container);
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessageEmojiViewHolder vh = (MessageEmojiViewHolder) holder;
    vh.container.setAlpha(1f);
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    MessageEmojiViewHolder vh = new MessageEmojiViewHolder(
        layoutInflater.inflate(R.layout.item_message_emoji, parent, false));

    return vh;
  }

  static class MessageEmojiViewHolder extends BaseTextViewHolder {
    @BindView(R.id.emoji) public TextViewFont emoji;
    @BindView(R.id.container) public LinearLayout container;

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }

    public MessageEmojiViewHolder(View itemView) {
      super(itemView);
    }
  }
}
