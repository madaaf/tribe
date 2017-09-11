package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.FriendshipsLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 05/11/2017.
 */

public class SettingsManageFriendshipsView extends FrameLayout {

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  // VARIABLES
  private FriendshipsLayoutManager layoutManager;
  //private FriendshipsAdapter adapter;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  //private PublishSubject<Friendship> onClickMute = PublishSubject.create();
  private PublishSubject<Recipient> onClickRemove = PublishSubject.create();

  public SettingsManageFriendshipsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    init();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    initDependencyInjector();

    subscriptions = new CompositeSubscription();

    layoutManager = new FriendshipsLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    itemAnimator.setSupportsChangeAnimations(false);
    recyclerView.setItemAnimator(itemAnimator);

    //adapter = new FriendshipsAdapter(getContext());

    //recyclerView.setAdapter(adapter);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10), 0));

    //subscriptions.add(adapter.onClickMute()
    //    .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
    //    .flatMap(friendship -> {
    //      if (!friendship.isMute()) {
    //        return DialogFactory.dialog(getContext(),
    //            getContext().getString(R.string.manage_friendships_mute_alert_title),
    //            getContext().getString(R.string.manage_friendships_mute_alert_description),
    //            getContext().getString(R.string.manage_friendships_mute_alert_mute),
    //            getContext().getString(R.string.manage_friendships_mute_alert_cancel));
    //      } else {
    //        return Observable.just(true);
    //      }
    //    }, (friendship, proceed) -> Pair.create(friendship, proceed))
    //    .filter(pair -> {
    //      if (!pair.second) {
    //        pair = Pair.create(pair.first, false);
    //        adapter.reset(pair.first);
    //      }
    //
    //      return pair.second;
    //    })
    //    .subscribe(pair -> onClickMute.onNext(pair.first)));
    //
    //subscriptions.add(adapter.onClickRemove()
    //    .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
    //    .subscribe(onClickRemove));
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

  /////////////////
  //   PUBLIC    //
  /////////////////

  /**
   * OBSERVABLES
   */
}
