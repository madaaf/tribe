package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;

import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 8/29/16.
 */
public class SettingItemView extends FrameLayout {

    public SettingItemView(Context context) {
        super(context);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.txtSectionTitle)
    TextViewFont txtSectionTitle;

    @BindView(R.id.txtSectionBody)
    TextViewFont txtSectionBody;

    @BindView(R.id.switchSetting)
    Switch switchMessage;

    @BindView(R.id.imageProf)
    ImageView imageProf;

    @BindView(R.id.imageCancelIcon)
    ImageView imageCancelIcon;

    @BindView(R.id.txtNameSetting)
    TextViewFont txtNameSetting;

    @BindView(R.id.imageSyncIcon)
    ImageView imageSyncIcon;

//    @Inject
//    Picasso picasso;

    Unbinder unbinder;


    int viewType;

    public static final int NAME = 1, SWITCH = 2, SIMPLE = 4, DELETE = 5, MORE = 6;

    private PublishSubject<Boolean> checkedSwitch = PublishSubject.create();


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_item, this);
        unbinder = ButterKnife.bind(this);

//        initDependencyInjector();
        setupSwitch();

    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

//        if (subscriptions.hasSubscriptions()) {
//            subscriptions.unsubscribe();
//            subscriptions.clear();
//        }

        super.onDetachedFromWindow();
    }

    private void setupSwitch() {
        switchMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkedSwitch.onNext(b);

            }
        });
    }

    public Observable<Boolean> checkedSwitch() {
        return checkedSwitch;
    }

    public void setTitleBodyViewType(String title, String body, int viewType) {
        txtSectionTitle.setText(title);
        txtSectionBody.setText(body);
        this.viewType = viewType;

        switch (viewType) {
            case NAME:
                txtNameSetting.setVisibility(VISIBLE);
                setFrameClickable();
                break;
            case SWITCH:
                switchMessage.setVisibility(VISIBLE);
                break;
            case SIMPLE:
                setFrameClickable();
                break;
            case DELETE:
                txtSectionTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.red_circle));
                setFrameClickable();
                break;
            case MORE:
                setFrameClickable();
                imageCancelIcon.setVisibility(VISIBLE);
                break;
            default:
                break;
        }

    }

    private void setFrameClickable() {
        setClickable(true);
        setForeground(ContextCompat.getDrawable(getContext(), R.drawable.selectable_button));
    }

    public void setSyncUp(int textColor, int image) {
        imageSyncIcon.setVisibility(VISIBLE);
        imageSyncIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), image));

        FrameLayout.LayoutParams titleViewLayoutParams = (FrameLayout.LayoutParams) txtSectionTitle.getLayoutParams();
        titleViewLayoutParams.setMarginStart(dpToPx(25));
        txtSectionTitle.setLayoutParams(titleViewLayoutParams);

        txtSectionBody.setTextColor(ContextCompat.getColor(getContext(), textColor));

    }

    public void setPicture(String picUrl) {

        imageProf.setVisibility(VISIBLE);

        Picasso.with(getContext())
                .load(picUrl)
                .fit()
                .centerCrop()
                .transform(new RoundedCornersTransformation(R.dimen.setting_pic_size >> 1, 0, RoundedCornersTransformation.CornerType.ALL))
                .into(imageProf);

        FrameLayout.LayoutParams titleViewLayoutParams = (FrameLayout.LayoutParams) txtSectionTitle.getLayoutParams();
        titleViewLayoutParams.setMarginStart(dpToPx(55));
        txtSectionTitle.setLayoutParams(titleViewLayoutParams);

        FrameLayout.LayoutParams bodyViewLayoutParams = (FrameLayout.LayoutParams) txtSectionBody.getLayoutParams();
        bodyViewLayoutParams.setMarginStart(dpToPx(55));
        txtSectionBody.setLayoutParams(bodyViewLayoutParams);

    }

    public ImageView getPictureImageView() {
        return imageProf;
    }

    public void setPictureBitmap(Bitmap bitmap) {
        imageProf.setImageBitmap(bitmap);
    }

    public void setName(String name) {
        txtNameSetting.setText(name);
    }

    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

}
