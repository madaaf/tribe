package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarView extends LinearLayout implements Avatar {

  @IntDef({ LIVE, REGULAR }) public @interface AvatarType {
  }

  public static final int LIVE = 0;
  public static final int REGULAR = 1;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgAvatar) ImageView imgAvatar;

  // VARIABLES
  private int type;
  private Paint transparentPaint;
  private String url;
  private Recipient recipient;
  private List<String> membersPic;
  private String groupId;

  // RESOURCES
  private int avatarSize;

  // SUBSCRIPTIONS
  private Subscription createImageSubscription;

  public AvatarView(Context context) {
    this(context, null);
    init(context, null);
  }

  public AvatarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_avatar, this, true);
    ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
    type = a.getInt(R.styleable.AvatarView_avatarType, REGULAR);

    avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);

    setWillNotDraw(false);
    a.recycle();

    setBackground(null);
    setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    transparentPaint = new Paint();
    transparentPaint.setAntiAlias(true);
    transparentPaint.setDither(false);
    transparentPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
    transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (type == LIVE) {
      int radius = getRadius();
      canvas.drawCircle(getWidth() - radius, getHeight() - radius, radius, transparentPaint);
    }
  }

  @Override public void load(Recipient recipient) {
    this.recipient = recipient;

    String previousAvatar = (String) getTag(R.id.profile_picture);

    if (createImageSubscription != null) createImageSubscription.unsubscribe();

    if (recipient instanceof Friendship) {
      if (StringUtils.isEmpty(previousAvatar) || !previousAvatar.equals(
          recipient.getProfilePicture())) {
        load(recipient.getProfilePicture());
      }
    } else if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      loadGroupAvatar(membership.getProfilePicture(), previousAvatar, membership.getSubId(),
          membership.getMembersPic());
    } else if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      loadGroupAvatar(invite.getProfilePicture(), previousAvatar, invite.getId(),
          invite.getMembersPic());
    }
  }

  @Override public void loadGroupAvatar(String url, String previousUrl, String groupId,
      List<String> membersPic) {
    this.url = url;
    this.membersPic = membersPic;
    this.groupId = groupId;

    if (StringUtils.isEmpty(url)) {
      File groupAvatarFile = FileUtils.getAvatarForGroupId(getContext(), groupId, FileUtils.PHOTO);

      if ((StringUtils.isEmpty(previousUrl) || !previousUrl.equals(
          groupAvatarFile.getAbsolutePath())) && groupAvatarFile.exists()) {
        setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());

        Glide.with(getContext())
            .load(groupAvatarFile)
            .bitmapTransform(new CropCircleTransformation(getContext()))
            .signature(new StringSignature(String.valueOf(groupAvatarFile.lastModified())))
            .crossFade()
            .into(imgAvatar);
      } else if (!groupAvatarFile.exists()) {
        if (!groupAvatarFile.exists() && membersPic != null && membersPic.size() > 0) {
          createImageSubscription =
              ImageUtils.createGroupAvatar(getContext(), groupId, membersPic, avatarSize)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .doOnError(throwable -> System.out.println("Error"))
                  .subscribe(bitmap -> imgAvatar.setImageBitmap(bitmap));
        }

        loadPlaceholder();
      }
    } else {
      load(url);
    }
  }

  @Override public void load(String url) {
    this.url = url;

    if (!StringUtils.isEmpty(url) && !url.equals(
        getContext().getString(R.string.no_profile_picture_url))) {
      setTag(R.id.profile_picture, url);

      GlideUtils.load(getContext(), url, avatarSize, imgAvatar);
    } else {
      loadPlaceholder();
    }
  }

  private void loadPlaceholder() {
    GlideUtils.load(getContext(), avatarSize, imgAvatar);
  }

  public int getRadius() {
    return (int) (getWidth() * 0.2f);
  }

  public String getUrl() {
    return url;
  }

  public Recipient getRecipient() {
    return recipient;
  }

  public String getGroupId() {
    return groupId;
  }

  public List<String> getMembersPic() {
    return membersPic;
  }

  public void setType(@AvatarType int type) {
    this.type = type;
  }
}
