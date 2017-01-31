package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveRowView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewWaiting) LiveWaitingView viewWaiting;

  // VARIABLES
  private Unbinder unbinder;
  private Recipient recipient;
  private int color;

  public LiveRowView(Context context) {
    super(context);
    init();
  }

  public LiveRowView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveRowView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    LayoutInflater.from(getContext()).inflate(R.layout.view_row_live, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    if (recipient != null) viewWaiting.setRecipient(recipient);
    viewWaiting.setColor(color);
  }

  public void setColor(int color) {
    this.color = color;
    if (viewWaiting != null) viewWaiting.setColor(color);
  }

  public void setRecipient(Recipient recipient) {
    this.recipient = recipient;
    if (viewWaiting != null) viewWaiting.setRecipient(recipient);
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    if (viewWaiting != null) viewWaiting.setRoomType(type);
  }

  public void startPulse() {
    if (viewWaiting != null) viewWaiting.startPulse();
  }
}
