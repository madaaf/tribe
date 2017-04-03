package com.tribe.app.presentation.view.component.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.layoutBG) ViewGroup layoutBG;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VARIABLES
  private Unbinder unbinder;
  private int type;
  private String imageUrl;
  private int iconId;
  private String title;

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
          R.string.search_add_addressbook_title)));
    }

    if (a.hasValue(R.styleable.LoadFriendsView_loadIcon)) {
      setIcon(a.getResourceId(R.styleable.LoadFriendsView_loadIcon, 0));
    }

    if (a.hasValue(R.styleable.LoadFriendsView_loadBG)) {
      setLayoutBG(a.getResourceId(R.styleable.LoadFriendsView_loadBG, 0));
    }

    a.recycle();

    setImage(user.getProfilePicture());
    setClickable(true);
    setOrientation(HORIZONTAL);
    setMinimumHeight(screenUtils.dpToPx(72.5f));
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

    if (type == FB) {
      TextViewCompat.setTextAppearance(txtTitle, R.style.Title_2_FB);
    } else {
      TextViewCompat.setTextAppearance(txtTitle, R.style.Title_2_AddressBook);
    }
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
  }

  public void setImage(String url) {
    imageUrl = url;
    computeImageView();
  }

  public void setIcon(@DrawableRes int iconId) {
    this.iconId = iconId;
    imgIcon.setImageResource(iconId);
  }

  public void setLayoutBG(@DrawableRes int bgId) {
    layoutBG.setBackgroundResource(bgId);
  }

  public void showLoading() {
    imgIcon.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  public void hideLoading() {
    progressView.setVisibility(View.GONE);
    imgIcon.setVisibility(View.VISIBLE);
  }

  private void computeTitle() {
    if (txtTitle != null && !StringUtils.isEmpty(title)) {
      txtTitle.setText(title);
    }
  }

  private void computeImageView() {
    if (viewAvatar != null && !StringUtils.isEmpty(imageUrl)) {
      viewAvatar.load(imageUrl);
    }
  }
}
