package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.HoleTransformation;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarView extends RelativeLayout implements Avatar {

  private static final float SHADOW_RATIO = 0.22f;

  @IntDef({ LIVE, ONLINE, REGULAR, PHONE }) public @interface AvatarType {
  }

  public static final int LIVE = 0;
  public static final int ONLINE = 1;
  public static final int REGULAR = 2;
  public static final int PHONE = 3;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgShadow) ImageView imgShadow;
  @BindView(R.id.imgAvatar) ImageView imgAvatar;
  @BindView(R.id.imgInd) ImageView imgInd;

  // VARIABLES
  private int type;
  private String url;
  private int drawableId;
  private Recipient recipient;
  private List<String> membersPic;
  private String groupId;
  private boolean hasShadow = false;
  private boolean hasInd = true;
  private boolean hasHole = true;
  private boolean isAttached = false;

  // RESOURCES
  private int avatarSize;
  private int paddingShadow;
  private String noUrl;

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
    hasShadow = a.getBoolean(R.styleable.AvatarView_hasShadow, false);
    hasInd = a.getBoolean(R.styleable.AvatarView_hasInd, false);
    hasHole = a.getBoolean(R.styleable.AvatarView_hasHole, true);

    // DEFAULT SIZE
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size);
    noUrl = getContext().getString(R.string.no_profile_picture_url);

    if (hasShadow) imgShadow.setVisibility(View.VISIBLE);
    if (hasInd && isOnlineOrLive()) imgInd.setVisibility(View.VISIBLE);

    setWillNotDraw(false);
    a.recycle();

    setBackground(null);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        changeSize(getMeasuredWidth(), false);
      }
    });
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    isAttached = true;
    Glide.get(getContext()).clearMemory();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttached = false;
    Glide.get(getContext()).clearMemory();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
      changeSize(getMeasuredWidth(), false);
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

    if ((StringUtils.isEmpty(url) || url.equals(noUrl))
        && membersPic != null
        && membersPic.size() > 0) {
      File groupAvatarFile = FileUtils.getAvatarForGroupId(getContext(), groupId, FileUtils.PHOTO);

      if ((StringUtils.isEmpty(previousUrl) || !previousUrl.equals(
          groupAvatarFile.getAbsolutePath())) && groupAvatarFile.exists()) {
        setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());
        new GlideUtils.Builder(getContext()).file(groupAvatarFile)
            .target(imgAvatar)
            .size(avatarSize)
            .hasHole(hasHole && isOnlineOrLive())
            .load();
      } else if (!groupAvatarFile.exists()) {
        if (!groupAvatarFile.exists() && membersPic != null && membersPic.size() > 0) {
          createImageSubscription =
              ImageUtils.createGroupAvatar(getContext(), groupId, membersPic, avatarSize)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .subscribe(bitmap -> new GlideUtils.Builder(getContext()).file(groupAvatarFile)
                      .size(avatarSize)
                      .target(imgAvatar)
                      .hasHole(hasHole && isOnlineOrLive())
                      .load());
        }
      }
    } else {
      load(url);
    }
  }

  @Override public void load(String url) {
    this.url = url;

    if (!StringUtils.isEmpty(url) && !url.equals(noUrl)) {
      setTag(R.id.profile_picture, url);

      new GlideUtils.Builder(getContext()).url(url)
          .size(avatarSize)
          .target(imgAvatar)
          .hasHole(hasHole && isOnlineOrLive())
          .load();
    } else {
      loadPlaceholder(hasHole && isOnlineOrLive());
    }
  }

  @Override public void load(int drawableId) {
    this.drawableId = drawableId;
    new GlideUtils.Builder(getContext()).resourceId(drawableId)
        .size(avatarSize)
        .target(imgAvatar)
        .hasPlaceholder(false)
        .hasHole(false)
        .load();
  }

  public void changeSize(int size, boolean shouldChangeLP) {
    paddingShadow = hasShadow ? (int) (size * getShadowRatio()) : 0;
    refactorSize(size);

    if (shouldChangeLP) {
      UIUtils.changeSizeOfView(this, size);
    }
  }

  public void refactorSize(int size) {
    avatarSize = size - paddingShadow;
    ViewGroup.LayoutParams params = imgAvatar.getLayoutParams();
    params.width = params.height = avatarSize;
    imgAvatar.setLayoutParams(params);

    int indSize = (int) (avatarSize * HoleTransformation.RATIO * (type == PHONE ? 3f : 2f));

    MarginLayoutParams paramsInd = (MarginLayoutParams) imgInd.getLayoutParams();
    paramsInd.width = paramsInd.height = indSize;
    paramsInd.bottomMargin = paramsInd.rightMargin =
        -(int) (indSize * (type == PHONE ? HoleTransformation.RATIO * 1.1f : 0));
    imgInd.setLayoutParams(paramsInd);
  }

  private void loadPlaceholder(boolean hasHole) {
    if (avatarSize == 0) return;
    if (isAttached) {
      new GlideUtils.Builder(getContext()).size(avatarSize)
          .target(imgAvatar)
          .hasHole(hasHole)
          .load();
    }
  }

  public float getShadowRatio() {
    return SHADOW_RATIO;
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

  private boolean isOnlineOrLive() {
    return type == LIVE || type == ONLINE || type == PHONE;
  }

  public void setType(@AvatarType int type) {
    this.type = type;

    //if (getMeasuredWidth() != 0) refactorSize(getWidth());

    if (type == PHONE) {
      imgInd.setVisibility(View.VISIBLE);
      imgInd.setImageResource(R.drawable.picto_call);
    } else if (type == LIVE && hasInd) {
      imgInd.setVisibility(View.VISIBLE);
      imgInd.setImageResource(R.drawable.picto_live);
    } else if (type == ONLINE && hasInd) {
      imgInd.setVisibility(View.VISIBLE);
      imgInd.setImageResource(R.drawable.picto_online);
    } else {
      imgInd.setVisibility(View.GONE);
    }

    if (recipient != null) {
      load(recipient);
    } else if (!StringUtils.isEmpty(url)) {
      load(url);
    } else if (!StringUtils.isEmpty(groupId)) {
      loadGroupAvatar(url, null, groupId, membersPic);
    } else if (drawableId != 0) {
      load(drawableId);
    }
  }

  public void setHasHole(boolean hasHole) {
    this.hasHole = hasHole;
  }

  public void setHasInd(boolean hasInd) {
    this.hasInd = hasInd;
  }

  public void setHasShadow(boolean hasShadow) {
    this.hasShadow = hasShadow;
  }
}
