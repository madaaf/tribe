package com.tribe.app.presentation.view.component.games;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */
public class GameUserCardView extends ConstraintLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;

  @BindView(R.id.txtRanking) TextViewFont txtRanking;

  @BindView(R.id.txtDisplayName) TextViewFont txtDisplayName;

  @BindView(R.id.txtPoints) TextViewFont txtPoints;

  // VARIABLES
  private Score score;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameUserCardView(Context context) {
    super(context);
  }

  public GameUserCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_game_user_card, this);
    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initResources();
    initUI();
    initSubscriptions();
  }

  public void dispose() {
    subscriptions.clear();
  }

  private void initUI() {

  }

  private void initResources() {

  }

  private void initSubscriptions() {

  }

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void setScore(Score score) {
    this.score = score;
    viewNewAvatar.load(score.getUser().getProfilePicture());
    txtDisplayName.setText(score.getUser().getDisplayName());
    txtPoints.setText("" + score.getValue());
    txtRanking.setText(score.getRanking());
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}