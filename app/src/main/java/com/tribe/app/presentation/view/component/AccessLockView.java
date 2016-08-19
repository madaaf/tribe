package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 8/18/16.
 */
public class AccessLockView extends FrameLayout {
    public AccessLockView(Context context) {
        super(context);
    }

    public AccessLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccessLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccessLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_lock, this);
        unbinder = ButterKnife.bind(this);

    }

    public void setToAccess() {
//        viewState = STATE_GET_ACCESS;
    }


    public void setToHangTight() {
//        viewState = STATE_HANG_TIGHT;
    }

    public void setToSorry() {
//        viewState = STATE_SORRY;
    }

    public void setToCongrats() {
//        viewState = STATE_CONGRATS;
    }

}
