package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
 * AccessBottomBarView.java
 * Created by horatiothomas on 8/19/16.
 * Component used in AccessFragment.java to create bottom bar
 */
public class AccessBottomBarView extends FrameLayout {
    public AccessBottomBarView(Context context) {
        super(context);
    }

    public AccessBottomBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccessBottomBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccessBottomBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Globals
     */

    Unbinder unbinder;

    @BindView(R.id.imgRedFb)
    ImageView imgRedFb;

    @BindView(R.id.txtAccessTry)
    TextViewFont txtAccessTry;

    /**
     * Lifecycle methods
     */

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_bottom_bar, this);
        unbinder = ButterKnife.bind(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    /**
     * Public modify view methods
     */

    public TextViewFont getTxtAccessTry() {
        return txtAccessTry;
    }

    public void setBackgroundColor(Context context, int color) {
        txtAccessTry.setBackgroundColor(ContextCompat.getColor(context, color));
    }

    public void setText(String text) {
        txtAccessTry.setText(text);
    }

    public void setImgRedFbVisibility(boolean isVisible) {
        if (isVisible) imgRedFb.setVisibility(VISIBLE);
        else imgRedFb.setVisibility(INVISIBLE);
    }

    public void setBackground(Drawable drawable) {
        txtAccessTry.setBackground(drawable);
    }
}
