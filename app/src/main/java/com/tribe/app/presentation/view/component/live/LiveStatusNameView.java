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
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveStatusNameView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.txtNameActive) TextViewFont txtNameActive;

  @BindView(R.id.txtNameInactive) TextViewFont txtNameInactive;

  // VARIABLES
  private Unbinder unbinder;
  private Live live;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onOpenView = PublishSubject.create();
  private PublishSubject<Boolean> onCloseView = PublishSubject.create();

  public LiveStatusNameView(Context context) {
    super(context);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_status_name, this);
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

  @OnClick(R.id.txtNameActive) void openView() {
    onCloseView.onNext(true);
    showView(txtNameInactive);
    hideView(txtNameActive);
  }

  @OnClick(R.id.txtNameInactive) void closeView() {
    onOpenView.onNext(true);
    showView(txtNameActive);
    hideView(txtNameInactive);
  }

  private void setAddFriendsTitle() {
    int str = R.string.action_add_friend;
    txtNameInactive.setText(str);
    txtNameActive.setText(str);
  }

  private void setShortcutTitle() {
    txtNameInactive.setText(live.getShortcut().getName());
    txtNameActive.setText(live.getShortcut().getName());
  }

  private void setPeopleCountTitle(int total) {
    String str = getContext().getString(R.string.shortcut_members_count, total);
    txtNameInactive.setText(str);
    txtNameActive.setText(str);
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

  public void setLive(Live live) {
    this.live = live;

    if (!live.hasUsers() || live.getUserIds().size() <= 1) {
      setAddFriendsTitle();
    } else if (live.getShortcut() != null && !StringUtils.isEmpty(live.getShortcut().getName())) {
      setShortcutTitle();
    } else {
      setPeopleCountTitle(live.getUserIds().size());
    }

    if (live.onShortcutUpdated() == null) return;
    subscriptions.add(live.onShortcutUpdated().subscribe(shortcut -> refactorTitle()));

    if (live.onRoomUpdated() == null) return;
    subscriptions.add(live.onRoomUpdated().subscribe(room -> refactorTitle()));
  }

  private void refactorTitle() {
    Room room = live.getRoom();
    Shortcut shortcut = live.getShortcut();

    if (shortcut != null && !StringUtils.isEmpty(shortcut.getName())) {
      setShortcutTitle();
    } else if (room != null) {
      if (room.nbUsersTotal() <= 1) {
        setAddFriendsTitle();
      } else {
        setPeopleCountTitle(room.nbUsersTotal());
      }
    } else {
      setAddFriendsTitle();
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Boolean> onOpenView() {
    return onOpenView;
  }

  public Observable<Boolean> onCloseView() {
    return onCloseView;
  }
}
