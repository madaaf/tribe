package com.tribe.app.presentation.view.widget.notifications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.widget.AvatarsSuperposedLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.List;
import java.util.Random;
import rx.subscriptions.CompositeSubscription;

import static com.tribe.app.presentation.view.widget.notifications.SharingCardNotificationView.MULTIPLE_CHOICE;
import static com.tribe.app.presentation.view.widget.notifications.SharingCardNotificationView.PACKAGE_FACEBOOK;

/**
 * Created by madaaflak on 15/05/2017.
 */

public class ShareWatermarkView extends FrameLayout {

  private LayoutInflater inflater;
  private Context context;
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  private View cluster;
  private Unbinder unbinder;
  private String txtFriend, txtMinute;

  @BindView(R.id.txtFriendsSharingCard) TextViewFont txtFriends;
  @BindView(R.id.txtMinutesSharingCard) TextViewFont txtMinutes;
  @BindView(R.id.txtSharingCard) TextViewFont txtSharingCard;
  @BindView(R.id.shareWatermarkViewBg) LinearLayout bg;
  @BindView(R.id.avatarsSuperposedView) AvatarsSuperposedLayout avatarsSuperposedLayout;

  public ShareWatermarkView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public ShareWatermarkView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  ////////////////
  //   PUBLIC  //
  ////////////////

  public void setParam(String txtMin, String txtFriend) {
    this.txtMinute = txtMin;
    this.txtFriend = txtFriend;
  }

  public void initView(String packageTitle, List<TribeGuest> members) {
    setTextLayout();

    int[] colors = new int[] {
        ContextCompat.getColor(context, R.color.watermark_1),
        ContextCompat.getColor(context, R.color.watermark_2),
        ContextCompat.getColor(context, R.color.watermark_3),
        ContextCompat.getColor(context, R.color.watermark_4),
        ContextCompat.getColor(context, R.color.watermark_5),
        ContextCompat.getColor(context, R.color.watermark_6)
    };
    int color = getRandom(colors);
    bg.setBackgroundColor(color);
    avatarsSuperposedLayout.drawAvatarsAndNamesMembers(members, color);
    setIntent(packageTitle);
  }

  ////////////////
  //   PRIVATE  //
  ////////////////

  private void init(Context context) {
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    cluster = inflater.inflate(R.layout.view_share_watermark, this, true);
    unbinder = ButterKnife.bind(this);
  }

  private void setIntent(String packageTitle) {
    Bitmap bitmap = createClusterBitmap();
    String pathofBmp =
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null);
    Uri bmpUri = Uri.parse(pathofBmp);

    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
    intent.setType("image/png");

    if (packageTitle.equals(PACKAGE_FACEBOOK)) {
      PackageManager pm = context.getPackageManager();
      List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);
      for (final ResolveInfo app : activityList) {
        if ((app.activityInfo.packageName).startsWith("com.facebook.katana")) {
          final ActivityInfo activity = app.activityInfo;
          final ComponentName name =
              new ComponentName(activity.applicationInfo.packageName, activity.name);
          intent.setComponent(name);
          context.startActivity(intent);
          break;
        }
      }
      return;
    }

    intent.putExtra(android.content.Intent.EXTRA_TEXT,
        EmojiParser.demojizedText(context.getString(R.string.live_sharing_media_caption)));
    if (packageTitle.equals(MULTIPLE_CHOICE)) {
      context.startActivity(Intent.createChooser(intent, null));
    } else {
      intent.setPackage(packageTitle);
      context.startActivity(intent);
    }
  }

  private void setTextLayout() {
    txtSharingCard.setText(
        " " + EmojiParser.demojizedText(context.getString(R.string.live_sharing_infos_live_call)));
    txtFriends.setText(txtFriend + " ");
    txtMinutes.setText(txtMinute);
  }

  private static int getRandom(int[] array) {
    int rnd = new Random().nextInt(array.length);
    return array[rnd];
  }

  private Bitmap createClusterBitmap() {
    cluster.measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    Bitmap b = Bitmap.createBitmap(cluster.getMeasuredWidth(), cluster.getMeasuredHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    cluster.layout(0, 0, cluster.getMeasuredWidth(), cluster.getMeasuredHeight());
    cluster.draw(c);
    return b;
  }
}
