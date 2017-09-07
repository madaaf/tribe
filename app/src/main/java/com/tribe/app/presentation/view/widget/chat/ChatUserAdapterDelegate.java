package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatUserAdapterDelegate extends RxAdapterDelegate<List<User>> {

  protected LayoutInflater layoutInflater;

  private Context context;

  public ChatUserAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
  }

  @Override public boolean isForViewType(@NonNull List<User> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new ChatUserViewHolder(layoutInflater.inflate(R.layout.item_user_chat, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<User> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ChatUserViewHolder vh = (ChatUserViewHolder) holder;
    User i = items.get(position);
    vh.name.setText(i.getDisplayName());
    vh.avatarView.load(i.getProfilePicture());
  }

  @Override
  public void onBindViewHolder(@NonNull List<User> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  static class ChatUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.viewAvatar) public AvatarView avatarView;

    public ChatUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
