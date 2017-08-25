package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 16/05/2017.
 */

public class AvatarsSuperposedLayout extends LinearLayout {

  @IntDef({
      AVATARS_SMALL_SIZE, AVATARS_BIG_SIZE
  }) public @interface AvatarSize {
  }

  private final static int MAX_AVATAR = 7;
  public final static int AVATARS_SMALL_SIZE = 55;
  public final static int AVATARS_BIG_SIZE = 115;

  @Inject User user;
  @Inject ScreenUtils screenUtils;
  @Nullable @BindView(R.id.imgGrpMembersAvatar) LinearLayout avatarsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private List<TribeGuest> members;
  private Context context;

  public AvatarsSuperposedLayout(@NonNull Context context, List<TribeGuest> members) {
    super(context);
    initView(context, members);
  }

  public AvatarsSuperposedLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, members);
  }

  private void initView(Context context, List<TribeGuest> members) {
    this.context = context;
    this.members = members;

    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_avatars_members, this, true);
    unbinder = ButterKnife.bind(this);
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  public void drawAvatarsMembersLayout(List<TribeGuest> membersList, int backgroundColor,
      @AvatarSize int avatarSize) {
    List<TribeGuest> members = new ArrayList<>();
    members.addAll(membersList);
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    LinearLayout layout = new LinearLayout(getContext());
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setGravity(Gravity.CENTER);
    layout.setLayoutParams(params);

    // add myself
    members.add(0,
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false,
            false, user.getUsername()));

    int maxAvatar = members.size() > MAX_AVATAR ? MAX_AVATAR : members.size();

    for (int i = 0; i < maxAvatar; i++) {
      TribeGuest tribeGuest = members.get(i);
      AvatarView avatarView = new AvatarView(getContext());

      avatarView.setBackgroundResource(R.drawable.shape_circle_white);
      ((GradientDrawable) avatarView.getBackground()).setColor(backgroundColor);

      int padding = getResources().getDimensionPixelSize(R.dimen.margin_horizonta_avatrs);
      avatarView.setPadding(padding, padding, padding, padding);
      avatarView.load(tribeGuest.getPicture());
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

      layoutParams.width = screenUtils.dpToPx(avatarSize);
      layoutParams.height = screenUtils.dpToPx(avatarSize);

      if (i != 0) {
        layoutParams.setMargins(screenUtils.dpToPx(-20), 0, 0, 0);
      }

      avatarView.setLayoutParams(layoutParams);
      layout.addView(avatarView);
    }

    avatarsContainer.addView(layout);
  }
}
