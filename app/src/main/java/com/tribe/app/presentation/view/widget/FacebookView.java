package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;

/**
 * Created by horatiothomas on 8/17/16.
 */
public class FacebookView  extends FrameLayout {

    public FacebookView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_facebook, this);
    }
}

