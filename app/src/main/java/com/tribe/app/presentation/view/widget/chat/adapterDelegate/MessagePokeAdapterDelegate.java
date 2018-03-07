package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tokenautocomplete.CustomTypefaceSpan;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.ChatView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessagePokeAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessagePokeAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessagePoke;
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    return new MessagePokeViewHolder(
        layoutInflater.inflate(R.layout.item_message_poke, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessagePoke m = (MessagePoke) items.get(position);
    MessagePokeViewHolder vh = (MessagePokeViewHolder) holder;

    vh.message.setText(m.getData());

    if (type == ChatView.FROM_LIVE) {
      vh.message.setTextColor(ContextCompat.getColor(context, R.color.white));
    }
    setPendingBehavior(m, vh.container);

   // linkifyMessage(m, vh);
  }

  private void linkifyMessage(MessagePoke m, MessagePokeViewHolder vh) {
   /* if (m.getMessage().contains("www.") || m.getMessage().contains("http://")) {
      Linkify.addLinks(vh.message, Linkify.ALL);
      vh.message.setLinkTextColor(Color.BLUE);

      int index1 = (m.getMessage().contains("http://")) ? m.getMessage().indexOf("http://")
          : m.getMessage().indexOf("www.");
      String formattedString = m.getMessage().substring(index1, m.getMessage().length());
      int index2 = (formattedString.indexOf(" ") == -1) ? formattedString.length()
          : formattedString.indexOf(" ");
      String link = formattedString.substring(0, index2);

      Typeface font = Typeface.createFromAsset(context.getAssets(), "ProximaNovaSoft-Bold.ttf");
      TypefaceSpan robotoRegularSpan = new CustomTypefaceSpan("", font);

      SpannableStringBuilder finalSpan = new SpannableStringBuilder(m.getMessage());
      finalSpan.setSpan(robotoRegularSpan, index1, index1 + link.length(),
          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      vh.message.setText(finalSpan);
    }*/
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessagePokeViewHolder vh = (MessagePokeViewHolder) holder;
    MessagePoke m = (MessagePoke) items.get(position);

    //  linkifyMessage(m, vh);
    if (m.isPending()) {
      vh.container.setAlpha(0.4f);
    } else {
      vh.container.setAlpha(1f);
    }
  }

  static class MessagePokeViewHolder extends BaseTextViewHolder {

    @BindView(R.id.message) public TextViewFont message;
    @BindView(R.id.container) public LinearLayout container;

    MessagePokeViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
