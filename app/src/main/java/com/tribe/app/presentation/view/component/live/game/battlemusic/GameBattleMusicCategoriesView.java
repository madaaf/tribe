package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameBattleMusicCategoriesView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindViews({
      R.id.viewCategoryFirst, R.id.viewCategorySecond, R.id.viewCategoryThird,
      R.id.viewCategoryFourth
  }) List<GameBattleMusicCategoryView> listCategoryViews;

  // VARIABLES
  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<BattleMusicPlaylist> onPlaylistSelected = PublishSubject.create();

  public GameBattleMusicCategoriesView(@NonNull Context context) {
    super(context);
  }

  public GameBattleMusicCategoriesView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
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
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_battlemusic_category, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
  }

  private void initSubscriptions() {

  }

  /**
   * ON CLICK
   */

  @OnClick({
      R.id.viewCategoryFirst, R.id.viewCategorySecond, R.id.viewCategoryThird,
      R.id.viewCategoryFourth
  }) void onCategorySelect(GameBattleMusicCategoryView categoryView) {
    onPlaylistSelected.onNext(categoryView.getPlaylist());
  }

  /**
   * PUBLIC
   */

  public void computeCategories(List<BattleMusicPlaylist> playlists) {
    for (int i = 0; i < playlists.size(); i++) {
      BattleMusicPlaylist playlist = playlists.get(i);
      listCategoryViews.get(i).setPlaylist(playlist);
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<BattleMusicPlaylist> onPlaylistSelected() {
    return onPlaylistSelected;
  }
}
