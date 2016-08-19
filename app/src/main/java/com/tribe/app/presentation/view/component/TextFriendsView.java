package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 8/18/16.
 */
public class TextFriendsView extends FrameLayout {
    public TextFriendsView(Context context) {
        super(context);
    }

    public TextFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextFriendsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_text_friends, this);
        unbinder = ButterKnife.bind(this);

    }

}
