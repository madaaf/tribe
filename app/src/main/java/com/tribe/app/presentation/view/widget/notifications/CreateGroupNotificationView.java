package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by madaaflak on 05/04/2017.
 */

public class CreateGroupNotificationView extends LifeNotification {

  public static final String PREFILLED_GRP_MEMBERS = "PREFILLED_GRP_MEMBERS";
  public static final String CREATE_GRP_DIRECTLY = "CREATE_GRP_DIRECTLY";

  private final static int AVATARS_SIZE = 55;

  @BindView(R.id.createGrpNotificationView) LinearLayout notificationView;
  @BindView(R.id.txtGrpMembersNames) TextViewFont txtGrpMembersNames;
  @BindView(R.id.imgGrpMembersAvatar) LinearLayout avatarsContainer;

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

  private void setMembers(List<TribeGuest> members) {
    prefilledGrpMembers.clear();
    drawAvatarsAndNamesMembers(members);
    prefilledGrpMembers = getUserList(members);
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void drawAvatarsAndNamesMembers(List<TribeGuest> members) {
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
    txtGrpMembersNames.setText(txtNames);
  }

  private List<GroupMember> getUserList(List<TribeGuest> tribeGuests) {
    List<GroupMember> userList = new ArrayList<>();

    if (tribeGuests == null) return userList;

    for (TribeGuest guest : tribeGuests) {
      User u = new User(guest.getId());
      u.setProfilePicture(guest.getPicture());
      u.setDisplayName(guest.getDisplayName());
      u.setUsername(guest.getUserName());
      GroupMember groupMember = new GroupMember(u);
      groupMember.setMember(true);
      groupMember.setFriend(true);
      userList.add(groupMember);
    }

    return userList;
  }
}
