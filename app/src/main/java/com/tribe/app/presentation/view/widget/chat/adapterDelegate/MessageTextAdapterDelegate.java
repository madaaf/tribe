package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.ChatView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageTextAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageTextAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageText;
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    return new MessageTextViewHolder(
        layoutInflater.inflate(R.layout.item_message_text, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessageText m = (MessageText) items.get(position);
    MessageTextViewHolder vh = (MessageTextViewHolder) holder;

    vh.message.setText(position + " " + m.getMessage());

    if (type == ChatView.FROM_LIVE) {
      vh.message.setTextColor(ContextCompat.getColor(context, R.color.white));
    }
    setPendingBehavior(m, vh.container);
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessageTextViewHolder vh = (MessageTextViewHolder) holder;
    MessageText m = (MessageText) items.get(position);
    Timber.w("PUT 1f " + m.toString() + ((MessageText) payloads.get(0)).toString());
    if (m.isPending()) {
      vh.container.setAlpha(0.4f);
      vh.message.setText(vh.message.getText().toString() + " 0.4f on BOND");
    } else {
      vh.container.setAlpha(1f);
      vh.message.setText(vh.message.getText().toString() + " 1f on BIND");
    }
  }

  static class MessageTextViewHolder extends BaseTextViewHolder {

    @BindView(R.id.message) public TextViewFont message;
    @BindView(R.id.container) public LinearLayout container;

    MessageTextViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
