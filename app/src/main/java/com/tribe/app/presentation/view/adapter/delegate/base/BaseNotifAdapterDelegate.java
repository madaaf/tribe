package com.tribe.app.presentation.view.adapter.delegate.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import java.util.List;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 29/06/2017.
 */

public abstract class BaseNotifAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;

  // OBSERVABLES
  protected PublishSubject<View> onClickAdd = PublishSubject.create();
  protected PublishSubject<View> onUnblock = PublishSubject.create();
  protected PublishSubject<View> clickMore = PublishSubject.create();

  public BaseNotifAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  protected RecyclerView.ViewHolder onCreateViewHolderNotif(ViewGroup parent) {
    BaseNotifViewHolder vh = new BaseNotifViewHolder(
        layoutInflater.inflate(R.layout.item_base_list_notif, parent, false));
    return vh;
  }

  protected void onBindViewHolderForUnfriend(User friend, RecyclerView.ViewHolder holder,
      boolean isHidden, User user) {
    BaseNotifViewHolder vh = (BaseNotifViewHolder) holder;

    vh.txtName.setText(friend.getDisplayName());
    vh.txtDescription.setText("@" + friend.getUsername());
    vh.viewAvatar.load("");
    vh.btnMore.setOnClickListener(v -> {
      Toast.makeText(context, context.getString(R.string.action_user_reported), Toast.LENGTH_SHORT)
          .show();
      clickMore.onNext(vh.itemView);
    });

    Shortcut s = ShortcutUtil.getShortcut(friend, user);
    if (s == null) {
      vh.btnAdd.setOnClickListener(v -> {
        vh.btnAdd.setImageResource(R.drawable.done_btn);
        onClickAdd.onNext(vh.itemView);
      });
    } else {
      vh.btnAdd.setImageResource(R.drawable.done_btn);
    }
  }
}
