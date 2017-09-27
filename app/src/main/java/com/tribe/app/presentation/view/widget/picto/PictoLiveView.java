package com.tribe.app.presentation.view.widget.picto;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.widget.PulseLayout;

/**
 * Created by tiago on 09/05/17.
 */
public class PictoLiveView extends FrameLayout {

  @IntDef({ ACTIVE, INACTIVE }) public @interface Status {
  }

  public static final int ACTIVE = 0;
  public static final int INACTIVE = 1;

  @BindView(R.id.imgLive) ImageView imgLive;
  @BindView(R.id.layoutPulse) PulseLayout layoutPulse;

  private int status;
  private Unbinder unbinder;

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
    setStatus(status);
  }

  public void setStatus(@Status int status) {
    if (this.status == status) return;

    this.status = status;
    if (status == INACTIVE) {
      imgLive.setImageResource(R.drawable.picto_live_inactive);
      layoutPulse.stop();
    } else {
      imgLive.setImageResource(R.drawable.picto_live_active);
      //layoutPulse.start();
    }
  }
}
