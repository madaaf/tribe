package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
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
  private GradientDrawable background;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<View> onClick = PublishSubject.create();

  public GameBattleMusicCategoryView(@NonNull Context context) {
    super(context);
  }

  public GameBattleMusicCategoryView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void init() {
    initDependencyInjector();
    initResources();
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

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(Color.WHITE);
        background.setCornerRadius(screenUtils.dpToPx(getMeasuredWidth() >> 1));
        setBackground(background);
      }
    });

    LayoutTransition transition = new LayoutTransition();
    transition.setDuration(200);
    setLayoutTransition(transition);

    setMinimumHeight(screenUtils.dpToPx(51));
    setClickable(true);
    setOnClickListener(v -> {
      UIUtils.changeLeftMarginOfView(txtName, 0);
      imgIcon.setVisibility(View.VISIBLE);
      onClick.onNext(this);
    });
  }

  private void initSubscriptions() {

  }

  /**
   * PUBLIC
   */

  public void setPlaylist(BattleMusicPlaylist playlist) {
    this.playlist = playlist;
    txtName.setText(playlist.getTitle());
    imgIcon.setVisibility(View.GONE);
    UIUtils.changeLeftMarginOfView(txtName, screenUtils.dpToPx(25));
  }

  public BattleMusicPlaylist getPlaylist() {
    return playlist;
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return onClick;
  }
}
