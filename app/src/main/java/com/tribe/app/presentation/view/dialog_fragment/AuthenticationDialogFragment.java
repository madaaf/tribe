package com.tribe.app.presentation.view.dialog_fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.CodeSentToView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 10/18/16.
 */
public class AuthenticationDialogFragment extends BaseDialogFragment {

  private static final int WIDTH_MAX = 300;

  public static AuthenticationDialogFragment newInstance(String phoneNumber, boolean resend) {
    Bundle args = new Bundle();
    args.putString(PHONE_NUMBER, phoneNumber);
    args.putBoolean(RESEND, resend);
    AuthenticationDialogFragment fragment = new AuthenticationDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutParent) LinearLayout layoutParent;

  @BindView(R.id.layoutBottom) LinearLayout layoutBottom;

  @BindView(R.id.textTitle) TextViewFont textTitle;

  @BindView(R.id.textSummary) TextViewFont textSummary;

  @BindView(R.id.textCancel) TextViewFont textCancel;

  @BindView(R.id.textConfirm) TextViewFont textConfirm;

  @BindView(R.id.textTrans) TextViewFont textTrans;

  private static final String PHONE_NUMBER = "phoneNumber";
  private static final String RESEND = "resend";

  private boolean resend;
  private String phoneNumber;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> confirmClicked = PublishSubject.create();
  private PublishSubject<Void> cancelClicked = PublishSubject.create();
  private PublishSubject<Void> onDismiss = PublishSubject.create();

  public Observable<Void> confirmClicked() {
    return confirmClicked;
  }

  public Observable<Void> cancelClicked() {
    return cancelClicked;
  }

  public Observable<Void> onDismiss() {
    return onDismiss;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View fragmentView =
        inflater.inflate(R.layout.dialog_fragment_authentication, container, false);
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

    Bundle args = getArguments();
    resend = args.getBoolean(RESEND);
    phoneNumber = args.getString(PHONE_NUMBER);

    if (resend) {
      textTitle.setText(R.string.onboarding_popup_error_title);
      textSummary.setText(getString(R.string.onboarding_popup_error_description));
      textCancel.setText(getString(R.string.onboarding_popup_error_edit_button_title));
      textConfirm.setText(getString(R.string.onboarding_popup_error_resend_button_title));
      LinearLayout.LayoutParams layoutBottomParams =
          (LinearLayout.LayoutParams) layoutBottom.getLayoutParams();
      layoutBottomParams.setMargins(0, 0, 0, 0);
      layoutBottom.setLayoutParams(layoutBottomParams);
      CodeSentToView codeSentToView = new CodeSentToView(getContext());
      LinearLayout.LayoutParams codeSentToViewLayoutParams =
          new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
              getResources().getDimensionPixelSize(R.dimen.notif_size_big));
      codeSentToViewLayoutParams.setMargins(0,
          getResources().getDimensionPixelOffset(R.dimen.vertical_margin_xlarge), 0, 0);
      codeSentToView.setLayoutParams(codeSentToViewLayoutParams);
      codeSentToView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red_2));
      layoutParent.addView(codeSentToView, 3);
      codeSentToView.initUi();
      codeSentToView.setTextPhoneNumber(phoneNumber);

      subscriptions.add(Observable.timer(600, TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(time -> {
            textTrans.animate()
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .alpha(AnimationUtils.ALPHA_FULL)
                .start();
          }));
    } else {
      textTitle.setText(phoneNumber);
      textSummary.setText(getString(R.string.onboarding_popup_confirm_description));
      textCancel.setText(getString(R.string.onboarding_popup_confirm_edit_button_title));
      textConfirm.setText(getString(R.string.onboarding_popup_confirm_send_button_title));
    }

    subscriptions.add(RxView.clicks(textCancel).subscribe(aVoid -> {
      cancelClicked.onNext(null);
      dismiss();
    }));

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
