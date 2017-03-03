package com.tribe.app.presentation.view.adapter.delegate.base;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/31/17.
 */
public abstract class BaseListAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  // VARIABLES
  protected LayoutInflater layoutInflater;
  protected Context context;
  protected int actionButtonHeight, marginSmall, avatarSize;

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickAdd = PublishSubject.create();
  protected final PublishSubject<View> clickRemove = PublishSubject.create();
  protected final PublishSubject<View> clickHangLive = PublishSubject.create();
  protected final PublishSubject<View> clickLong = PublishSubject.create();

  public BaseListAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    actionButtonHeight = context.getResources().getDimensionPixelSize(R.dimen.action_button_height);
    marginSmall = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    BaseListViewHolder vh =
        new BaseListViewHolder(layoutInflater.inflate(R.layout.item_friend_member, parent, false));
    vh.gradientDrawable = new GradientDrawable();
    vh.gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    vh.gradientDrawable.setCornerRadius(screenUtils.dpToPx(100));
    vh.btnAdd.setBackground(vh.gradientDrawable);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    if (!(items instanceof BaseListInterface)) return;

    BaseListViewHolder vh = (BaseListViewHolder) holder;
    BaseListInterface item = (BaseListInterface) items.get(position);

    ButtonModel buttonModelFrom = getButtonModelFrom(item);
    ButtonModel buttonModelTo = getButtonModelTo(item);
    String username = item.getUsername();
    String displayName = item.getDisplayName();
    boolean isFriend = item.isFriend();
    AvatarModel avatarModel = item.getAvatar();
    boolean isAnimateAdd = item.isAnimateAdd();
    boolean isReverse = item.isReverse();

    if (!StringUtils.isEmpty(avatarModel.getUrl())) {
      GlideUtils.load(context, avatarModel.getUrl(), avatarSize, vh.imgAvatar);
    }

    vh.txtName.setText(displayName);
    setFriendLabel(vh, isFriend);

    if (!StringUtils.isEmpty(username)) {
      vh.txtUsername.setText("@" + username);
    } else {
      vh.txtUsername.setText("");
    }

    if (isAnimateAdd) {
      item.setAnimateAdd(false);
      animateAdd(vh, buttonModelFrom, buttonModelTo);
    } else {
      if (isReverse) {
        setButton(buttonModelTo, vh);
      } else {
        setButton(buttonModelFrom, vh);
      }
    }

    setClicks(item, vh);
  }

  protected abstract ButtonModel getButtonModelFrom(BaseListInterface baseListItem);

  protected abstract ButtonModel getButtonModelTo(BaseListInterface baseListItem);

  protected abstract void setClicks(BaseListInterface baseList, BaseListViewHolder vh);

  private void animateAdd(BaseListViewHolder vh, ButtonModel buttonModelFrom,
      ButtonModel buttonModelTo) {
    AnimatorSet animatorSet = new AnimatorSet();
    String text;
    int textColorFrom, textColorTo;
    int bgColorFrom, bgColorTo;

    text = buttonModelFrom.getText();
    textColorFrom = buttonModelFrom.getTextColor();
    textColorTo = buttonModelTo.getTextColor();
    bgColorFrom = buttonModelFrom.getBackgroundColor();
    bgColorTo = buttonModelTo.getBackgroundColor();

    vh.txtAction.setText(text);
    vh.txtAction.measure(0, 0);

    Animator animator = AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(),
        vh.txtAction.getMeasuredWidth() + (2 * marginSmall));

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(animator);
    animatorSet.start();

    AnimationUtils.animateTextColor(vh.txtAction, textColorFrom, textColorTo, DURATION);
    AnimationUtils.animateBGColor(vh.btnAdd, bgColorFrom, bgColorTo, DURATION);
  }

  private void setButton(ButtonModel buttonModel, BaseListViewHolder vh) {
    vh.txtAction.setText(buttonModel.getText());
    vh.txtAction.measure(0, 0);
    UIUtils.changeWidthOfView(vh.btnAdd, vh.txtAction.getMeasuredWidth() + (2 * marginSmall));
    vh.txtAction.setTextColor(buttonModel.getTextColor());
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, buttonModel.getBackgroundColor()));
  }

  private void setFriendLabel(BaseListViewHolder vh, boolean isFriend) {
    int visibility = isFriend ? View.VISIBLE : View.GONE;
    vh.txtFriend.setVisibility(visibility);
    vh.txtBubble.setVisibility(visibility);
  }

  public Observable<View> clickAdd() {
    return clickAdd;
  }

  public Observable<View> clickRemove() {
    return clickRemove;
  }

  public Observable<View> onHangLive() {
    return clickHangLive;
  }

  public Observable<View> onLongClick() {
    return clickLong;
  }
}
