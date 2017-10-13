package com.tribe.app.presentation.view.component.live;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
import com.tribe.app.presentation.utils.FontUtils;
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

  @BindView(R.id.layoutBG) ViewGroup layoutBG;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Unbinder unbinder;
  private Live live;
  private boolean active = false;

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

    setLayoutTransition(new LayoutTransition());

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

  @OnClick(R.id.txtName) void clickOpenView() {
    if (active) {
      closeView();
    } else {
      openView();
    }
  }

  private void setAddFriendsTitle() {
    int str = R.string.action_add_friend;
    txtName.setText(str);
  }

  private void setShortcutTitle() {
    txtName.setText(live.getShortcut().getName());
  }

  private void setPeopleCountTitle(int total) {
    String str = getContext().getString(R.string.shortcut_members_count, total);
    txtName.setText(str);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public int getNewWidth() {
    txtName.measure(0, 0);
    return txtName.getMeasuredWidth() + screenUtils.dpToPx(15 * 2);
  }

  public void openView() {
    if (active) return;

    active = true;
    layoutBG.setBackgroundResource(R.drawable.bg_live_name_active);
    txtName.setText(R.string.action_back);
    txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.picto_drawer_active, 0);
    txtName.setShadowLayer(0, 0, 0, 0);
    TextViewCompat.setTextAppearance(txtName, R.style.Body_Two_Black);

    txtName.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    onOpenView.onNext(active);
  }

  public void closeView() {
    if (!active) return;

    active = false;
    layoutBG.setBackgroundResource(R.drawable.bg_live_name);
    refactorTitle();
    txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.picto_drawer_inactive, 0);
    txtName.setShadowLayer(10, 3, 3,
        ContextCompat.getColor(getContext(), R.color.black_opacity_50));
    TextViewCompat.setTextAppearance(txtName, R.style.Body_Two_White);

    txtName.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    onCloseView.onNext(active);
  }

  public void dispose() {
  }

  public void setLive(Live live) {
    this.live = live;

    if (!live.hasUsers() || live.getUserIdsOfShortcut().size() <= 1) {
      setAddFriendsTitle();
    } else if (live.getShortcut() != null && !StringUtils.isEmpty(live.getShortcut().getName())) {
      setShortcutTitle();
    } else {
      setPeopleCountTitle(live.getUserIdsOfShortcut().size());
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
      if (room.nbUsersTotalWithoutMe(user.getId()) <= 1) {
        setAddFriendsTitle();
      } else {
        setPeopleCountTitle(room.nbUsersTotalWithoutMe(user.getId()));
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
