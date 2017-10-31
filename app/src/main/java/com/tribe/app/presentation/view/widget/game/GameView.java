package com.tribe.app.presentation.view.widget.game;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameView extends FrameLayout {

  @Inject User user;
  @Inject ScreenUtils screenUtils;

  protected LayoutInflater inflater;
  protected Unbinder unbinder;
  protected Context context;
  protected GameManager gameManager;
  protected WebRTCRoom webRTCRoom;
  protected Game game;
  protected Map<String, TribeGuest> peerList;

  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected CompositeSubscription subscriptionsRoom = new CompositeSubscription();

  public GameView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    gameManager = GameManager.getInstance(context);

    peerList = new HashMap<>();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  /**
   * PRIVATE
   */

  protected abstract void initWebRTCRoomSubscriptions();

  public abstract void setNextGame();

  /**
   * PUBLIC
   */

  public void setGame(Game game) {
    this.game = game;
  }

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;

    initWebRTCRoomSubscriptions();
  }

  public void dispose() {
    subscriptionsRoom.clear();
    subscriptions.clear();
  }

  /**
   * OBSERVABLE
   */
}
