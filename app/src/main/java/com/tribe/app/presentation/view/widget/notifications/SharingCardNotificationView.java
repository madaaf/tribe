package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
 * Created by madaaflak on 09/05/2017.
 */

public class SharingCardNotificationView extends LifeNotification {

  public static final String CALL_GRP_MEMBERS = "CALL_GRP_MEMBERS";
  public static final String DURATION_CALL = "DURATION_CALL";

  public static final String PACKAGE_SNAPSHAT = "com.snapchat.android";
  public static final String PACKAGE_INSTA = "com.instagram.android";
  public static final String PACKAGE_TWITTER = "com.twitter.android";
  public static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
  public static final String PACKAGE_FACEBOOK = "PACKAGE_FACEBOOK";

  @BindView(R.id.txtFriendsSharingCard) TextViewFont txtFriends;
  @BindView(R.id.txtMinutesSharingCard) TextViewFont txtMinutes;
  @BindView(R.id.avatarsSuperposedView) AvatarsSuperposedLayout avatarsSuperposedLayout;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private List<GroupMember> prefilledGrpMembers = new ArrayList<>();
  private String txtMin = " ";
  private String txtFriend = " ";
  private List<TribeGuest> members;

  public SharingCardNotificationView(@NonNull Context context, List<TribeGuest> members,
      double durationCall) {
    super(context);
    initView(context, members, durationCall);
  }

  public SharingCardNotificationView(@NonNull Context context, @Nullable AttributeSet attrs,
      List<TribeGuest> members, double durationCall) {
    super(context, attrs);
    initView(context, members, durationCall);
  }

  private void initView(Context context, List<TribeGuest> members, double durationCall) {
    this.context = context;
    this.members = members;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_sharing_card_notification, this, true);
    unbinder = ButterKnife.bind(this);
    setMembers(members);

    int nbrFriends = members.size();
    int duration = (int) durationCall;

    txtMin += (duration > 1) ? context.getString(R.string.live_sharing_infos_you_mins, duration)
        : context.getString(R.string.live_sharing_infos_you_min, duration);

    txtFriend +=
        (nbrFriends > 1) ? context.getString(R.string.live_sharing_infos_you_friends, nbrFriends)
            : context.getString(R.string.live_sharing_infos_you_friend, nbrFriends);

    txtMinutes.setText(txtMin);
    txtFriends.setText(txtFriend + "!");
  }

  private void setMembers(List<TribeGuest> members) {
    prefilledGrpMembers.clear();
    avatarsSuperposedLayout.drawAvatarsMembersLayout(members, Color.WHITE);
    prefilledGrpMembers = getUserList(members);
  }

  ///////////////////
  // ACTION CLICK  //
  ///////////////////

  @OnClick(R.id.btnInsta) void onClickInstaBtn() {
    setImageIntent(PACKAGE_INSTA);
  }

  @OnClick(R.id.btnSnap) void onClickSnapBtn() {
    setImageIntent(PACKAGE_SNAPSHAT);
  }

  @OnClick(R.id.btnTwitter) void onClickTwitterBtn() {
    setImageIntent(PACKAGE_TWITTER);
  }

  @OnClick(R.id.btnFacebook) void onClickFacebookBtn() {
    setImageIntent(PACKAGE_FACEBOOK);
  }

  @OnClick(R.id.btnShare) void onClickShareBtn() {
    setImageIntent(MULTIPLE_CHOICE);
  }

  private void setImageIntent(String packageTxt) {
    ShareWatermarkView view = new ShareWatermarkView(context);
    view.setParam(txtMin, txtFriend);
    view.initView(packageTxt, members);
  }
}







