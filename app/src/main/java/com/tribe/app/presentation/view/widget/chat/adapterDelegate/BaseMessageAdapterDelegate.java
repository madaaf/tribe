package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.app.Activity;
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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.ChatView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEvent;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 21/09/2017.
 */

public abstract class BaseMessageAdapterDelegate extends RxAdapterDelegate<List<Message>> {

  final private static int DIFF_TIMING_ALLOWED_MINUTE = 2;

  private int currentSeenPosition;
  private BaseTextViewHolder playingHolder;
  protected DateUtils dateUtils;
  protected ScreenUtils screenUtils;
  protected Navigator navigator;

  protected Context context;
  protected int type;
  protected LayoutInflater layoutInflater;

  private PublishSubject<List<Object>> onPopulateSCLastSeen = PublishSubject.create();
  private PublishSubject<Message> onLongClickItem = PublishSubject.create();

  public BaseMessageAdapterDelegate(Context context, int type) {
    this.type = type;
    this.context = context;
    dateUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
        .dateUtils();
    screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
        .screenUtils();
    navigator = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
        .navigator();
    currentSeenPosition = -1;
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    BaseTextViewHolder vh = getViewHolder(parent);
    if (type == ChatView.FROM_LIVE) {
      vh.name.setTextColor(ContextCompat.getColor(context, R.color.white));
      vh.time.setTextColor(ContextCompat.getColor(context, R.color.white_opacity_40));
      vh.time2.setTextColor(ContextCompat.getColor(context, R.color.white_opacity_40));
      vh.daySeparator.setTextColor(ContextCompat.getColor(context, R.color.white));
    }
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  protected void setPendingBehavior(Message m, View container) {
    if (m.isPending()) {
      container.setAlpha(0.4f);
    } else {
      container.setAlpha(1f);
    }
  }

  private void populateShortcutLastSeen(BaseTextViewHolder vh, Message m) {
    List<Object> list = new ArrayList<>();
    list.add(vh.shortcutLastSeen);
    list.add(m);
    onPopulateSCLastSeen.onNext(list);
  }

  private void onClickItem(int position, BaseTextViewHolder vh) {
    if (position == currentSeenPosition) {
      if (vh.shortcutLastSeen.getVisibility() == View.GONE
          || vh.shortcutLastSeen.getVisibility() == View.INVISIBLE) {
        vh.shortcutLastSeen.setVisibility(View.VISIBLE);
      } else {
        vh.shortcutLastSeen.setVisibility(View.GONE);
      }
    } else {
      currentSeenPosition = position;
      if (null != playingHolder) {
        playingHolder.shortcutLastSeen.setVisibility(View.GONE);
      }
      playingHolder = vh;
      vh.shortcutLastSeen.setVisibility(View.VISIBLE);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    BaseTextViewHolder vh = (BaseTextViewHolder) holder;
    Message m = items.get(position);

    populateShortcutLastSeen(vh, m);

    vh.itemView.setOnClickListener(view -> {
      screenUtils.hideKeyboard((Activity) context);
      onClickItem(position, vh);
    });

    vh.itemView.setOnLongClickListener(view -> {
      onLongClickItem.onNext(m);
      return true;
    });

    if (position == items.size() - 1) {
      vh.shortcutLastSeen.setVisibility(View.VISIBLE);
    } else {
      vh.shortcutLastSeen.setVisibility(View.GONE);
    }

    if (m instanceof MessageEvent) {
      vh.header.setVisibility(View.GONE);
      return;
    }

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

      if (previous.getAuthor() != null && m.getAuthor() != null && previous.getAuthor()
          .getId()
          .equals(m.getAuthor().getId())) {
        if (dateUtils.getDiffDate(previous.getCreationDate(), time) > DIFF_TIMING_ALLOWED_MINUTE) {
          vh.time2.setText(dateUtils.getHourAndMinuteInLocal(time));
          vh.time2.setVisibility(View.VISIBLE);
        }
        vh.header.setVisibility(View.GONE);
      } else {
        vh.header.setVisibility(View.VISIBLE);
      }
    } else {
      vh.daySeparatorContainer.setVisibility(View.VISIBLE);
      vh.header.setVisibility(View.VISIBLE);
    }
  }

  protected abstract BaseTextViewHolder getViewHolder(ViewGroup parent);

  static abstract class BaseTextViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewAvatar) public AvatarView avatarView;
    @BindView(R.id.name) public TextViewFont name;
    @BindView(R.id.time) public TextViewFont time;
    @BindView(R.id.header) public LinearLayout header;
    @BindView(R.id.daySeparatorContainer) public LinearLayout daySeparatorContainer;
    @BindView(R.id.daySeparator) public TextViewFont daySeparator;
    @BindView(R.id.time2) public TextViewFont time2;
    @BindView(R.id.shortcutLastSeen) public TextViewFont shortcutLastSeen;

    public BaseTextViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    protected abstract ViewGroup getLayoutContent();
  }

  public Observable<List<Object>> onPopulateSCLastSeen() {
    return onPopulateSCLastSeen;
  }

  public Observable<Message> onLongClickItem() {
    return onLongClickItem;
  }
}
