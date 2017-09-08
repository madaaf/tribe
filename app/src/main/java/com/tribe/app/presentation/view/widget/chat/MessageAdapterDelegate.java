package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  protected LayoutInflater layoutInflater;

  private Context context;
  private int imageSize;

  public MessageAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;

    // DEFAULT SIZE
    imageSize = context.getResources().getDimensionPixelSize(R.dimen.image_size);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new MessageViewHolder(layoutInflater.inflate(R.layout.item_chat, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    MessageViewHolder vh = (MessageViewHolder) holder;
    Message i = items.get(position);
    vh.name.setText(i.getAuthor().getDisplayName());
    vh.avatarView.load(i.getAuthor().getProfilePicture());

    if (i instanceof MessageText) {
      vh.emoji.setVisibility(View.GONE);
      vh.message.setVisibility(View.VISIBLE);
      vh.image.setVisibility(View.GONE);

      vh.message.setText(((MessageText) i).getMessage());
    } else if (i instanceof MessageEmoji) {
      vh.emoji.setVisibility(View.VISIBLE);
      vh.message.setVisibility(View.GONE);
      vh.image.setVisibility(View.GONE);

      vh.emoji.setText(((MessageEmoji) i).getEmoji());
    } else if (i instanceof MessageImage) {
      vh.emoji.setVisibility(View.GONE);
      vh.message.setVisibility(View.GONE);
      vh.image.setVisibility(View.VISIBLE);
      Timber.e("SOEF IMAGE SIZE " + vh.image.getWidth());
      Original o = ((MessageImage) i).getOriginal();
      new GlideUtils.Builder(context).url(o.getUrl())
          .rounded(false)
          .target(vh.image)
          .size(vh.image.getWidth())
          .load();
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  static class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.message) public TextViewFont message;
    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.emoji) public TextViewFont emoji;
    @BindView(R.id.viewAvatar) public AvatarView avatarView;
    @BindView(R.id.header) public LinearLayout header;
    @BindView(R.id.image) public ImageView image;

    public MessageViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
