package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;

/**
 * Created by tiago on 09/08/2017.
 */
public class LiveViewFake extends FrameLayout {

  @BindView(R.id.viewControlsLive) LiveControlsView viewControlsLive;

  // VARIABLES
  private Live live;
  private Recipient recipient;

  // RESOURCES

  // OBSERVABLES
  private Unbinder unbinder;

  public LiveViewFake(Context context) {
    super(context);
    init();
  }

  public LiveViewFake(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_live_fake, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {

  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);
  }

  private void initResources() {

  }

  private void initSubscriptions() {

  }

  public void setLive(Live live, Recipient recipient) {
    if (this.recipient != null && this.recipient.equals(recipient)) return;
    this.recipient = recipient;
    this.live = live;
    viewControlsLive.setLive(this.live);
  }
}

