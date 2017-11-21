package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.adapter.GameAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.NewChatLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

public class NewGameActivity extends BaseActivity {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, NewGameActivity.class);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject GameAdapter gameAdapter;

  @BindView(R.id.recyclerViewGame) RecyclerView recyclerViewGame;

  // VARIABLES
  private Unbinder unbinder;
  private NewChatLayoutManager layoutManager;
  private List<Game> items;
  private GameManager gameManager;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init(savedInstanceState);
    initUI();
    initRecyclerView();
  }

  @Override protected void onDestroy() {

    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

    super.onDestroy();
  }

  private void init(Bundle savedInstanceState) {
    items = new ArrayList<>();
    gameManager = GameManager.getInstance(this);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initUI() {

  }

  private void initRecyclerView() {
    layoutManager = new NewChatLayoutManager(this);
    recyclerViewGame.setLayoutManager(layoutManager);
    recyclerViewGame.setItemAnimator(null);
    recyclerViewGame.addItemDecoration(
        new DividerHeadersItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10)));
    recyclerViewGame.setAdapter(gameAdapter);

    items.addAll()
    gameAdapter.setItems(items);

    subscriptions.add(gameAdapter.onClick()
        .map(view -> gameAdapter.getItemAtPosition(recyclerViewGame.getChildLayoutPosition(view)))
        .subscribe());
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }
}
