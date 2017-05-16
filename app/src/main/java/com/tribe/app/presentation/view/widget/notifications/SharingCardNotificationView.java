package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.widget.AvatarsSuperposedLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 09/05/2017.
 */

public class SharingCardNotificationView extends LifeNotification {

  public static final String CALL_GRP_MEMBERS = "CALL_GRP_MEMBERS";

  private static final String PACKAGE_SNAPSHAT = "com.snapchat.android";
  private static final String PACKAGE_INSTA = "com.instagram.android";
  private static final String PACKAGE_TWITTER = "com.twitter.android";
  public static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
  private static final String PACKAGE_FACEBOOK = "CALL_GRP_MEMBERS";

  @BindView(R.id.txtFriendsSharingCard) TextViewFont txtFriends;
  @BindView(R.id.txtMinutesSharingCard) TextViewFont txtMinutes;
  @BindView(R.id.viewScreenShot) ImageView viewScreenShot;
  @BindView(R.id.avatarsSuperposedView) AvatarsSuperposedLayout avatarsSuperposedLayout;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private List<GroupMember> prefilledGrpMembers = new ArrayList<>();
  protected LoginManager loginManager;
  private CallbackManager callbackManager;
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
    avatarsSuperposedLayout.drawAvatarsAndNamesMembers(members, Color.WHITE);
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

  }

  @OnClick(R.id.btnShare) void onClickShareBtn() {
    setImageIntent(MULTIPLE_CHOICE);
  }

  private void setImageIntent(String packageTxt) {
    ShareWatermarkView view = new ShareWatermarkView(context);
    view.setParam(txtMin, txtFriend);
    view.initView(packageTxt, members);
  }

  private void shareImage(Bitmap bmp) {
    SharePhoto photo = new SharePhoto.Builder().setBitmap(bmp).build();
    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
    ShareDialog shareDialog = new ShareDialog((Activity) context);
    if (ShareDialog.canShow(SharePhotoContent.class)) {
      shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    } else {
      Timber.e("ok");
    }
  }
}
  /*
  private void initView(String packageTitle) {
    subscriptions.add(InstaCapture.getInstance((Activity) context)
        .captureRx()
        .subscribe(new Subscriber<Bitmap>() {
          @Override public void onCompleted() {
          }

          @Override public void onError(Throwable e) {
          }

          @Override public void onNext(Bitmap bitmap) {

            String pathofBmp =
                MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "title",
                    null);
            Uri bmpUri = Uri.parse(pathofBmp);
            final Intent emailIntent1 = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent1.putExtra(Intent.EXTRA_STREAM, bmpUri);
            emailIntent1.setType("image/png");
            emailIntent1.setPackage(packageTitle);
            context.startActivity(emailIntent1);
          }
        }));
  }
  */








