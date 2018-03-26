package com.tribe.app.presentation.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 26/03/2018.
 */

public class Test extends FrameLayout {

  private Game game;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.imgBackgroundGradient) View imgBackgroundGradient;
  @BindView(R.id.imgBackgroundLogo) ImageView imgBackgroundLogo;
  @BindView(R.id.imgIcon) ImageView imgIcon;
  @BindView(R.id.imgLogo) ImageView imgLogo;
  @BindView(R.id.txtBaseline) TextViewFont txtBaseline;
  @BindView(R.id.imgRays) ImageView imgRays;
  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;
  @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
  @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
  @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
  @BindView(R.id.cardAvatarMyScore) CardView cardAvatarMyScore;
  @BindView(R.id.avatarMyScore) NewAvatarView avatarMyScore;
  @BindView(R.id.txtMyScoreRanking) TextViewRanking txtMyScoreRanking;
  @BindView(R.id.txtMyScoreScore) TextViewScore txtMyScoreScore;
  @BindView(R.id.txtMyScoreName) TextViewFont txtMyScoreName;
  @BindView(R.id.btnMulti) View btnMulti;
  @BindView(R.id.leaderbordContainer) View leaderbordContainer;
  @BindView(R.id.leaderbordLabel) TextViewFont leaderbordLabel;
  @BindView(R.id.leaderbordPictoStart) ImageView leaderbordPictoStart;
  @BindView(R.id.leaderbordPictoEnd) ImageView leaderbordPictoEnd;
  @BindView(R.id.leaderbordSeparator) View leaderbordSeparator;

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private Context context;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public Test(@NonNull Context context, Game game) {
    super(context);
    this.context = context;
    this.game = game;
    init();
  }

  public Test(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    LayoutInflater.from(getContext()).inflate(R.layout.activity_game_details, this);
    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    initUI();
  }

  private void initUI() {
    txtBaseline.setText(game.getBaseline());

    new GlideUtils.GameImageBuilder(context, screenUtils).url(game.getIcon())
        .hasBorder(false)
        .hasPlaceholder(true)
        .rounded(true)
        .target(imgIcon)
        .load();

    Glide.with(context).load(game.getLogo()).into(imgLogo);
    Glide.with(context).load(game.getBackground()).into(imgBackgroundLogo);

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });

    ViewCompat.setBackground(imgBackgroundGradient, gd);

    for (int i = 0; i < game.getAnimation_icons().size(); i++) {
      String url = game.getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = imgAnimation1;
      } else if (i == 1) {
        imageView = imgAnimation2;
      } else if (i == 2) {
        imageView = imgAnimation3;
      }

      Glide.with(context).load(url).into(imageView);
    }
/*
    animateImg(imgAnimation1, true);
    animateImg(imgAnimation1, false);
    animateImg(imgAnimation2, true);
    animateImg(imgAnimation2, false);
    animateImg(imgAnimation3, true);
    animateImg(imgAnimation3, false);*/

    Score score = user.getScoreForGame(game.getId());
    if (score == null) {
      score = new Score();
      score.setValue(0);
    }

    txtMyScoreName.setText(user.getDisplayName());
    txtMyScoreScore.setScore(score.getValue());
    txtMyScoreRanking.setRanking(score.getRanking());
    avatarMyScore.load(user.getProfilePicture());
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
