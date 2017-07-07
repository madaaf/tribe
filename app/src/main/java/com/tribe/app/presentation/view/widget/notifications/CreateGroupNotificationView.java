package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.widget.AvatarsSuperposedLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by madaaflak on 05/04/2017.
 */

public class CreateGroupNotificationView extends LifeNotification {

  public static final String PREFILLED_GRP_MEMBERS = "PREFILLED_GRP_MEMBERS";
  public static final String CREATE_GRP_DIRECTLY = "CREATE_GRP_DIRECTLY";

  @BindView(R.id.createGrpNotificationView) LinearLayout notificationView;
  @BindView(R.id.avatarsSuperposedView) AvatarsSuperposedLayout avatarsSuperposedView;
  @BindView(R.id.txtGrpMembersNames) TextViewFont txtGrpMembersNames;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private List<GroupMember> prefilledGrpMembers = new ArrayList<>();

  public CreateGroupNotificationView(@NonNull Context context, List<TribeGuest> members) {
    super(context);
    initView(context, members);
  }

  public CreateGroupNotificationView(@NonNull Context context, @Nullable AttributeSet attrs,
      List<TribeGuest> members) {
    super(context, attrs);
    initView(context, members);
  }

  private void initView(Context context, List<TribeGuest> members) {
    this.context = context;

    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_create_grp_notification, this, true);
    unbinder = ButterKnife.bind(this);
    setMembers(members);
  }

  @OnClick(R.id.btnAction1) void onClickAction1() {
    navigator.navigateToPrefilledCreationGroup((Activity) getContext(), prefilledGrpMembers, false);
    hideView();
  }

  @OnClick(R.id.btnAction2) void onClickAction2() {
    navigator.navigateToPrefilledCreationGroup((Activity) getContext(), prefilledGrpMembers, true);
    hideView();
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void setMembers(List<TribeGuest> members) {
    prefilledGrpMembers.clear();
    setNamesMembers(members);
    avatarsSuperposedView.drawAvatarsMembersLayout(members, Color.WHITE,
        AvatarsSuperposedLayout.AVATARS_SMALL_SIZE);
    prefilledGrpMembers = getUserList(members);
  }

  private void setNamesMembers(List<TribeGuest> members) {
    String txtNames = "";
    for (int i = 0; i < members.size(); i++) {
      TribeGuest tribeGuest = members.get(i);
      txtNames += tribeGuest.getDisplayName();
      if (!((members.size() - 1) == i)) {
        txtNames += ", ";
      }
    }
    txtGrpMembersNames.setText(txtNames);
  }
}
