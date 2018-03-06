package com.tribe.app.presentation.view.popup.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.presentation.view.component.trophies.TrophyRequirementView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/26/2018
 */

public class PopupTrophy extends PopupView {

  @BindView(R.id.layoutTop) View layoutTop;

  @BindView(R.id.layoutDesc) View layoutDesc;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtDesc) TextViewFont txtDesc;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.cardViewTrophy) CardView cardViewTrophy;

  @BindView(R.id.viewRequirementFriends) TrophyRequirementView viewRequirementFriends;

  @BindView(R.id.viewRequirementDays) TrophyRequirementView viewRequirementDays;

  @BindView(R.id.viewRequirementGames) TrophyRequirementView viewRequirementGames;

  // VARIABLES
  private GradientDrawable bg, smallBG;
  private TrophyEnum trophyEnum;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PopupTrophy(Builder builder) {
    super(builder.context);
    this.bg = builder.bg;
    this.smallBG = builder.smallBg;
    this.trophyEnum = builder.trophyEnum;

    LayoutInflater.from(getContext()).inflate(R.layout.view_popup_trophy, this, true);
    ButterKnife.bind(this);

    initSubscriptions();
    initUI();
  }

  public static class Builder {

    private Context context;
    private GradientDrawable bg, smallBg;
    private boolean achieved;
    private TrophyEnum trophyEnum;

    public Builder(Context context, TrophyEnum trophyEnum) {
      this.context = context;
      this.trophyEnum = trophyEnum;
    }

    public Builder bg(int radius) {
      this.bg = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          ColorUtils.setAlphaComponent(
              ContextCompat.getColor(context, trophyEnum.getPrimaryColor()), (int) (0.8f * 255)),
          ContextCompat.getColor(context, trophyEnum.getSecondaryColor())
      });

      this.smallBg = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          ContextCompat.getColor(context, trophyEnum.getPrimaryColor()),
          ContextCompat.getColor(context, trophyEnum.getSecondaryColor())
      });

      float[] radiusMatrix = new float[] { radius, radius, radius, radius, 0, 0, 0, 0 };
      this.smallBg.setCornerRadii(radiusMatrix);

      return this;
    }

    public Builder achieved(boolean achieved) {
      this.achieved = achieved;
      return this;
    }

    public PopupTrophy build() {
      return new PopupTrophy(this);
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
    txtTitle.setText(trophyEnum.getTitle());
    txtDesc.setText(getContext().getString(R.string.trophy_requirement_friends_description, 3) +
        ", " +
        getContext().getString(R.string.trophy_requirement_day_usage_description, 3) +
        ", " +
        getContext().getString(R.string.trophy_requirement_games_played_description, 3));
    viewRequirementFriends.setRequirement(trophyEnum, null);
    viewRequirementDays.setRequirement(trophyEnum, null);
    viewRequirementGames.setRequirement(trophyEnum, null);

    layoutTop.setBackground(smallBG);

    cardViewTrophy.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            cardViewTrophy.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            cardViewTrophy.setRadius(((float) cardViewTrophy.getMeasuredWidth() / (float) 4) /
                (float) 1.61803398874989484820);
          }
        });

    Glide.with(getContext()).load(trophyEnum.getIcon()).into(imgIcon);
  }

  /**
   * ON CLICK
   */

  /**
   * PUBLIC
   */
  public GradientDrawable getBg() {
    return bg;
  }

  /**
   * OBSERVABLES
   */

}
