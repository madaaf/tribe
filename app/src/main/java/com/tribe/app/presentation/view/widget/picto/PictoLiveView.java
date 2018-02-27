package com.tribe.app.presentation.view.widget.picto;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;

/**
 * Created by tiago on 09/05/17.
 */
public class PictoLiveView extends FrameLayout {

  @IntDef({ ACTIVE, PLAYING, INACTIVE }) public @interface Status {
  }

  public static final int ACTIVE = 0;
  public static final int PLAYING = 1;
  public static final int INACTIVE = 2;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.imgLive) ImageView imgLive;

  private int status;
  private Unbinder unbinder;
  private GradientDrawable gradientDrawable;

  public PictoLiveView(Context context) {
    super(context);
    init();
  }

  public PictoLiveView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PictoLiveView);
    status = a.getInt(R.styleable.PictoLiveView_liveStatus, INACTIVE);
    a.recycle();

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_picto_live, this);
    unbinder = ButterKnife.bind(this);

    TextViewCompat.setTextAppearance(txtAction, R.style.Body_Two_White);
    txtAction.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setCornerRadius(screenUtils.dpToPx(4));
    txtAction.setBackground(gradientDrawable);

    setStatus(status);
  }

  public void setStatus(@Status int status) {
    this.status = status;

    if (status == INACTIVE) {
      imgLive.setVisibility(View.VISIBLE);
      txtAction.setVisibility(View.GONE);
      return;
    }

    imgLive.setVisibility(View.GONE);
    txtAction.setVisibility(View.VISIBLE);

    if (status == PLAYING) {
      txtAction.setText(R.string.home_action_join);
      gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.blue_new));
    } else {
      txtAction.setText(R.string.home_action_ask_join);
      gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.red));
    }
  }
}
