package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/13/16.
 */
public class BaseFrameLayout extends FrameLayout {

    public BaseFrameLayout(Context context) {
        super(context);
    }

    public BaseFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;
    public CompositeSubscription subscriptions = new CompositeSubscription();


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        unbinder = ButterKnife.bind(this);

    }

    public void initUi(int layout) {
        LayoutInflater.from(getContext()).inflate(layout, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

}
