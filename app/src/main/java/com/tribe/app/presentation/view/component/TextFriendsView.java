package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * TextFriendsView.java
 * Created by horatiothomas on 8/18/16.
 * Component used in AccessFragment to create the view that a user can tap on to invite their friends to Tribe.
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

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

}
