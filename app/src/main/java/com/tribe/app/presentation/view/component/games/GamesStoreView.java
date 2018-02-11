package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.GameAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.GamesLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class GamesStoreView extends FrameLayout {

  // VARIABLES


  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Game> onGameClick = PublishSubject.create();

  public GamesStoreView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();



  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }



  /**
   * OBSERVABLES
   */

  public Observable<Game> onGameClick() {
    return onGameClick;
  }
}
