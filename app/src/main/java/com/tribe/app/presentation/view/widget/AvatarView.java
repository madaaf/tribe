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
import com.bumptech.glide.signature.StringSignature;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;

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
    private boolean hasBorder = false;
    private int type;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_avatar, this, true);
        ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        //hasBorder = a.getBoolean(R.styleable.AvatarView_border, false);
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

        if (createImageSubscription != null) createImageSubscription.unsubscribe();

        if (recipient instanceof Friendship) {
            if (StringUtils.isEmpty(previousAvatar) || !previousAvatar.equals(recipient.getProfilePicture()))
                load(recipient.getProfilePicture());
        } else if (recipient instanceof Membership) {
            Membership membership = (Membership) recipient;
            if (StringUtils.isEmpty(recipient.getProfilePicture())) {
                File groupAvatarFile = FileUtils.getAvatarForGroupId(getContext(), recipient.getSubId(), FileUtils.PHOTO);

                if ((StringUtils.isEmpty(previousAvatar) || !previousAvatar.equals(groupAvatarFile.getAbsolutePath()))
                        && groupAvatarFile.exists()) {
                    setTag(R.id.profile_picture, groupAvatarFile.getAbsolutePath());

                    Glide.with(getContext())
                            .load(groupAvatarFile)
                            .signature(new StringSignature(String.valueOf(groupAvatarFile.lastModified())))
                            .crossFade()
                            .into(imgAvatar);
                } else if (!groupAvatarFile.exists()) {
//                    if (!groupAvatarFile.exists() && membership.getMembersPic() != null && membership.getMembersPic().size() > 0) {
//                        createImageSubscription = ImageUtils.createGroupAvatar(getContext(), membership.getSubId(), membership.getMembersPic(), avatarSize)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribeOn(Schedulers.io())
//                                .subscribe(bitmap -> imgAvatar.setImageBitmap(bitmap));
//                    }

                    loadPlaceholder();
                }
            } else {
                load(recipient.getProfilePicture());
            }
        }
    }

    public void load(String url) {
        if (!StringUtils.isEmpty(url) && !url.equals(getContext().getString(R.string.no_profile_picture_url))) {
            setTag(R.id.profile_picture, url);

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
