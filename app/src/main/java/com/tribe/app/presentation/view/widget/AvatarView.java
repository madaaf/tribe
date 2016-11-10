package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarView extends RoundedCornerLayout {

    @IntDef({GROUP, SINGLE})
    public @interface AvatarType {}

    public static final int GROUP = 0;
    public static final int SINGLE = 1;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    // VARIABLES
    private boolean hasBorder = true;
    private int type;

    // RESOURCES
    private int avatarSize;

    public AvatarView(Context context) {
        this(context, null);
        init(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_avatar, this, true);
        ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        hasBorder = a.getBoolean(R.styleable.AvatarView_border, false);
        type = a.getInt(R.styleable.AvatarView_avatarType, SINGLE);

        avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);

        setWillNotDraw(false);
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (hasBorder) {
            float borderWidth = 1f;

            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            borderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, borderWidth, metrics);

            Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintBorder.setAntiAlias(true);
            paintBorder.setColor(Color.WHITE);
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setAntiAlias(true);
            paintBorder.setStrokeWidth(borderWidth);

            float viewWidth = getWidth();
            float circleCenter = viewWidth / 2;

            canvas.drawCircle(circleCenter, circleCenter,
                    circleCenter, paintBorder);
        }
    }

    public void load(Recipient recipient) {
        String previousAvatar = (String) getTag(R.id.profile_picture);

        List<String> urls = new ArrayList<>();

        if (StringUtils.isEmpty(previousAvatar) || !previousAvatar.equals(recipient.getProfilePicture())) {
            if (recipient instanceof Membership && StringUtils.isEmpty(recipient.getProfilePicture())) {
                Membership membership = (Membership) recipient;
                urls.addAll(membership.getMembersPic());
            } else {
                urls.add(recipient.getProfilePicture());
            }
        }

        if (urls != null && urls.size() == 1) {
            setTag(R.id.profile_picture, urls.get(0));
            load(urls.get(0));
        } else if (urls != null && urls.size() > 1) {
            File groupAvatarFile = FileUtils.getAvatarForGroupId(getContext(), recipient.getSubId(), FileUtils.PHOTO);

            if (StringUtils.isEmpty(previousAvatar) || !previousAvatar.equals(groupAvatarFile.getAbsolutePath())) {
                if (!groupAvatarFile.exists()) {
                    System.out.println("CREATE");
                    ImageUtils.createGroupAvatar(getContext(), recipient.getSubId(), urls, avatarSize)
                            .subscribe(bitmap -> {
                                setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());
                                imgAvatar.setImageBitmap(bitmap);
                            });
                } else {
                    setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());
                    System.out.println("EXISTS");

                    Glide.with(getContext())
                            .load(groupAvatarFile)
                            .crossFade()
                            .into(imgAvatar);
                }
            }
        }
    }

    public void load(String url) {
        if (!StringUtils.isEmpty(url) && !url.equals(getContext().getString(R.string.no_profile_picture_url))) {
            Glide.with(getContext())
                    .load(url)
                    .override(avatarSize, avatarSize)
                    .centerCrop()
                    .crossFade()
                    .into(imgAvatar);
        } else {
            loadPlaceholder();
        }
    }

    private void loadPlaceholder() {
        Glide.with(getContext())
                .load(R.drawable.picto_placeholder_avatar)
                .override(avatarSize, avatarSize)
                .crossFade()
                .into(imgAvatar);
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }

    public void setType(@AvatarType int type) {
        this.type = type;
    }
}
