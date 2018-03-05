package com.tribe.app.presentation.view.popup.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import butterknife.ButterKnife;
import com.tribe.app.R;
import java.util.ArrayList;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/26/2018
 */

public class PopupTrophy extends PopupView {

  //@BindView(R.id.)

  // VARIABLES
  private String title;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PopupTrophy(Builder builder) {
    super(builder.context);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_game_user_card, this);
    ButterKnife.bind(this);

    items = new ArrayList<>();

    initSubscriptions();
    initUI();
  }

  public static class Builder {

    private Context context;
    private GradientDrawable bg;
    private GradientDrawable smallBg;
    private String title;
    private int icon;
    private String description;
    private boolean achieved;

    public Builder(Context context) {
      this.context = context;
    }

    public Builder bg(int firstColor, int secondColor) {
      this.bg = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          ColorUtils.setAlphaComponent(ContextCompat.getColor(context, firstColor),
              (int) 0.8f * 255), ContextCompat.getColor(context, secondColor)
      });

      this.smallBg = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
          ContextCompat.getColor(context, firstColor), ContextCompat.getColor(context, secondColor)
      });

      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder icon(int icon) {
      this.icon = icon;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
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

  }

  /**
   * ON CLICK
   */

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */

}
