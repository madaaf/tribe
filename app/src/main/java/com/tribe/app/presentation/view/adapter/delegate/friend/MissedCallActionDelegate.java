package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.notification.MissedCallAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by madaaflak on 28/04/2017.
 */

public class MissedCallActionDelegate extends BaseListAdapterDelegate {

  private SimpleDateFormat sdf;

  public MissedCallActionDelegate(Context context) {
    super(context);
    sdf = new SimpleDateFormat("MMM dd, HH:mm");
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    return getHangLiveButton();
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    return getHangLiveButton();
  }

  @Override protected void setClicks(BaseListInterface baseListItem, BaseListViewHolder vh) {
    MissedCallAction missedCallAction = (MissedCallAction) baseListItem;

    if (missedCallAction.getNbrMissedCall() > 1) {
      vh.txtUsername.setText(context.getString(R.string.callback_notification_many_missed_call,
          Integer.toString(missedCallAction.getNbrMissedCall())));
    } else {
      vh.txtUsername.setText(context.getString(R.string.callback_notification_one_missed_call,
          Integer.toString(missedCallAction.getNbrMissedCall())));
    }

    String time = formatDate(missedCallAction.getNotificationPayload().getTime());
    vh.txtFriend.setText(time);
    vh.btnAdd.setOnClickListener(v -> clickHangLive.onNext(vh));
  }

  private String formatDate(long yourMilliSeconds) {
    Date resultDate = new Date(yourMilliSeconds);
    return sdf.format(resultDate);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof MissedCallAction;
  }

  private ButtonModel getHangLiveButton() {
    return new ButtonModel(context.getString(R.string.action_hang_live),
        ContextCompat.getColor(context, R.color.red), Color.WHITE);
  }
}
