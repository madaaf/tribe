package com.tribe.app.presentation.view.adapter.delegate.newchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 09/04/2017
 */
public class ShortcutNewChatAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  protected LayoutInflater layoutInflater;
  protected Context context;
  private int width = 0;

  private PublishSubject<View> onClick = PublishSubject.create();

  public ShortcutNewChatAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    return items.get(position) instanceof Shortcut &&
        !items.get(position).getId().equals(Shortcut.ID_EMPTY) &&
        !items.get(position).getId().equals(Shortcut.ID_CALL_ROULETTE);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ShortcutInviteViewHolder shortcutInviteViewHolder =
        new ShortcutInviteViewHolder(layoutInflater.inflate(R.layout.item_shortcut, parent, false));
    return shortcutInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    Shortcut shortcut = (Shortcut) items.get(position);
    bind(vh, shortcut);
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    Shortcut shortcut = (Shortcut) items.get(position);
    bind(vh, shortcut);
  }

  private void bind(ShortcutInviteViewHolder vh, Shortcut shortcut) {
    Timber.d("bind : " + shortcut);
    User user = shortcut.getSingleFriend();
    vh.viewNewAvatar.load(shortcut);
    vh.txtName.setText(user.getDisplayName());

    vh.itemView.setOnClickListener(view -> onClick.onNext(vh.itemView));

    if (shortcut.isAnimateAdd()) {
      Timber.d("animateAdd : " + shortcut);
      if (shortcut.isSelected()) {
        AnimationUtils.fadeIn(vh.viewBg, DURATION);
        vh.txtAdded.setText(R.string.action_friend);
        TextViewCompat.setTextAppearance(vh.txtAdded, R.style.BiggerBody_One_BlueNew);
        vh.txtAdded.setCustomFont(context, FontUtils.PROXIMA_REGULAR);

        vh.imgSelected.animate()
            .translationX(0)
            .setDuration(DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        vh.layoutContent.animate()
            .translationX(screenUtils.dpToPx(60))
            .setDuration(DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .start();
      } else {
        AnimationUtils.fadeOut(vh.viewBg, DURATION);
        vh.txtAdded.setText(R.string.action_tap_to_add);
        TextViewCompat.setTextAppearance(vh.txtAdded, R.style.BiggerBody_One_Black40);
        vh.txtAdded.setCustomFont(context, FontUtils.PROXIMA_REGULAR);

        vh.imgSelected.animate()
            .translationX(-screenUtils.dpToPx(60))
            .setDuration(DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        vh.layoutContent.animate()
            .translationX(0)
            .setDuration(DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .start();
      }

      shortcut.setAnimateAdd(false);
    } else {
      Timber.d("notAnimate : " + shortcut);
      if (shortcut.isSelected()) {
        vh.imgSelected.setTranslationX(0);
        vh.layoutContent.setTranslationX(screenUtils.dpToPx(60));
        vh.viewBg.setAlpha(1);
      } else {
        vh.layoutContent.setTranslationX(0);
        vh.imgSelected.setTranslationX(-screenUtils.dpToPx(60));
        vh.viewBg.setAlpha(0);
      }
    }
  }

  static class ShortcutInviteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgSelected) ImageView imgSelected;

    @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtAdded) TextViewFont txtAdded;

    @BindView(R.id.viewBG) View viewBg;

    @BindView(R.id.layoutContent) ViewGroup layoutContent;

    public ShortcutInviteViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onClick() {
    return onClick;
  }
}
