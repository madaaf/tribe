package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 17/02/2016.
 */
public class NewAvatarView extends RelativeLayout implements Avatar {

  @IntDef({ LIVE, ONLINE, NORMAL }) public @interface AvatarType {
  }

  public static final int LIVE = 0;
  public static final int ONLINE = 1;
  public static final int NORMAL = 2;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewAvatar) AvatarView avatar;
  @BindView(R.id.viewRing) View viewRing;

  // VARIABLES
  private int type;
  private GradientDrawable gradientDrawable;

  // RESOURCES
  private int strokeWidth;

  public NewAvatarView(Context context) {
    this(context, null);
    init(context, null);
  }

  public NewAvatarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_new_avatar, this, true);
    ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();

    avatar.setType(AvatarView.REGULAR);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NewAvatarView);
    setType(a.getInt(R.styleable.NewAvatarView_newAvatarType, NORMAL));

    setWillNotDraw(false);
    a.recycle();

    setBackground(null);
  }

  private void initResources() {
    strokeWidth = screenUtils.dpToPx(3);
  }

  @Override public void load(Recipient recipient) {
    avatar.load(recipient);
  }

  @Override public void loadGroupAvatar(String url, String previousUrl, String groupId,
      List<String> membersPic) {
    avatar.loadGroupAvatar(url, previousUrl, groupId, membersPic);
  }

  @Override public void load(String url) {
    avatar.load(url);
  }

  @Override public void load(int drawableId) {
    avatar.load(drawableId);
  }

  public void setType(int type) {
    if (this.type == type) return;

    this.type = type;

    if (gradientDrawable == null) {
      gradientDrawable = new GradientDrawable();
      gradientDrawable.setColor(Color.TRANSPARENT);
      gradientDrawable.setShape(GradientDrawable.OVAL);
    }

    if (type == LIVE) {
      gradientDrawable.setStroke(strokeWidth, ContextCompat.getColor(getContext(), R.color.red));
    } else if (type == ONLINE) {
      gradientDrawable.setStroke(strokeWidth,
          ContextCompat.getColor(getContext(), R.color.blue_new));
    } else {
      gradientDrawable.setStroke(strokeWidth,
          ContextCompat.getColor(getContext(), R.color.grey_offline));
    }

    viewRing.setBackground(gradientDrawable);
  }
}
