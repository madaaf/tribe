package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEvent;
import java.util.List;

/**
 * Created by madaaflak on 21/09/2017.
 */

public abstract class BaseMessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  protected DateUtils dateUtils;

  protected Context context;
  protected LayoutInflater layoutInflater;

  public BaseMessageAdapterDelegate(Context context) {
    this.context = context;
    dateUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
        .dateUtils();
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message m = items.get(position);
    return !(m instanceof MessageEvent);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return getViewHolder(parent);
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    BaseTextViewHolder vh = (BaseTextViewHolder) holder;
    Message m = items.get(position);

    String time = m.getCreationDate();

    vh.time.setText(dateUtils.getHourAndMinuteInLocal(time));
    vh.avatarView.load(m.getAuthor().getProfilePicture());
    vh.name.setText(m.getAuthor().getDisplayName());
    vh.daySeparator.setText(dateUtils.getFormattedDayId(time));

    if (position > 0) {
      Message previous = items.get(position - 1);

      if (dateUtils.getDayId(previous.getCreationDate()).equals(dateUtils.getDayId(time))) {
        vh.daySeparatorContainer.setVisibility(View.GONE);
      } else {
        vh.daySeparatorContainer.setVisibility(View.VISIBLE);
      }

      if (previous.getAuthor().getId().equals(m.getAuthor().getId())) {
        if (dateUtils.getDiffDate(previous.getCreationDate(), time) > 2) {
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
  }

  protected abstract BaseTextViewHolder getViewHolder(ViewGroup parent);

  static abstract class BaseTextViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewAvatar) public AvatarView avatarView;
    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.time) public TextViewFont time;
    @BindView(R.id.header) public LinearLayout header;
    @BindView(R.id.daySeparatorContainer) public FrameLayout daySeparatorContainer;
    @BindView(R.id.daySeparator) public TextViewFont daySeparator;
    @BindView(R.id.time2) public TextViewFont time2;

    public BaseTextViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    protected abstract ViewGroup getLayoutContent();
  }
}
