package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
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

  @IntDef({ LIVE, ONLINE, REGULAR, PHONE, FACEBOOK }) public @interface AvatarType {
  }

  public static final int LIVE = 0;
  public static final int ONLINE = 1;
  public static final int REGULAR = 2;
  public static final int PHONE = 3;
  public static final int FACEBOOK = 4;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgAvatar) ImageView imgAvatar;

  // VARIABLES
  private int type;
  private String url;
  private int drawableId;
  private Recipient recipient;
  private List<String> membersPic;
  private String groupId;

  // RESOURCES
  private int avatarSize;
  private String noUrl;
  private boolean isAttached = false;
  private GlideUtils.Builder pendingBuilder = null;

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

    // DEFAULT SIZE
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size);
    noUrl = getContext().getString(R.string.no_profile_picture_url);

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

    if (pendingBuilder != null) {
      pendingBuilder.load();
      pendingBuilder = null;
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttached = false;
    if (createImageSubscription != null) createImageSubscription.unsubscribe();
    pendingBuilder = null;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
      changeSize(getMeasuredWidth(), false);
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    isAttached = true;

    if (pendingBuilder != null) {
      pendingBuilder.load();
      pendingBuilder = null;
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttached = false;
    if (createImageSubscription != null) createImageSubscription.unsubscribe();
    pendingBuilder = null;
  }

  @Override public void load(Recipient recipient) {
    this.recipient = recipient;

    String previousAvatar = (String) getTag(R.id.profile_picture);

    if (createImageSubscription != null) createImageSubscription.unsubscribe();

    if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      loadGroupAvatar(invite.getProfilePicture(), previousAvatar, invite.getId(),
          invite.getMembersPic());
    } else if (recipient instanceof Shortcut) {
      load(recipient.getProfilePicture());
    }
  }

  @Override public void loadGroupAvatar(String url, String previousUrl, String groupId,
      List<String> membersPic) {
    this.url = url;
    this.membersPic = membersPic;
    this.groupId = groupId;

    if ((StringUtils.isEmpty(url) || url.equals(noUrl)) &&
        membersPic != null &&
        membersPic.size() > 0) {
      File groupAvatarFile = FileUtils.getAvatarForGroupId(getContext(), groupId, FileUtils.PHOTO);

      if ((StringUtils.isEmpty(previousUrl) ||
          !previousUrl.equals(groupAvatarFile.getAbsolutePath())) && groupAvatarFile.exists()) {
        setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());

        GlideUtils.Builder builder = new GlideUtils.Builder(getContext()).file(groupAvatarFile)
            .target(imgAvatar)
            .size(avatarSize);

        if (isAttached) {
          builder.load();
        } else {
          pendingBuilder = builder;
        }
      } else if (!groupAvatarFile.exists()) {
        if (!groupAvatarFile.exists() && membersPic != null && membersPic.size() > 0) {
          createImageSubscription =
              ImageUtils.createGroupAvatar(getContext(), groupId, membersPic, avatarSize)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .subscribe(bitmap -> {
                    GlideUtils.Builder builder =
                        new GlideUtils.Builder(getContext()).file(groupAvatarFile)
                            .size(avatarSize)
                            .target(imgAvatar);

                    if (isAttached) {
                      builder.load();
                    } else {
                      pendingBuilder = builder;
                    }
                  });
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

      GlideUtils.Builder builder =
          new GlideUtils.Builder(getContext()).url(url).size(avatarSize).target(imgAvatar);

      if (isAttached) {
        builder.load();
      } else {
        pendingBuilder = builder;
      }
    } else {
      loadPlaceholder();
    }
  }

  @Override public void load(int drawableId) {
    this.drawableId = drawableId;

    GlideUtils.Builder glideBuilder = new GlideUtils.Builder(getContext()).resourceId(drawableId)
        .size(avatarSize)
        .target(imgAvatar)
        .hasPlaceholder(false);

    if (isAttached) {
      glideBuilder.load();
    } else {
      pendingBuilder = glideBuilder;
    }
  }

  public void changeSize(int size, boolean shouldChangeLP) {
    refactorSize(size);

    if (shouldChangeLP) {
      UIUtils.changeSizeOfView(this, size);
    }
  }

  public void refactorSize(int size) {
    avatarSize = size;
    ViewGroup.LayoutParams params = imgAvatar.getLayoutParams();
    params.width = params.height = avatarSize;
    imgAvatar.setLayoutParams(params);
  }

  private void loadPlaceholder() {
    if (avatarSize == 0) return;

    GlideUtils.Builder builder =
        new GlideUtils.Builder(getContext()).size(avatarSize).target(imgAvatar);
        
    if (isAttached) {
      builder.load();
    } else {
      pendingBuilder = builder;
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
    return type == LIVE || type == ONLINE || type == PHONE || type == FACEBOOK;
  }

  public void setType(@AvatarType int type) {
    this.type = type;

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
}
