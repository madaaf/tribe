package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 8/29/16.
 */
public class SettingItemView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtSectionTitle)
    TextViewFont txtSectionTitle;

    @BindView(R.id.txtSectionBody)
    TextViewFont txtSectionBody;

    @BindView(R.id.switchSetting)
    SwitchCompat switchMessage;

    @BindView(R.id.imageProf)
    ImageView imageProf;

    @BindView(R.id.imageCancelIcon)
    ImageView imageCancelIcon;

    @BindView(R.id.txtNameSetting)
    TextViewFont txtNameSetting;

    @BindView(R.id.imageSyncIcon)
    ImageView imageSyncIcon;

    private Unbinder unbinder;
    private int viewType;

    public static final int NAME = 1, SWITCH = 2, SIMPLE = 4, DELETE = 5, MORE = 6;

    private PublishSubject<Boolean> checkedSwitch = PublishSubject.create();

    public SettingItemView(Context context) {
        super(context);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_item, this);
        unbinder = ButterKnife.bind(this);

        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setupSwitch();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (unbinder != null) unbinder.unbind();
        super.onDetachedFromWindow();
    }

    private void setupSwitch() {
        switchMessage.setOnCheckedChangeListener((compoundButton, b) -> checkedSwitch.onNext(b));
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

        int size = getContext().getResources().getDimensionPixelSize(R.dimen.setting_pic_size);

        if (!StringUtils.isEmpty(picUrl)) {
            Glide.with(getContext()).load(picUrl)
                    .override(size, size)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .crossFade()
                    .into(imageProf);
        }

        FrameLayout.LayoutParams titleViewLayoutParams = (FrameLayout.LayoutParams) txtSectionTitle.getLayoutParams();
        titleViewLayoutParams.setMarginStart(dpToPx(55));
        txtSectionTitle.setLayoutParams(titleViewLayoutParams);

        FrameLayout.LayoutParams bodyViewLayoutParams = (FrameLayout.LayoutParams) txtSectionBody.getLayoutParams();
        bodyViewLayoutParams.setMarginStart(dpToPx(55));
        bodyViewLayoutParams.setMarginEnd(dpToPx(55));
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

    public void setCheckedSwitch(boolean isChecked) {
        switchMessage.setChecked(isChecked);
    }

    public int dpToPx(int dp) {
        return screenUtils.dpToPx(dp);
    }
}
