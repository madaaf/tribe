package com.tribe.app.presentation.view.popup.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.adapter.HomeListAdapter;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.popup.listener.PopupDigestListener;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/26/2018
 */

public class PopupDigest extends PopupView {

  @Inject ScreenUtils screenUtils;

  @Inject HomeListAdapter adapter;

  @Inject Navigator navigator;

  @BindView(R.id.layoutTop) LinearLayout layoutTop;
  @BindView(R.id.layoutBottom) LinearLayout layoutBottom;
  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  // VARIABLES
  private HomeLayoutManager layoutManager;
  private List<HomeAdapterInterface> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PopupDigest(@NonNull Context context) {
    super(context);
  }

  public PopupDigest(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    items = new ArrayList<>();

    initDependencyInjector();
    initSubscriptions();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    int radius = screenUtils.dpToPx(5);

    float[] radiusMatrix = new float[] { radius, radius, radius, radius, 0, 0, 0, 0 };
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setCornerRadii(radiusMatrix);
    gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.red));
    layoutTop.setBackground(gradientDrawable);

    radiusMatrix = new float[] { 0, 0, 0, 0, radius, radius, radius, radius };
    gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setCornerRadii(radiusMatrix);
    gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.grey_popup_digest));
    layoutBottom.setBackground(gradientDrawable);

    layoutManager = new HomeLayoutManager(getContext());
    layoutManager.setAutoMeasureEnabled(false);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    adapter.setHasChat(false);
    adapter.setItems(new ArrayList<>());
    recyclerView.setAdapter(adapter);

    subscriptions.add(adapter.onLiveClick()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
        .debounce(100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(recipient -> {
          if (popupListener != null) {
            ((PopupDigestListener) popupListener).onClick((Recipient) recipient);
            onDone.onNext(null);
          }
        }));
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

  /**
   * ON CLICK
   */

  @OnClick(R.id.layoutBottom) void onClickMore() {
    if (popupListener != null) {
      ((PopupDigestListener) popupListener).onClickMore();
      onDone.onNext(null);
    }
  }

  /**
   * PUBLIC
   */

  public void setItems(List<HomeAdapterInterface> items) {
    this.items.clear();
    this.items.addAll(items);
    this.adapter.setItems(this.items);
  }

  /**
   * OBSERVABLES
   */
}
