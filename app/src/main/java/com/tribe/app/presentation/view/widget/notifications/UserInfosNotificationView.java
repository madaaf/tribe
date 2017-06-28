package com.tribe.app.presentation.view.widget.notifications;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 05/12/2017.
 */

public class UserInfosNotificationView extends FrameLayout {

  private final static int DURATION = 750;
  private final static float OVERSHOOT = 0.75f;

  @Inject ScreenUtils screenUtils;

  @Inject ContactAdapter contactAdapter;

  @BindView(R.id.recyclerViewContacts) RecyclerView recyclerViewContacts;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private ContactsLayoutManager layoutManager;
  private boolean animating = false;

  // OBSERVABLES
  Subscription timerSubscription;

  public UserInfosNotificationView(Context context) {
    super(context);
    initView(context, null);
  }

  public UserInfosNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    if (timerSubscription != null) {
      timerSubscription.unsubscribe();
      timerSubscription = null;
    }
    super.onDetachedFromWindow();
  }

  private void initView(Context context, AttributeSet attrs) {
    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_user_infos_notification, this, true);

    unbinder = ButterKnife.bind(this);

    layoutManager = new ContactsLayoutManager(getContext());
    layoutManager.setScrollEnabled(false);

    recyclerViewContacts.setLayoutManager(layoutManager);
    recyclerViewContacts.setItemAnimator(null);
    recyclerViewContacts.setAdapter(contactAdapter);

    contactAdapter.setItems(new ArrayList<>());
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void setTimer() {
    timerSubscription = Observable.timer(5, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> hideView());
  }

  ////////////
  // PUBLIC //
  ////////////

  public void hideView() {
    if (animating) return;

    animating = true;

    if (timerSubscription != null) timerSubscription.unsubscribe();

    animate().setDuration(DURATION)
        .translationY(-screenUtils.getHeightPx() >> 1)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            setVisibility(View.GONE);
            animating = false;
          }
        })
        .start();
  }

  public void displayView(Object obj) { //SOEF
    if (getVisibility() == View.VISIBLE || animating) return;

    animating = true;

    contactAdapter.clear();
    contactAdapter.addItem(obj);
    setTimer();

    setTranslationY(-screenUtils.getHeightPx() >> 1);
    animate().setDuration(DURATION)
        .translationY(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animation) {
            setVisibility(View.VISIBLE);
          }

          @Override public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            animating = false;
          }
        })
        .start();
  }

  public void update(Friendship friendship) {
    contactAdapter.updateAdd(friendship.getFriend());
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onInvite() {
    return contactAdapter.onClickInvite();
  }

  public Observable<Recipient> onClickMore() {
    return contactAdapter.onClickMore()
        .map(view -> ((Recipient) contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view))));
  }

  public Observable<String> onAdd() {
    return contactAdapter.onClickAdd()
        .map(view -> ((User) contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildLayoutPosition(view))).getId());
  }

  public Observable<Recipient> onUnblock() {
    return contactAdapter.onUnblock()
        .map(view -> ((Recipient) contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildAdapterPosition(view))));
  }

  public Observable<Recipient> onHangLive() {
    return contactAdapter.onHangLive()
        .map(view -> ((Recipient) contactAdapter.getItemAtPosition(
            recyclerViewContacts.getChildAdapterPosition(view))));
  }
}
