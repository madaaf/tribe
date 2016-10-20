package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 08/19/2016.
 */
public class ButtonPointsView extends LinearLayout {

    @IntDef({PROFILE, FB_SYNC, FB_NOTIFY, FB_DISABLED})
    public @interface ButtonType {}

    public static final int PROFILE = 0;
    public static final int FB_SYNC = 1;
    public static final int FB_NOTIFY = 2;
    public static final int FB_DISABLED = 3;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewBG)
    View viewBG;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.txtLabel)
    TextViewFont txtLabel;

    @BindView(R.id.txtSubLabel)
    TextViewFont txtSubLabel;

    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    @BindView(R.id.viewBGProgress)
    View viewBGProgress;

    // VARIABLES
    private int drawableId;
    private int type;
    private int label;
    private int subLabel;
    private int points;

    // RESOURCES
    private int radiusImage;
    private int margin;

    // OBSERVABLES
    private final PublishSubject<View> clickButton = PublishSubject.create();
    private final PublishSubject<View> syncFBDone = PublishSubject.create();
    private final PublishSubject<View> notifyDone = PublishSubject.create();

    public ButtonPointsView(Context context) {
        this(context, null);
        init(context, null);
    }

    public ButtonPointsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_button_points, this, true);
        ButterKnife.bind(this);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        radiusImage = context.getResources().getDimensionPixelSize(R.dimen.radius_share_img);
        margin = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_xsmall);

        setOrientation(VERTICAL);
        setPadding(margin, margin, margin, margin);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonPointsView);

        setType(a.getInt(R.styleable.ButtonPointsView_buttonType, PROFILE));

        if (a.hasValue(R.styleable.ButtonPointsView_buttonDrawable))
            setDrawableResource(a.getResourceId(R.styleable.ButtonPointsView_buttonDrawable, 0));

        setLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonLabel, R.string.contacts_share_profile_button));
        setSubLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonSubLabel, R.string.contacts_share_profile_description));
        setPoints(a.getInteger(R.styleable.ButtonPointsView_buttonPoints, 0));

        if (type != FB_DISABLED) {
            viewBG.setClickable(true);
            viewBG.setEnabled(true);
            viewBG.setOnClickListener(v -> clickButton.onNext(this));
        } else {
            viewBG.setEnabled(false);
            viewBG.setClickable(false);
        }

        a.recycle();
    }

    public void setType(int type) {
        this.type = type;

        if (type == PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        setBackgroundButton();
    }

    public void setBackgroundButton() {
        if (type == PROFILE)
            viewBG.setBackgroundResource(R.drawable.bg_button_blue_text);
        else if (type == FB_SYNC || type == FB_NOTIFY)
            viewBG.setBackgroundResource(R.drawable.bg_button_fb_light);
        else if (type == FB_DISABLED)
            viewBG.setBackgroundResource(R.drawable.bg_button_disabled);
    }

    public void setDrawableResource(int res) {
        this.drawableId = res;

        if (type != PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(drawableId);
        }
    }

    public void setLabel(int res) {
        label = res;
        txtLabel.setText(label);
    }

    public void setSubLabel(int res) {
        subLabel = res;
        txtSubLabel.setText(res);
    }

    public void setDrawable(String url) {
        if (type == PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (!StringUtils.isEmpty(url)) {
                Glide.with(getContext()).load(url).centerCrop()
                        .bitmapTransform(new RoundedCornersTransformation(getContext(), radiusImage, 0, RoundedCornersTransformation.CornerType.LEFT))
                        .crossFade()
                        .into(imageView);
            }
        }
    }

    public void setPoints(int points) {
        txtPoints.setText(getContext().getString(R.string.points_suffix, points));
    }

    public void animateProgress() {
        ViewGroup.LayoutParams params = viewBGProgress.getLayoutParams();
        ValueAnimator widthAnimator = ValueAnimator.ofInt(0, viewBG.getWidth());
        widthAnimator.setDuration(1500);
        widthAnimator.setInterpolator(new DecelerateInterpolator());
        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                viewBGProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewBGProgress.setVisibility(View.GONE);
                if (type == FB_SYNC) syncFBDone.onNext(ButtonPointsView.this);
                else if (type == FB_NOTIFY) notifyDone.onNext(ButtonPointsView.this);
            }
        });
        widthAnimator.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            viewBGProgress.setLayoutParams(params);
        });
        widthAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    // OBSERVABLES
    public Observable<View> onClick() {
        return clickButton;
    }

    public Observable<View> onFBSyncDone() {
        return syncFBDone;
    }

    public Observable<View> onNotifyDone() {
        return notifyDone;
    }
}
