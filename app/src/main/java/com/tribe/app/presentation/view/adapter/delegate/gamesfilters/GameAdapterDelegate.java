package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import com.tribe.tribelivesdk.model.TribeGuest;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 06/02/17.
 */
public class GameAdapterDelegate extends RxAdapterDelegate<List<Game>> {

  protected static final int DURATION = 100;
  protected static final int DURATION_MOVING = 2500;
  protected static final float OVERSHOOT_LIGHT = 0.45f;

  @Inject ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;
  private int radius;
  private GradientDrawable gradientDrawable;
  private Map<String, ValueAnimator> mapAnimator;

  protected PublishSubject<View> click = PublishSubject.create();

  public GameAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    radius = screenUtils.dpToPx(10);
    mapAnimator = new HashMap<>();
  }

  @Override public boolean isForViewType(@NonNull List<Game> items, int position) {
    Game g = items.get(position);
    return !(g instanceof GameFooter);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final GameViewHolder vh =
        new GameViewHolder(layoutInflater.inflate(R.layout.item_game, parent, false));

    float radius = 5;

    vh.viewBlur.setupWith((ViewGroup) vh.itemView)
        .blurAlgorithm(new RenderScriptBlur(context))
        .blurRadius(radius);

    vh.cardView.setOnClickListener(v -> click.onNext(vh.itemView));

    animateImgX(vh.imgAnimation1);
    animateImgX(vh.imgAnimation2);
    animateImgX(vh.imgAnimation3);

    return vh;
  }

  private void animateImgX(ImageView imgAnimation) {
    int rdm = new Random().nextInt(5) - 2;

    ValueAnimator animator = mapAnimator.get(imgAnimation.getId());
    if (animator != null) {
      animator.cancel();
    }

    animator = ValueAnimator.ofInt(0, screenUtils.dpToPx(rdm));
    animator.setDuration(DURATION_MOVING);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.addUpdateListener(animation -> {
      int translation = (int) animation.getAnimatedValue();
      imgAnimation.setTranslationX(translation);
      imgAnimation.setTranslationY(translation);
    });
    animator.start();
  }

  @Override public void onBindViewHolder(@NonNull List<Game> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GameViewHolder vh = (GameViewHolder) holder;
    Game game = items.get(position);

    if (game.getFriendLeader() != null) {
      UIUtils.changeHeightOfView(vh.layoutContent, screenUtils.dpToPx(200));
      TribeGuest leader = game.getFriendLeader();
      vh.layoutBestFriend.setVisibility(View.VISIBLE);
      vh.viewAvatar.load(leader.getPicture());
      vh.txtEmojiGame.setText(game.getEmoji());

      String text = context.getString(R.string.leaderboard_friend_is_best, leader.getDisplayName());

      //SpannableString string = new SpannableString(text);
      //int indexOf = text.indexOf(leader.getDisplayName());
      //string.setSpan(new TextAppearanceSpan(context, R.style.BiggerBody_Two_Black), 0,
      //    indexOf + leader.getDisplayName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

      vh.txtName.setText(text);
    } else {
      UIUtils.changeHeightOfView(vh.layoutContent, screenUtils.dpToPx(156));
      vh.layoutBestFriend.setVisibility(View.GONE);
    }

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });
    gd.setCornerRadius(radius);

    ViewCompat.setBackground(vh.viewBackground, gd);

    vh.txtBaseline.setText(game.getBaseline());

    new GlideUtils.GameImageBuilder(context, screenUtils).url(game.getIcon())
        .hasBorder(true)
        .hasPlaceholder(true)
        .rounded(true)
        .target(vh.imgIcon)
        .load();

    Glide.with(context).load(game.getLogo()).into(vh.imgLogo);

    for (int i = 0; i < game.getAnimation_icons().size(); i++) {
      String url = game.getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = vh.imgAnimation1;
      } else if (i == 1) {
        imageView = vh.imgAnimation2;
      } else if (i == 2) {
        imageView = vh.imgAnimation3;
      }

      Glide.with(context).load(url).into(imageView);
    }

    if (game.isFeatured()) {
      vh.txtInfo.setText(R.string.new_game_featured);
    } else if (game.isNew()) {
      vh.txtInfo.setText(R.string.new_game_new);
    } else {
      vh.txtInfo.setText("");
    }

    vh.txtPlayCount.setText(context.getString(R.string.new_game_plays, "" + game.getPlays_count()));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Game> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  static class GameViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewBackground) View viewBackground;
    @BindView(R.id.imgIcon) ImageView imgIcon;
    @BindView(R.id.imgLogo) ImageView imgLogo;
    @BindView(R.id.txtBaseline) TextViewFont txtBaseline;
    @BindView(R.id.txtInfo) TextViewFont txtInfo;
    @BindView(R.id.txtPlayCount) TextViewFont txtPlayCount;
    @BindView(R.id.cardView) CardView cardView;
    @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
    @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
    @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
    @BindView(R.id.viewBlur) BlurView viewBlur;
    @BindView(R.id.layoutContent) FrameLayout layoutContent;
    @BindView(R.id.layoutBestFriend) RelativeLayout layoutBestFriend;
    @BindView(R.id.viewAvatar) AvatarView viewAvatar;
    @BindView(R.id.txtEmojiGame) TextViewFont txtEmojiGame;
    @BindView(R.id.txtName) TextViewFont txtName;

    public GameViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
