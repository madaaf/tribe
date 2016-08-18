package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;

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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_connected, this);

    }
}
