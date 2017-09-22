package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageTextAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageTextAdapterDelegate(Context context) {
    super(context);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageText;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessageText m = (MessageText) items.get(position);
    MessageTextViewHolder vh = (MessageTextViewHolder) holder;

    vh.message.setText(m.getMessage());
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    MessageTextViewHolder vh = new MessageTextViewHolder(
        layoutInflater.inflate(R.layout.item_message_text, parent, false));

    return vh;
  }

  static class MessageTextViewHolder extends BaseTextViewHolder {

    @BindView(R.id.message) public TextViewFont message;

    MessageTextViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return null;
    }
  }
}
