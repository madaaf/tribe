package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveStatusNameView extends FrameLayout {

  private static final int DURATION = 300;
  private static final float OVERSHOOT = 0.90f;

  public static final int INITIATING = R.string.live_waiting_state_initiating;
  public static final int NOTIFYING = R.string.live_waiting_state_notifying;
  public static final int WAITING = R.string.live_waiting_state_waiting;
  public static final int DONE = -1;

  @IntDef({ INITIATING, NOTIFYING, WAITING, DONE }) public @interface StatusType {
  }

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtStatus1) TextViewFont txtStatus1;

  // VARIABLES
  private Unbinder unbinder;
  private Live live;
  private @StatusType int status;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveStatusNameView(Context context) {
    super(context);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_status_name, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
  }

  private void initResources() {
    translationY = screenUtils.dpToPx(20);
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

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    txtStatus1.clearAnimation();
  }

  public void setLive(Live live) {
    this.live = live;

    if (!StringUtils.isEmpty(live.getLinkId())) {
      if (live.getId().equals(Live.WEB)) {
        txtName.setText(
            getContext().getString(R.string.live_title_with_guests, user.getDisplayName()));
      } else if (live.getId().equals(Live.NEW_CALL)) {
        txtName.setText(getContext().getString(R.string.live_new_call_title_alone));
      }

      txtStatus1.setVisibility(View.GONE);
    } else {
      if (live.isGroup()) {
        txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small_shadow, 0, 0,
            0);
      } else {
        txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
      }

      txtName.setText(live.getDisplayName());

      setStatus(INITIATING);
    }
  }

  public void refactorTitle() {
    if (!live.getId().equals(Live.NEW_CALL)) return;
    txtName.setText(getContext().getString(R.string.live_title_with_guests, user.getDisplayName()));
  }

  public void setStatus(@StatusType int status) {
    if (this.status == DONE || !StringUtils.isEmpty(live.getLinkId())) return;

    this.status = status;

    if (status == DONE) {
      txtStatus1.setVisibility(View.GONE);
      return;
    }

    txtStatus1.setText(
        EmojiParser.demojizedText(getContext().getString(status, live.getDisplayName())));
  }

  public @LiveStatusNameView.StatusType int getStatus() {
    return status;
  }
}
