package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  protected LayoutInflater layoutInflater;

  private Context context;

  public MessageAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
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

/*    vh.txtName.setText(i.getMessage());
    vh.name.setText(i.getAuthor().getDisplayName());
    vh.avatarView.load(i.getAuthor().getProfilePicture());*/
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  static class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text) public TextViewFont txtName;
    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.viewAvatar) public AvatarView avatarView;
    @BindView(R.id.header) public LinearLayout header;

    public MessageViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
