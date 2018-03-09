package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveRowView;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.corona.GameCoronaView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
import java.util.Map;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class CoronaGameActivity extends BaseActivity {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, CoronaGameActivity.class);
    return intent;
  }

  @BindView(R.id.viewRoot) FrameLayout viewRoot;

  // VARIABLES
  private Unbinder unbinder;
  private GameManager gameManager;
  private GameCoronaView gameCoronaView;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private ObservableRxHashMap<String, TribeGuest> masterMap = new ObservableRxHashMap();
  private BehaviorSubject<Map<String, TribeGuest>> mapObservable = BehaviorSubject.create();
  private BehaviorSubject<Map<String, TribeGuest>> mapInvitedObservable = BehaviorSubject.create();
  private BehaviorSubject<Map<String, LiveStreamView>> mapViewsObservable =
      BehaviorSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_corona);

    unbinder = ButterKnife.bind(this);

    gameManager = GameManager.getInstance(this);
    gameCoronaView = new GameCoronaView(this, gameManager.getGameById(Game.GAME_INVADERS_CORONA));
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    viewRoot.addView(gameCoronaView, params);

    initDependencyInjector();
    init();
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void init() {
    TribeGuest tribeGuest = getCurrentUser().asTribeGuest();

    masterMap.put(tribeGuest.getId(), tribeGuest);
    mapObservable.onNext(masterMap.getMap());
    Map<String, LiveStreamView> mapLiveStream = new HashMap<>();
    mapLiveStream.put(tribeGuest.getId(), new LiveRowView(this));
    mapViewsObservable.onNext(mapLiveStream);

    gameCoronaView.start(gameManager.getGameById(Game.GAME_INVADERS_CORONA),
        masterMap.getObservable(), mapObservable, mapInvitedObservable, mapViewsObservable,
        getCurrentUser().getId());
  }

  private void initDependencyInjector() {

  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }
}