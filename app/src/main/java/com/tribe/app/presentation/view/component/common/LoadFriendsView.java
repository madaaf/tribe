package com.tribe.app.presentation.view.component.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import javax.inject.Inject;

/**
 * Created by tiago on 12/14/16.
 */
public class LoadFriendsView extends LinearLayout {

  public static final int FB = 0;
  public static final int ADDRESSBOOK = 1;

  @IntDef({ FB, ADDRESSBOOK }) public @interface Type {
  }

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtStatus) TextViewFont txtStatus;

  @BindView(R.id.viewSwitch) SwitchCompat viewSwitch;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // OBSERVABLES
  private PublishSubject<Boolean> onChecked = PublishSubject.create();

  // VARIABLES
  private Unbinder unbinder;
  private int type;
  private String title;
  private String status;

  public LoadFriendsView(Context context) {
    super(context);
    init(context, null);
  }

  public LoadFriendsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadFriendsView);

    setType(a.getInt(R.styleable.LoadFriendsView_loadType, FB));

    int layout = R.layout.view_load_friends;

    LayoutInflater.from(getContext()).inflate(layout, this);
    unbinder = ButterKnife.bind(this);

    if (a.hasValue(R.styleable.LoadFriendsView_loadTitle)) {
      setTitle(getResources().getString(a.getResourceId(R.styleable.LoadFriendsView_loadTitle,
              R.string.linked_friends_address_book)));
      setStatus(getResources().getString(a.getResourceId(R.styleable.LoadFriendsView_loadStatus,
              R.string.linked_friends_status_not_linked)));
    }

    a.recycle();

    viewSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onChecked.onNext(isChecked));

    setClickable(true);
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

  public void setType(int type) {
    this.type = type;
  }

  public void setTitle(String str) {
    title = str;
    computeTitle();
  }

  public void setStatus(String str) {
    status = str;
    computeStatus();
  }

  public void setChecked(boolean checked) {
    viewSwitch.setChecked(checked);
    setStatus(checked ? getContext().getString(R.string.linked_friends_status_linked)
            : getContext().getString(R.string.linked_friends_status_not_linked));
  }

  public void showLoading() {
    viewSwitch.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  public void hideLoading() {
    progressView.setVisibility(View.GONE);
    viewSwitch.setVisibility(View.VISIBLE);
  }

  private void computeTitle() {
    if (txtTitle != null && !StringUtils.isEmpty(title)) {
      txtTitle.setText(title);
    }
  }

  private void computeStatus() {
    if (txtStatus != null && !StringUtils.isEmpty(status)) {
      txtStatus.setText(status);
    }
  }

  // OBSERVABLES

  public Observable<Boolean> onChecked() {
    return onChecked;
  }
}
