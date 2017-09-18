package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 09/18/17.
 */
public class LiveChatButton extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtActive) TextViewFont txtActive;
  @BindView(R.id.txtInactive) TextViewFont txtInactive;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onOpenChat = PublishSubject.create();
  private PublishSubject<Boolean> onCloseChat = PublishSubject.create();

  public LiveChatButton(Context context) {
    super(context);
    init();
  }

  public LiveChatButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveChatButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_chat_button, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
  }

  private void initResources() {
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

  @OnClick(R.id.txtActive) void openChat() {
    onOpenChat.onNext(true);
    showView(txtInactive);
    hideView(txtActive);
  }

  @OnClick(R.id.txtInactive) void closeChat() {
    onCloseChat.onNext(true);
    showView(txtActive);
    hideView(txtInactive);
  }

  private void showView(View v) {
    v.animate()
        .setInterpolator(new DecelerateInterpolator())
        .alpha(1)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            v.animate().setListener(null).start();
            v.setClickable(true);
          }
        })
        .setDuration(DURATION)
        .start();
  }

  private void hideView(View v) {
    v.setClickable(false);
    v.animate()
        .setInterpolator(new DecelerateInterpolator())
        .alpha(0)
        .setDuration(DURATION)
        .start();
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
  }

  ////////////////
  // OBSERVABLE //
  ////////////////

  public Observable<Boolean> onOpenChat() {
    return onOpenChat;
  }

  public Observable<Boolean> onCloseChat() {
    return onCloseChat;
  }
}
