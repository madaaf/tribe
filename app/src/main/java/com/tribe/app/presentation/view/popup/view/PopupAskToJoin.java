package com.tribe.app.presentation.view.popup.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.StringDef;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.popup.listener.PopupAskToJoinListener;
import com.tribe.app.presentation.view.transformer.PositionedCropTransformation;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 03/07/2018
 */

public class PopupAskToJoin extends PopupView {

  @StringDef({ ASK_TO_JOIN, INVITED_TO_JOIN }) public @interface PopupType {
  }

  public static final String ASK_TO_JOIN = "ASK_TO_JOIN";
  public static final String INVITED_TO_JOIN = "INVITED_TO_JOIN";

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgTop) ImageView imgTop;

  @BindView(R.id.viewNewAvatar) NewAvatarView newAvatarView;

  @BindView(R.id.btnPositive) TextViewFont btnPositive;

  @BindView(R.id.btnNegative) TextViewFont btnNegative;

  @BindView(R.id.txtScore) TextViewScore txtViewScore;

  @BindView(R.id.txtGame) TextViewFont txtGame;

  @BindView(R.id.cardViewTrophy) CardView cardViewTrophy;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtTrophy) TextViewFont txtTrophy;

  @BindView(R.id.txtDesc) TextViewFont txtDesc;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;

  @BindView(R.id.separator) View separator;

  // VARIABLES
  private GameManager gameManager;
  private User user;
  private List<User> members;
  private Game game;
  private String roomId;
  private @PopupType String popupType;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PopupAskToJoin(Builder builder) {
    super(builder.context);

    popupType = builder.popupType;
    gameManager = GameManager.getInstance(builder.context);
    game = gameManager.getGameById(builder.gameId);
    user = builder.user;
    members = builder.members;
    roomId = builder.roomId;

    LayoutInflater.from(getContext()).inflate(R.layout.view_popup_ask_to_join, this, true);
    ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initSubscriptions();
    initUI();
  }

  public static class Builder {

    private Context context;
    private User user;
    private String gameId, roomId;
    private @PopupType String popupType;
    private List<User> members = new ArrayList<>();

    public Builder(Context context, @PopupType String popupType) {
      this.context = context;
      this.popupType = popupType;
    }

    public Builder game(String gameId) {
      this.gameId = gameId;
      return this;
    }

    public Builder room(String roomId) {
      this.roomId = roomId;
      return this;
    }

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder members(List<User> members) {
      if (members == null) return this;
      this.members.addAll(members);
      return this;
    }

    public PopupAskToJoin build() {
      return new PopupAskToJoin(this);
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    GradientDrawable gd = null;
    Score score = null;
    TrophyEnum trophyEnum = TrophyEnum.NOOB;
    String displayName = "";

    if (user != null) {
      newAvatarView.load(user);
      trophyEnum = TrophyEnum.getTrophyEnum(user.getTrophy());
      displayName = user.getDisplayName();
    }

    if (popupType.equals(INVITED_TO_JOIN)) {
      btnNegative.setText(R.string.invited_to_join_popup_action_later);
      txtTitle.setText(getContext().getString(R.string.invited_to_join_popup_title, displayName));
      String desc = "";

      if (members.size() > 0) {
        for (User user : members) {
          desc += user.getDisplayName() + ", ";
        }

        desc.substring(0, desc.length() - 2);
        txtDesc.setText(getContext().getString(R.string.invited_to_join_popup_subtitle, desc));
      } else {
        txtDesc.setVisibility(View.GONE);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layoutConstraint);
        constraintSet.connect(txtTitle.getId(), ConstraintSet.BOTTOM, R.id.separator,
            ConstraintSet.TOP);
        constraintSet.connect(R.id.separator, ConstraintSet.TOP, txtTitle.getId(),
            ConstraintSet.BOTTOM);
        constraintSet.applyTo(layoutConstraint);
      }
    } else {
      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(layoutConstraint);
      constraintSet.connect(txtTitle.getId(), ConstraintSet.BOTTOM, R.id.separator,
          ConstraintSet.TOP);
      constraintSet.connect(R.id.separator, ConstraintSet.TOP, txtTitle.getId(),
          ConstraintSet.BOTTOM);

      if (game != null) {
        txtTitle.setText(getContext().getString(R.string.ask_to_join_popup_title, displayName));
      } else {
        txtTitle.setText(
            getContext().getString(R.string.ask_to_join_popup_title_no_game, displayName));

        separator.setVisibility(View.GONE);
        txtViewScore.setVisibility(View.GONE);
        txtGame.setVisibility(View.GONE);

        constraintSet.connect(txtTitle.getId(), ConstraintSet.BOTTOM, R.id.separator2,
            ConstraintSet.TOP);
        constraintSet.connect(R.id.separator2, ConstraintSet.TOP, txtTitle.getId(),
            ConstraintSet.BOTTOM);
      }

      txtDesc.setVisibility(View.GONE);
      constraintSet.applyTo(layoutConstraint);
    }

    if (game != null) {
      if (user != null) score = user.getScoreForGame(game.getId());

      Glide.with(getContext())
          .load(game.getBackground())
          .bitmapTransform(new PositionedCropTransformation(getContext(), 1, 0.7f))
          .into(imgTop);
      gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          Color.parseColor("#" + game.getPrimary_color()),
          Color.parseColor("#" + game.getSecondary_color())
      });
    } else {
      gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          ContextCompat.getColor(getContext(), trophyEnum.getPrimaryColor()),
          ContextCompat.getColor(getContext(), trophyEnum.getSecondaryColor())
      });

      if (popupType.equals(INVITED_TO_JOIN)) {
        txtGame.setText(R.string.invited_to_join_popup_no_game_selected);
        txtGame.setGravity(Gravity.CENTER);
        txtViewScore.setVisibility(View.GONE);
      }
    }

    txtTrophy.setText(getContext().getString(R.string.ask_to_join_popup_level,
        getContext().getString(trophyEnum.getTitle())));
    new GlideUtils.TrophyImageBuilder(getContext(), screenUtils).drawableRes(trophyEnum.getIcon())
        .cardView(cardViewTrophy)
        .hasBorder(true)
        .target(imgIcon)
        .load();

    TextViewCompat.setTextAppearance(txtViewScore, R.style.Headline_Circular_2_Black);
    txtViewScore.setCustomFont(getContext(), FontUtils.CIRCULARSTD_BOLD);
    txtViewScore.setBackgroundResource(R.drawable.bg_pts_dark);
    txtViewScore.setGravity(Gravity.CENTER);
    txtViewScore.setPadding(screenUtils.dpToPx(10), screenUtils.dpToPx(5), screenUtils.dpToPx(10),
        screenUtils.dpToPx(5));
    txtViewScore.setScore(score != null ? score.getValue() : 0);

    int radius = screenUtils.dpToPx(8);
    float[] radiusMatrix = new float[] { 0, 0, 0, 0, 0, 0, radius, radius };
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.blue_new));
    gradientDrawable.setCornerRadii(radiusMatrix);
    btnPositive.setBackground(gradientDrawable);

    radiusMatrix = new float[] { 0, 0, 0, 0, radius, radius, 0, 0 };
    gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.grey_popup_digest));
    gradientDrawable.setCornerRadii(radiusMatrix);
    btnNegative.setBackground(gradientDrawable);

    radiusMatrix = new float[] { radius, radius, radius, radius, 0, 0, 0, 0 };
    gd.setCornerRadii(radiusMatrix);
    ViewCompat.setBackground(imgTop, gd);
  }

  /**
   * ON CLICK
   */

  @OnClick(R.id.btnPositive) void clickPositive() {
    if (popupListener != null) ((PopupAskToJoinListener) popupListener).accept();
  }

  @OnClick(R.id.btnNegative) void clickNegative() {
    if (popupListener != null) {
      if (popupType.equals(ASK_TO_JOIN)) {
        ((PopupAskToJoinListener) popupListener).decline();
      } else {
        ((PopupAskToJoinListener) popupListener).later();
      }
    }
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */

}
