package com.tribe.app.presentation.view.adapter.delegate.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.AnimationUtils;
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
  protected Map<BaseListViewHolder, AnimatorSet> animations = new HashMap<>();

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickAdd = PublishSubject.create();
  protected final PublishSubject<View> clickRemove = PublishSubject.create();
  protected final PublishSubject<View> clickHangLive = PublishSubject.create();
  protected final PublishSubject<View> clickLong = PublishSubject.create();
  protected final PublishSubject<View> clickUnblock = PublishSubject.create();

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
        displayName = context.getString(R.string.contacts_section_search_searching);
      }
    } else if (item instanceof Membership) {
      Membership membership = (Membership) item;
      int size = membership.getGroup().getMembers() == null ? 0
          : membership.getGroup().getMembers().size();
      username = size + " " + (size > 1 ? context.getString(R.string.group_members)
          : context.getString(R.string.group_member));
    } else if (item instanceof User) {
      User user = (User) item;
      vh.txtNew.setVisibility(user.isNew() ? View.VISIBLE : View.GONE);
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

    vh.btnAdd.setVisibility(isActionAvailable ? View.VISIBLE : View.GONE);

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
      vh.txtAction.setAlpha(1);
      vh.progressBarAdd.setAlpha(0);

      setButton(vh.buttonModelFrom, vh);
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

    text = buttonModelTo.getText();
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

  protected AnimatorSet animateProgressBar(BaseListViewHolder vh) {
    AnimatorSet animatorSet = new AnimatorSet();

    ObjectAnimator alphaAnimAdd = ObjectAnimator.ofFloat(vh.txtAction, "alpha", 1f, 0f);

    ObjectAnimator alphaAnimProgress = ObjectAnimator.ofFloat(vh.progressBarAdd, "alpha", 0f, 1f);

    Animator animator =
        AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(), actionButtonHeight);

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(alphaAnimAdd).with(alphaAnimProgress).with(animator);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationCancel(Animator animation) {
        animatorSet.removeAllListeners();
        vh.txtAction.setAlpha(1);
        vh.progressBarAdd.setAlpha(1);
      }

      @Override public void onAnimationEnd(Animator animation) {

      }
    });
    animatorSet.start();
    return animatorSet;
  }

  private void setButton(ButtonModel buttonModel, BaseListViewHolder vh) {
    vh.txtAction.setText(buttonModel.getText());
    vh.txtAction.measure(0, 0);
    int width = vh.txtAction.getMeasuredWidth() + (2 * marginSmall);
    UIUtils.changeWidthOfView(vh.btnAdd, width);
    ViewGroup.MarginLayoutParams layoutInfos =
        (ViewGroup.MarginLayoutParams) vh.layoutInfos.getLayoutParams();
    layoutInfos.rightMargin = width + marginSmall;
    vh.layoutInfos.requestLayout();
    vh.txtAction.setTextColor(buttonModel.getTextColor());
    vh.gradientDrawable.setColor(buttonModel.getBackgroundColor());
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

  public Observable<View> onUnblock() {
    return clickUnblock;
  }
}
