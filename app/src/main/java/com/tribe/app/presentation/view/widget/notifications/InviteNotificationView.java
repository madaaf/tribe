package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by tiago on 04/23/2017.
 */

public class InviteNotificationView extends LifeNotification {

  @BindView(R.id.btnAction1) TextViewFont btnAction1;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  public InviteNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public InviteNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  ///////////////////
  //    ON CLICK   //
  ///////////////////

  @OnClick(R.id.btnAction1) void onClickAction1() {
    // TODO tiago
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_invite_notification, this, true);

    unbinder = ButterKnife.bind(this);
  }
}
