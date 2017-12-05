package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 03/29/2016.
 */

public class SettingsBlockedFriendsView extends FrameLayout {

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  // VARIABLES
  private ContactsLayoutManager layoutManager;
  private ContactAdapter adapter;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Pair> clickUnblock = PublishSubject.create();
  private PublishSubject<Recipient> clickHangLive = PublishSubject.create();

  public SettingsBlockedFriendsView(Context context, AttributeSet attrs) {
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

    layoutManager = new ContactsLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);

    adapter = new ContactAdapter(getContext());

    recyclerView.setAdapter(adapter);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(15), screenUtils.dpToPx(10), 0));

    subscriptions.add(adapter.onUnblock()
        .map(view -> {
          int position = recyclerView.getChildLayoutPosition(view.itemView);
          Recipient recipient = (Recipient) adapter.getItemAtPosition(position);
          return new Pair<>(position, recipient);
        })
        .doOnError(throwable -> throwable.printStackTrace())
        .flatMap(pairPositionRecipient -> DialogFactory.dialog(getContext(),
            pairPositionRecipient.second.getDisplayName(),
            getContext().getString(R.string.search_unblock_alert_message),
            getContext().getString(R.string.search_unblock_alert_unblock,
                pairPositionRecipient.second.getDisplayName()),
            getContext().getString(R.string.search_unblock_alert_cancel)),
            (pairPositionRecipient, aBoolean) -> new Pair<>(pairPositionRecipient, aBoolean))
        .filter(pair -> pair.second == true)
        .subscribe(pair -> {
          BaseListViewHolder v =
              (BaseListViewHolder) recyclerView.findViewHolderForAdapterPosition(pair.first.first);
          v.progressView.setVisibility(VISIBLE);

          Shortcut shortcut = (Shortcut) pair.first.second;
          Pair p = new Pair<>(pair.first.second, v);
          clickUnblock.onNext(p);
          shortcut.setStatus(ShortcutRealm.DEFAULT);
          shortcut.setAnimateAdd(true);
          adapter.notifyItemChanged(pair.first.first);
        }));

    subscriptions.add(adapter.onHangLive()
        .map(view -> (Recipient) adapter.getItemAtPosition(
            recyclerView.getChildLayoutPosition(view.itemView)))
        .subscribe(clickHangLive));
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

  public void renderBlockedShortcutList(List<Shortcut> shortcutList) {
    adapter.setItems(new ArrayList<>(shortcutList));
  }

  /**
   * OBSERVABLES
   */

  public Observable<Pair> onUnblock() {
    return clickUnblock;
  }

  public Observable<Recipient> onHangLive() {
    return clickHangLive;
  }
}
