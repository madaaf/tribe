package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 9/16/16.
 */
public class ViewPrivacyStatus extends FrameLayout {
    public ViewPrivacyStatus(Context context) {
        super(context);
    }

    public ViewPrivacyStatus(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPrivacyStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewPrivacyStatus(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @BindView(R.id.imagePrivacyStatus)
    ImageView imagePrivacyStatus;
    @BindView(R.id.textPrivacyStatus)
    TextViewFont textPrivacyStatus;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_privacy_status, this);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        super.onDetachedFromWindow();
    }

    public void setup(Boolean privateGroup, int memberCount) {
        if (privateGroup) {
            if (memberCount > 1) textPrivacyStatus.setText(getContext().getString(R.string.group_private_title) + " - " + memberCount  + " " + getContext().getString(R.string.group_members));
            else textPrivacyStatus.setText(getContext().getString(R.string.group_private_title) + " - " + memberCount  + " " + getContext().getString(R.string.group_member));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_grey));
        } else {
            if (memberCount > 1) textPrivacyStatus.setText(getContext().getString(R.string.group_public_title) + " - " + memberCount  + " " + getContext().getString(R.string.group_members));
            else textPrivacyStatus.setText(getContext().getString(R.string.group_public_title) + " - " + memberCount  + " " + getContext().getString(R.string.group_member));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        }
    }

}
