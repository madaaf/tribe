package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class TileInviteView extends SquareFrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;
  @BindView(R.id.viewBG) View viewBG;

  // RESOURCES
  private int marginBG;
  private int marginAvatar;

  // VARIABLES
  private Unbinder unbinder;
  private User user;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TileInviteView(Context context) {
    super(context);
    init(context, null);
  }

  public TileInviteView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TileInviteView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  public void updateWidth(int width) {
    int tempMarginBG = (int) (width * 0.15f);
    int tempMarginAvatar = (int) (width * 0.25f);
    updateGraphicConstraints(width, tempMarginBG, tempMarginAvatar);
  }

  private void updateGraphicConstraints(int width, int marginBG, int marginAvatar) {
    if (this.marginBG == marginBG || this.marginAvatar == marginAvatar) return;
    this.marginBG = marginBG;
    this.marginAvatar = marginAvatar;

    viewNewAvatar.updateWidth(width - marginAvatar);
    UIUtils.changeMarginOfView(viewBG, marginBG);
    UIUtils.changeMarginOfView(viewNewAvatar, marginAvatar);
    post(() -> requestLayout());
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_tile_invite, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
    setWillNotDraw(false);
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void setUser(User user) {
    if (this.user != null && this.user.equals(user)) return;
    this.user = user;

    if (!StringUtils.isEmpty(user.getCurrentRoomId())) {
      viewNewAvatar.setType(NewAvatarView.LIVE);
    } else if (user.isOnline()) {
      viewNewAvatar.setType(NewAvatarView.ONLINE);
    } else {
      viewNewAvatar.setType(NewAvatarView.NORMAL);
    }

    viewNewAvatar.load(user.getProfilePicture());
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
