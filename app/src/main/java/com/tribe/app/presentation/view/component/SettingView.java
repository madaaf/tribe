package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.tribe.app.R;
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
public class SettingView extends FrameLayout {
    public SettingView(Context context) {
        super(context);
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.txtSectionTitle)
    TextViewFont txtSectionTitle;

    @BindView(R.id.txtSectionBody)
    TextViewFont txtSectionBody;

    @BindView(R.id.switchSetting)
    Switch switchMessage;

    @BindView(R.id.imageSetting)
    ImageView imageSetting;

    @BindView(R.id.txtNameSetting)
    TextViewFont txtNameSetting;

    Unbinder unbinder;


    int viewType;

    public static final int PICTURE = 0, NAME = 1, SWITCH = 2, SIMPLE = 4, DELETE = 5;

    private PublishSubject<Boolean> checkedSwitch = PublishSubject.create();


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting, this);
        unbinder = ButterKnife.bind(this);

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
            case PICTURE:
                imageSetting.setVisibility(VISIBLE);
                setFrameClickable();
                break;
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
            default:
                break;
        }

    }

    private void setFrameClickable() {
        setClickable(true);
        setForeground(ContextCompat.getDrawable(getContext(), R.drawable.selectable_button));
    }

}
