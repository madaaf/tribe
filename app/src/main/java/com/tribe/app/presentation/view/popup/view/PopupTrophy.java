package com.tribe.app.presentation.view.popup.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.domain.entity.TrophyRequirement;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.preferences.SelectedTrophy;
import com.tribe.app.presentation.view.component.trophies.TrophyRequirementView;
import com.tribe.app.presentation.view.popup.listener.PopupTrophyListener;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/26/2018
 */

public class PopupTrophy extends PopupView {

  @Inject @SelectedTrophy Preference<String> selectedTrophy;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;

  @BindView(R.id.layoutTop) View layoutTop;

  @BindView(R.id.layoutDesc) View layoutDesc;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtDesc) TextViewFont txtDesc;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.cardViewTrophy) CardView cardViewTrophy;

  @BindView(R.id.viewRequirementFirst) TrophyRequirementView viewRequirementFirst;

  @BindView(R.id.viewRequirementSecond) TrophyRequirementView viewRequirementSecond;

  @BindView(R.id.viewRequirementThird) TrophyRequirementView viewRequirementThird;

  @BindView(R.id.btnUseIcon) TextViewFont btnUseIcon;

  @BindView(R.id.btnCloseIcon) TextViewFont btnCloseIcon;

  @BindView(R.id.btnCloseGroup) Group btnCloseGroup;

  @BindView(R.id.txtAlreadyInUse) TextViewFont txtAlreadyInUse;

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
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initSubscriptions();
    initUI();
  }

  public static class Builder {

    private Context context;
    private GradientDrawable bg, smallBg;
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
    int radius = screenUtils.dpToPx(5);
    float[] radiusMatrix = new float[] { 0, 0, 0, 0, radius, radius, radius, radius };
    GradientDrawable background =
        new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {
            ContextCompat.getColor(getContext(), R.color.trophy_use_primary),
            ContextCompat.getColor(getContext(), R.color.trophy_use_secondary)
        });
    background.setCornerRadii(radiusMatrix);
    btnUseIcon.setBackground(background);

    background = new GradientDrawable();
    background.setColor(ContextCompat.getColor(getContext(), R.color.grey_popup_digest));
    background.setCornerRadii(radiusMatrix);
    btnCloseIcon.setBackground(background);

    //if (!selectedTrophy.get().equals(trophyEnum.getTrophy())) {
    //  if (trophyEnum.isAchieved()) {
    //    btnCloseGroup.setVisibility(View.GONE);
    //    btnUseIcon.setVisibility(View.VISIBLE);
    //  } else {
    //    btnCloseGroup.setVisibility(View.VISIBLE);
    //    btnUseIcon.setVisibility(View.GONE);
    //  }
    //} else {
    //  btnCloseGroup.setVisibility(View.GONE);
    //  btnUseIcon.setVisibility(View.GONE);
    //  txtAlreadyInUse.setVisibility(View.VISIBLE);
    //  ConstraintSet constraintSet = new ConstraintSet();
    //  constraintSet.clone(layoutConstraint);
    //  constraintSet.clear(layoutDesc.getId(), ConstraintSet.BOTTOM);
    //  constraintSet.clear(viewRequirementFirst.getId(), ConstraintSet.BOTTOM);
    //  constraintSet.applyTo(layoutConstraint);
    //}

    txtTitle.setText(trophyEnum.getTitle());

    String description = "";
    int count = 0;

    if (trophyEnum.getRequirements() != null && trophyEnum.getRequirements().size() >= 3) {
      for (TrophyRequirement trophyRequirement : trophyEnum.getRequirements()) {
        if (count > 0) description += ", ";
        description +=
            getContext().getString(trophyRequirement.description(), trophyRequirement.totalCount());
        count++;
      }
      txtDesc.setText(description);

      viewRequirementFirst.setRequirement(trophyEnum, trophyEnum.getRequirements().get(0));
      viewRequirementSecond.setRequirement(trophyEnum, trophyEnum.getRequirements().get(1));
      viewRequirementThird.setRequirement(trophyEnum, trophyEnum.getRequirements().get(2));

      UIUtils.changeHeightOfView(layoutDesc, screenUtils.dpToPx(275));
    } else {
      txtDesc.setText(R.string.trophy_noob_description);

      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(layoutConstraint);
      constraintSet.clear(layoutDesc.getId(), ConstraintSet.BOTTOM);
      constraintSet.connect(layoutDesc.getId(), ConstraintSet.BOTTOM, btnUseIcon.getId(),
          ConstraintSet.TOP);
      constraintSet.connect(btnUseIcon.getId(), ConstraintSet.TOP, layoutDesc.getId(),
          ConstraintSet.BOTTOM);
      constraintSet.connect(txtDesc.getId(), ConstraintSet.BOTTOM, layoutDesc.getId(),
          ConstraintSet.BOTTOM);
      constraintSet.setVisibility(R.id.viewRequirementFirst, View.GONE);
      constraintSet.setVisibility(R.id.viewRequirementSecond, View.GONE);
      constraintSet.setVisibility(R.id.viewRequirementThird, View.GONE);
      constraintSet.setVisibility(R.id.separator, View.GONE);
      constraintSet.applyTo(layoutConstraint);

      UIUtils.changeHeightOfView(layoutDesc, screenUtils.dpToPx(165));
    }

    layoutTop.setBackground(smallBG);

    new GlideUtils.TrophyImageBuilder(getContext(), screenUtils).drawableRes(trophyEnum.getIcon())
        .cardView(cardViewTrophy)
        .target(imgIcon)
        .load();
  }

  /**
   * ON CLICK
   */

  @OnClick(R.id.btnUseIcon) void useTrophy() {
    if (popupListener != null) {
      ((PopupTrophyListener) popupListener).onClick(trophyEnum);
      onDone.onNext(null);
    }
  }

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
