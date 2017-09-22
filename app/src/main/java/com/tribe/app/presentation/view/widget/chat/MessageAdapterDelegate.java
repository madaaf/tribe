package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.model.MessageEvent;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  protected LayoutInflater layoutInflater;

  @Inject Navigator navigator;
  @Inject DateUtils dateUtils;

  private Context context;
  private int imageSize;
  //private Message stamp = null;

  private PublishSubject<List<Object>> onPictureTaken = PublishSubject.create();

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

  private void setVisibilityItem(MessageViewHolder vh, String type) {
    switch (type) {
      case Message.MESSAGE_TEXT:
        vh.emoji.setVisibility(View.GONE);
        vh.message.setVisibility(View.VISIBLE);
        vh.image.setVisibility(View.GONE);
        vh.containerNotif.setVisibility(View.GONE);
        break;
      case Message.MESSAGE_EMOJI:
        vh.emoji.setVisibility(View.VISIBLE);
        vh.message.setVisibility(View.GONE);
        vh.image.setVisibility(View.GONE);
        vh.containerNotif.setVisibility(View.GONE);
        break;
      case Message.MESSAGE_IMAGE:
        vh.emoji.setVisibility(View.GONE);
        vh.message.setVisibility(View.GONE);
        vh.image.setVisibility(View.VISIBLE);
        vh.containerNotif.setVisibility(View.GONE);
        break;
      case Message.MESSAGE_EVENT:
        vh.emoji.setVisibility(View.GONE);
        vh.message.setVisibility(View.GONE);
        vh.image.setVisibility(View.GONE);
        vh.containerNotif.setVisibility(View.VISIBLE);
        break;
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {

    MessageViewHolder vh = (MessageViewHolder) holder;
    Message m = items.get(position);

    String time = m.getCreationDate();
    vh.time.setText(dateUtils.getHourAndMinuteInLocal(time));
    vh.daySeparator.setText(dateUtils.getFormattedDayId(time));

    if (position > 0) {
      Message previous = items.get(position - 1);

      if (dateUtils.getDayId(previous.getCreationDate())
          .equals(dateUtils.getDayId(m.getCreationDate()))) {
        vh.daySeparatorContainer.setVisibility(View.GONE);
      } else {
        vh.daySeparatorContainer.setVisibility(View.VISIBLE);
      }

      if (previous.getAuthor().getId().equals(m.getAuthor().getId())) {
        if (dateUtils.getDiffDate(previous.getCreationDate(), m.getCreationDate()) > 2) {
          vh.time2.setText(dateUtils.getHourAndMinuteInLocal(time));
          vh.time2.setVisibility(View.VISIBLE);
        }
        vh.header.setVisibility(View.GONE);
      } else {
        vh.header.setVisibility(View.VISIBLE);
      }
    } else {
      vh.daySeparatorContainer.setVisibility(View.VISIBLE);
    }

    //  stamp = m;
    vh.name.setText(m.getAuthor().getDisplayName());
    vh.avatarView.load(m.getAuthor().getProfilePicture());

    if (m instanceof MessageEvent) {
      //  Timber.e("ON BIND HOLDER " + m + " " + ((MessageEvent) m).toString());
      setVisibilityItem(vh, Message.MESSAGE_EVENT);
      vh.notifContent.setText(
          ((MessageEvent) m).getContent(((MessageEvent) m).getUser().getDisplayName()));
      vh.avatarNotif.load(((MessageEvent) m).getUser().getProfilePicture());
      return;
    } else if (m instanceof MessageText) {
      setVisibilityItem(vh, Message.MESSAGE_TEXT);
      vh.message.setText(((MessageText) m).getMessage());
    } else if (m instanceof MessageEmoji) {
      setVisibilityItem(vh, Message.MESSAGE_EMOJI);
      vh.emoji.setText(((MessageEmoji) m).getEmoji());
    } else if (m instanceof MessageImage) {
      setVisibilityItem(vh, Message.MESSAGE_IMAGE);

      Image o = ((MessageImage) m).getOriginal();
      Uri uri = ((MessageImage) m).getUri();

      Glide.with(context).load(o.getUrl()).asBitmap().into(new BitmapImageViewTarget(vh.image) {
        @Override protected void setResource(Bitmap resource) {
          RoundedBitmapDrawable circularBitmapDrawable =
              RoundedBitmapDrawableFactory.create(context.getResources(), resource);
          circularBitmapDrawable.setCornerRadius(30);
          vh.image.setImageDrawable(circularBitmapDrawable);
        }
      });

      if (uri != null) {
        vh.image.setAlpha(0.4f);
        List<Object> list = new ArrayList<>();
        list.add(uri);
        list.add(vh.image);
        onPictureTaken.onNext(list);
        ((MessageImage) m).setUri(null);
      } else {
        vh.image.setAlpha(1f);
      }

      vh.image.setOnClickListener(v -> {
        navigator.navigateToPicture(context, o.getUrl());
      });
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  static class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.viewAvatar) public AvatarView avatarView;
    @BindView(R.id.time) public TextViewFont time;
    @BindView(R.id.message) public TextViewFont message;
    @BindView(R.id.emoji) public TextViewFont emoji;
    @BindView(R.id.image) public ImageView image;
    @BindView(R.id.header) public LinearLayout header;

    @BindView(R.id.viewAvatarNotif) public AvatarView avatarNotif;
    @BindView(R.id.notifContent) public TextViewFont notifContent;
    @BindView(R.id.time2) public TextViewFont time2;
    @BindView(R.id.containerNotif) public LinearLayout containerNotif;
    @BindView(R.id.daySeparatorContainer) public FrameLayout daySeparatorContainer;
    @BindView(R.id.daySeparator) public TextViewFont daySeparator;

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

  public Observable<List<Object>> onPictureTaken() {
    return onPictureTaken;
  }
}
