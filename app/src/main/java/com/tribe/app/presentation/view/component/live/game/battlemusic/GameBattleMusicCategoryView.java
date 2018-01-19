package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */

public class GameBattleMusicCategoryView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Unbinder unbinder;
  private BattleMusicPlaylist playlist;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameBattleMusicCategoryView(@NonNull Context context) {
    super(context);
  }

  public GameBattleMusicCategoryView(@NonNull Context context, @Nullable AttributeSet attrs) {
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
  }

  private void initSubscriptions() {

  }

  /**
   * PUBLIC
   */

  public void setPlaylist(BattleMusicPlaylist playlist) {
    this.playlist = playlist;
    this.txtName.setText(playlist.getTitle());
  }

  public BattleMusicPlaylist getPlaylist() {
    return playlist;
  }

  /**
   * OBSERVABLES
   */

}
