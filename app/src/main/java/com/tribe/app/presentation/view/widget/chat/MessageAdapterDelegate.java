package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
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
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  protected LayoutInflater layoutInflater;

  @Inject Navigator navigator;

  private Context context;
  private int imageSize;
  private Message stamp = null;

  public MessageAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
    // DEFAULT SIZE
    imageSize = context.getResources().getDimensionPixelSize(R.dimen.image_size);
    initDependencyInjector();
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
    if (stamp != null && stamp.getAuthor().getId().equals(i.getAuthor().getId())) {
      vh.header.setVisibility(View.GONE);
    } else {
      vh.header.setVisibility(View.VISIBLE);
    }
    stamp = i;
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
      Image o = ((MessageImage) i).getOriginal();

      /*new GlideUtils.Builder(context).url(o.getUrl())
          .rounded(false)
          .target(vh.image)
          .hasPlaceholder(false)
          .load();*/

      Glide.with(context)
          .load(o.getUrl())
          .bitmapTransform(new RoundedCornersTransformation(context, 35, 0))
          .placeholder(ContextCompat.getColor(context, R.color.blue_new))
          .into(vh.image);

      vh.image.setOnClickListener(v -> {
        navigator.navigateToPicture(context, o.getUrl());
      });
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

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) context).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) context));
  }
}
