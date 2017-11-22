package com.tribe.app.presentation.view.adapter.delegate.base;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.notification.MissedCallAction;
import com.tribe.app.presentation.view.utils.ScreenUtils;
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
  protected Map<BaseListViewHolder, AnimatorSet> animations = new HashMap<>();

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickAdd = PublishSubject.create();
  protected final PublishSubject<View> clickRemove = PublishSubject.create();
  protected final PublishSubject<View> clickLong = PublishSubject.create();
  protected final PublishSubject<BaseListViewHolder> clickHangLive = PublishSubject.create();
  protected final PublishSubject<BaseListViewHolder> clickUnblock = PublishSubject.create();

  public BaseListAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    marginSmall = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
    actionButtonHeight =
        context.getResources().getDimensionPixelSize(R.dimen.action_button_height) + marginSmall;
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    BaseListViewHolder vh =
        new BaseListViewHolder(layoutInflater.inflate(R.layout.item_base_list, parent, false));
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
    if (!(items.get(position) instanceof BaseListInterface)) return;

    BaseListViewHolder vh = (BaseListViewHolder) holder;
    BaseListInterface item = (BaseListInterface) items.get(position);

    String username = StringUtils.isEmpty(item.getUsername()) ? "" : "@" + item.getUsername();
    String displayName = item.getDisplayName();
    boolean isFriend = item.isFriend();
    AvatarModel avatarModel = item.getAvatar();
    boolean isAnimateAdd = item.isAnimateAdd();
    boolean isActionAvailable = item.isActionAvailable(user);
    boolean isInvisible = item.isInvisible();

    if (item instanceof SearchResult && StringUtils.isEmpty(displayName)) {
      SearchResult searchResult = (SearchResult) item;
      if (searchResult.isSearchDone()) {
        displayName = "No user found";
      } else {
        displayName = context.getString(R.string.search_searching);
      }
    } else if (item instanceof User) {
      User user = (User) item;
      vh.txtNew.setVisibility(user.isNew() ? View.VISIBLE : View.GONE);
    } else if (item instanceof MissedCallAction) {
      //  vh.btnAdd.setVisibility(View.VISIBLE);
    }

    if (animations.containsKey(holder)) {
      AnimatorSet animatorSet = animations.get(holder);
      animatorSet.removeAllListeners();
      animatorSet.end();
      animatorSet.cancel();
      animatorSet.setDuration(0);
      animations.remove(holder);
    }

    vh.buttonModelFrom = getButtonModelFrom(item);
    vh.buttonModelTo = getButtonModelTo(item);

    vh.viewAvatar.setType(avatarModel.getType());
    if (avatarModel.getMemberPics() != null) {
      vh.viewAvatar.loadGroupAvatar(avatarModel.getUrl(), null, item.getId(),
          avatarModel.getMemberPics());
    } else {
      vh.viewAvatar.load(avatarModel.getUrl());
    }

    vh.txtName.setText(displayName);
    setFriendLabel(vh, isFriend);

    if (!StringUtils.isEmpty(username)) {
      vh.txtUsername.setText(username);
    } else {
      vh.txtUsername.setText("");
    }

    if (isAnimateAdd) {
      item.setAnimateAdd(false);

      animateAdd(vh, vh.buttonModelTo, vh.buttonModelFrom);

      vh.buttonModelFrom = getButtonModelFrom(item);
      vh.buttonModelTo = getButtonModelTo(item);
    } else {
      setButton(vh.buttonModelFrom, vh);
    }

    setClicks(item, vh);
  }

  protected abstract ButtonModel getButtonModelFrom(BaseListInterface baseListItem);

  protected abstract ButtonModel getButtonModelTo(BaseListInterface baseListItem);

  protected abstract void setClicks(BaseListInterface baseList, BaseListViewHolder vh);

  private void animateAdd(BaseListViewHolder vh, ButtonModel buttonModelFrom,
      ButtonModel buttonModelTo) {
    if (buttonModelFrom.getImageRessource() != 0) {
      vh.progressView.setVisibility(View.VISIBLE);
      vh.btnAdd.setVisibility(View.GONE);
      vh.btnAdd.setClickable(false);
    } else {
      vh.gradientDrawable.setColor(buttonModelFrom.getBackgroundColor());
    }
  }

  private void setButton(ButtonModel buttonModel, BaseListViewHolder vh) {
    vh.layoutInfos.requestLayout();
    if (buttonModel.getImageRessource() != 0) {
      vh.btnAdd.setImageResource(buttonModel.getImageRessource());
    } else {
      vh.gradientDrawable.setColor(buttonModel.getBackgroundColor());
    }
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

  public Observable<BaseListViewHolder> onHangLive() {
    return clickHangLive;
  }

  public Observable<View> onLongClick() {
    return clickLong;
  }

  public Observable<BaseListViewHolder> onUnblock() {
    return clickUnblock;
  }
}
