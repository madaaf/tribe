package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 16/05/2017.
 */

public class AvatarsSuperposedLayout extends LinearLayout {

  protected final static int AVATARS_SIZE = 55;

  @Inject ScreenUtils screenUtils;
  @Nullable @BindView(R.id.txtGrpMembersNames) TextViewFont txtGrpMembersNames;
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

  public void drawAvatarsAndNamesMembers(List<TribeGuest> members, int backgroundColor) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    LinearLayout layout = new LinearLayout(getContext());
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setGravity(Gravity.CENTER);
    layout.setLayoutParams(params);

    String txtNames = "";
    for (int i = 0; i < members.size(); i++) {

      TribeGuest tribeGuest = members.get(i);
      txtNames += tribeGuest.getDisplayName();
      if (!((members.size() - 1) == i)) {
        txtNames += ", ";
      }
      AvatarView avatarView = new AvatarView(getContext());

      avatarView.setBackgroundResource(R.drawable.shape_circle_white);
      ((GradientDrawable) avatarView.getBackground()).setColor(backgroundColor);

      int padding = getResources().getDimensionPixelSize(R.dimen.margin_horizonta_avatrs);
      avatarView.setPadding(padding, padding, padding, padding);
      avatarView.load(tribeGuest.getPicture());
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

      layoutParams.width = screenUtils.dpToPx(AVATARS_SIZE);
      layoutParams.height = screenUtils.dpToPx(AVATARS_SIZE);
      if (i != 0) {
        layoutParams.setMargins(screenUtils.dpToPx(-AVATARS_SIZE / 3), 0, 0, 0);
      }
      avatarView.setLayoutParams(layoutParams);
      layout.addView(avatarView);
    }
    avatarsContainer.addView(layout);
    if (txtGrpMembersNames != null) txtGrpMembersNames.setText(txtNames);
  }
}
