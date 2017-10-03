package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEvent;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageEventAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageEventAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageEvent;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);
    MessageEventViewHolder vh = (MessageEventViewHolder) holder;
    MessageEvent m = (MessageEvent) items.get(position);

    vh.notifContent.setText(m.getContent(m.getUser().getDisplayName()));
    vh.avatarNotif.load(m.getUser().getProfilePicture());
    if (m.getAction().equals(MessageEvent.ACTION_JOIN)) {
      vh.notifContent.setTextColor(ContextCompat.getColor(context, R.color.red_13));
      vh.videoCallBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_chat_video_connected));
    } else {
      vh.notifContent.setTextColor(ContextCompat.getColor(context, R.color.grey_offline));
      vh.videoCallBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_chat_video_grey));
    }
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    MessageEventViewHolder vh = new MessageEventViewHolder(
        layoutInflater.inflate(R.layout.item_message_event, parent, false));

    return vh;
  }

  static class MessageEventViewHolder extends BaseTextViewHolder {
    @BindView(R.id.viewAvatarNotif) public AvatarView avatarNotif;
    @BindView(R.id.notifContent) public TextViewFont notifContent;
    @BindView(R.id.containerNotif) public LinearLayout container;
    @BindView(R.id.videoCallBtn) public ImageView videoCallBtn;

    @Override protected ViewGroup getLayoutContent() {
      return null;
    }

    MessageEventViewHolder(View itemView) {
      super(itemView);
    }
  }
}
