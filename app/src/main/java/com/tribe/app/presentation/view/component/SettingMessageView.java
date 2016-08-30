package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 8/29/16.
 */
public class SettingMessageView extends FrameLayout {
    public SettingMessageView(Context context) {
        super(context);
    }

    public SettingMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @BindView(R.id.txtSectionTitle)
    TextViewFont txtSectionTitle;

    @BindView(R.id.txtSectionBody)
    TextViewFont txtSectionBody;

    @BindView(R.id.switchMessage)
    Switch switchMessage;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_message, this);
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
                Toast.makeText(getContext(), txtSectionTitle.getText() + " selected", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setTitleBody(String title, String body) {
        txtSectionTitle.setText(title);
        txtSectionBody.setText(body);
    }

}
