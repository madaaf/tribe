package com.tribe.app.presentation.view.component.trophies;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.domain.entity.TrophyRequirement;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */
public class TrophyRequirementView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutReq) LinearLayout layoutReq;

  @BindView(R.id.txtReqDone) TextViewFont txtReqDone;

  @BindView(R.id.txtReqTotal) TextViewFont txtReqTotal;

  @BindView(R.id.txtRequirement) TextViewFont txtRequirement;

  // VARIABLES
  private TrophyEnum trophyEnum;
  private TrophyRequirement trophyRequirement;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TrophyRequirementView(Context context) {
    super(context);
  }

  public TrophyRequirementView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_trophy_requirement, this);
    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);

    initResources();
    initSubscriptions();
  }

  public void dispose() {
    subscriptions.clear();
  }

  private void initUI() {
    GradientDrawable border = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
        ContextCompat.getColor(getContext(), trophyEnum.getPrimaryColor()),
        ContextCompat.getColor(getContext(), trophyEnum.getPrimaryColor())
    });

    int radius = screenUtils.dpToPx(5);
    border.setCornerRadius(radius);

    int marginDrawable = screenUtils.dpToPx(2);

    GradientDrawable fill = new GradientDrawable();
    fill.setColor(Color.WHITE);
    fill.setCornerRadius(screenUtils.dpToPx(3));

    GradientDrawable[] layers = { border, fill };
    LayerDrawable layerDrawable = new LayerDrawable(layers);

    layerDrawable.setLayerInset(0, 0, 0, 0, 0);
    layerDrawable.setLayerInset(1, marginDrawable, marginDrawable, marginDrawable, marginDrawable);

    layoutReq.setBackground(layerDrawable);

    txtReqDone.setText("" +
        ((trophyEnum.isUnlockedByUser()) ? trophyRequirement.totalCount()
            : trophyRequirement.achievedCount()));
    txtReqTotal.setText(" / " + trophyRequirement.totalCount());
    txtRequirement.setText(trophyRequirement.title());
  }

  private void initResources() {

  }

  private void initSubscriptions() {

  }

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void setRequirement(@NonNull TrophyEnum trophy,
      @NonNull TrophyRequirement trophyRequirement) {
    this.trophyEnum = trophy;
    this.trophyRequirement = trophyRequirement;
    initUI();
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}