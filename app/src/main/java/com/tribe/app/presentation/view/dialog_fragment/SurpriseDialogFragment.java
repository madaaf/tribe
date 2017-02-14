package com.tribe.app.presentation.view.dialog_fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/14/16.
 */
public class SurpriseDialogFragment extends BaseDialogFragment {

  private static final int WIDTH_MAX = 300;

  public static SurpriseDialogFragment newInstance() {
    Bundle args = new Bundle();
    SurpriseDialogFragment fragment = new SurpriseDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.textTitle) TextViewFont textTitle;

  @BindView(R.id.textSummary) TextViewFont textSummary;

  @BindView(R.id.textConfirm) TextViewFont textConfirm;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> confirmClicked = PublishSubject.create();
  private PublishSubject<Void> onDismiss = PublishSubject.create();

  public Observable<Void> confirmClicked() {
    return confirmClicked;
  }

  public Observable<Void> onDismiss() {
    return onDismiss;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View fragmentView = inflater.inflate(R.layout.dialog_fragment_surprise, container, false);
    initDependencyInjector();
    initUi(fragmentView);
    return fragmentView;
  }

  @Override public void onStart() {
    super.onStart();

    if (getDialog() == null) return;

    int widthMax = screenUtils.dpToPx(WIDTH_MAX);
    int dialogWidth = screenUtils.getWidthPx() - (screenUtils.dpToPx(15) * 2);
    dialogWidth = Math.min(widthMax, dialogWidth);
    int dialogHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

    getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
  }

  @Override public void initUi(View view) {
    super.initUi(view);
    getDialog().getWindow()
        .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    textTitle.setText(getString(R.string.onboarding_new_version_popup_title));
    textSummary.setText(getString(R.string.onboarding_new_version_popup_message));
    textConfirm.setText(getString(R.string.onboarding_popup_confirm_send_button_title));

    subscriptions.add(RxView.clicks(textConfirm).subscribe(aVoid -> {
      confirmClicked.onNext(null);
      dismiss();
    }));
  }

  @Override public void removeSubscriptions() {
    super.removeSubscriptions();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
