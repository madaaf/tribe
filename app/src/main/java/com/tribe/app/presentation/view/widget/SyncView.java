package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 08/19/2016.
 */
public class SyncView extends FrameLayout {

    @IntDef({FB, ADDRESSBOOK})
    public @interface SyncViewType {}

    public static final int FB = 0;
    public static final int ADDRESSBOOK = 1;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewBG)
    View viewBG;

    @BindView(R.id.imgIcon)
    ImageView imgIcon;

    @BindView(R.id.viewExclamation)
    ImageView viewExclamation;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VARIABLES
    private int iconId;
    private int type;
    private int backgroundId;

    // RESOURCES
    private int radiusImage;
    private int margin;

    // OBSERVABLES
    private final PublishSubject<View> onClick = PublishSubject.create();

    public SyncView(Context context) {
        this(context, null);
        init(context, null);
    }

    public SyncView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(R.layout.view_sync, this, true);
//        ButterKnife.bind(this);
//
//        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
//
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonPointsView);
//
//        setType(a.getInt(R.styleable.ButtonPointsView_buttonType, PROFILE));
//
//        if (a.hasValue(R.styleable.ButtonPointsView_buttonDrawable))
//            setDrawableResource(a.getResourceId(R.styleable.ButtonPointsView_buttonDrawable, 0));
//
//        setLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonLabel, R.string.contacts_share_profile_button));
//        setSubLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonSubLabel, R.string.contacts_share_profile_description));
//        setPoints(a.getInteger(R.styleable.ButtonPointsView_buttonPoints, 0));
//
//        if (type != FB_DISABLED) {
//            viewBG.setClickable(true);
//            viewBG.setEnabled(true);
//            viewBG.setOnClickListener(v -> clickButton.onNext(this));
//        } else {
//            viewBG.setEnabled(false);
//            viewBG.setClickable(false);
//        }
//
//        a.recycle();
//    }
//
//    public void setType(int type) {
//        this.type = type;
//
//        if (type == PROFILE) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        } else {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        }
//
//        setBackgroundButton();
//    }
//
//    public void setBackgroundButton() {
//        if (type == PROFILE)
//            viewBG.setBackgroundResource(R.drawable.bg_button_blue_text);
//        else if (type == FB_SYNC || type == FB_NOTIFY)
//            viewBG.setBackgroundResource(R.drawable.bg_button_fb_light);
//        else if (type == FB_DISABLED)
//            viewBG.setBackgroundResource(R.drawable.bg_button_disabled);
//    }
//
//    public void setDrawableResource(int res) {
//        this.drawableId = res;
//
//        if (type != PROFILE) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            imageView.setImageResource(drawableId);
//        }
//    }
//
//    public void setLabel(int res) {
//        label = res;
//        txtLabel.setText(label);
//    }
//
//    public void setSubLabel(int res) {
//        subLabel = res;
//        txtSubLabel.setText(res);
//    }
//
//    public void setDrawable(String url) {
//        if (type == PROFILE) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//            if (!StringUtils.isEmpty(url)) {
//                Glide.with(getContext()).load(url).centerCrop()
//                        .bitmapTransform(new RoundedCornersTransformation(getContext(), radiusImage, 0, RoundedCornersTransformation.CornerType.LEFT))
//                        .crossFade()
//                        .into(imageView);
//            }
//        }
//    }
//
//    public void setPoints(int points) {
//        txtPoints.setText(getContext().getString(R.string.points_suffix, points));
//    }
//
//    public void animateProgress() {
//        ViewGroup.LayoutParams params = viewBGProgress.getLayoutParams();
//        ValueAnimator widthAnimator = ValueAnimator.ofInt(0, viewBG.getWidth());
//        widthAnimator.setDuration(1500);
//        widthAnimator.setInterpolator(new DecelerateInterpolator());
//        widthAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                viewBGProgress.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                viewBGProgress.setVisibility(View.GONE);
//                if (type == FB_SYNC) syncFBDone.onNext(SyncView.this);
//                else if (type == FB_NOTIFY) notifyDone.onNext(SyncView.this);
//            }
//        });
//        widthAnimator.addUpdateListener(animation -> {
//            params.width = (Integer) animation.getAnimatedValue();
//            viewBGProgress.setLayoutParams(params);
//        });
//        widthAnimator.start();
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//    }
//
//    // OBSERVABLES
//    public Observable<View> onClick() {
//        return clickButton;
//    }
//
//    public Observable<View> onFBSyncDone() {
//        return syncFBDone;
//    }
//
//    public Observable<View> onNotifyDone() {
//        return notifyDone;
//    }
}
