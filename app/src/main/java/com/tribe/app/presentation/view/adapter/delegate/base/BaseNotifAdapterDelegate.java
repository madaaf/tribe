package com.tribe.app.presentation.view.adapter.delegate.base;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import java.util.List;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 29/06/2017.
 */

public abstract class BaseNotifAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;
  private Animation anim;
  private ResizeAnimation resizeAnim;

  // OBSERVABLES
  protected PublishSubject<View> onClickAdd = PublishSubject.create();
  protected PublishSubject<View> clickMore = PublishSubject.create();

  public BaseNotifAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate90);
  }

  protected RecyclerView.ViewHolder onCreateViewHolderForUnfriend(ViewGroup parent) {
    BaseNotifViewHolder vh = new BaseNotifViewHolder(
        layoutInflater.inflate(R.layout.item_base_list_notif, parent, false));

    vh.btnMore.setOnClickListener(v -> clickMore.onNext(vh.itemView));
    vh.layoutAddFriend.setOnClickListener(v -> {
      onClickAdd.onNext(vh.itemView);
      vh.txtAction.setText(context.getString(R.string.action_adding_friend));
      resizeAnim = new ResizeAnimation(vh.addBtnBg);
      resizeAnim.setDuration(500);
      resizeAnim.setStartOffset(1000);
      resizeAnim.setAnimationListener(new AnimationListenerAdapter() {

        @Override public void onAnimationEnd(Animation animation) {
          super.onAnimationEnd(animation);
          vh.iconAdd.clearAnimation();
          vh.layoutAddFriend.setClickable(false);
          vh.addBtnBg.setBackground(ContextCompat.getDrawable(context,
              R.drawable.shape_rect_blueaddfriendplus_rounded_corners_expended));
          vh.iconAdd.setScaleX(0);
          vh.iconAdd.setScaleY(0);
          vh.iconAdd.animate()
              .setInterpolator(new OvershootInterpolator())
              .setDuration(300)
              .scaleX(1)
              .scaleY(1)
              .withStartAction(() -> vh.iconAdd.setImageResource(R.drawable.added_icon_bg));
          vh.txtAction.setText(context.getString(R.string.action_friend_added));
        }
      });
      resizeAnim.setParams(vh.addBtnBg.getWidth(), vh.layoutAddFriend.getWidth(),
          vh.addBtnBg.getHeight(), vh.addBtnBg.getHeight());
      vh.addBtnBg.startAnimation(resizeAnim);
    });
    return vh;
  }

  protected void onBindViewHolderForUnfriend(List<Object> items, int position,
      RecyclerView.ViewHolder holder) {
    BaseNotifViewHolder vh = (BaseNotifViewHolder) holder;
    User user = (User) items.get(position);

    vh.txtName.setText(user.getDisplayName());
    vh.txtDescription.setText(user.getUsername());
    vh.viewAvatar.setHasShadow(false);
    vh.viewAvatar.load("");
    vh.txtAction.setText(context.getString(R.string.action_add_friend));
    vh.iconAdd.setImageResource(R.drawable.add_icon_bg);
    vh.iconAdd.startAnimation(anim);
  }
}
