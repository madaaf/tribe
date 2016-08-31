package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 8/17/16.
 */
public class ConnectedView extends FrameLayout {
    public ConnectedView(Context context) {
        super(context);
    }

    public ConnectedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConnectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ConnectedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.txtConnected)
    TextViewFont txtConnected;

    Unbinder unbinder;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_connected, this);

        ButterKnife.bind(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        if (unbinder != null)
            unbinder.unbind();

        super.onDetachedFromWindow();
    }


    public void animateConnected() {
        txtConnected.animate()
                .translationX(-20)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        txtConnected.animate()
                                .translationX(0)
                                .setDuration(150)
                                .start();
                    }
                }).start();
    }


}
