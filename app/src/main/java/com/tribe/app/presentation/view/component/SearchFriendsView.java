package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.FlowLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 9/22/16.
 */
public class SearchFriendsView extends FrameLayout {

    public SearchFriendsView(Context context) {
        super(context);
    }

    public SearchFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchFriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SearchFriendsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Unbinder unbinder;

    @BindView(R.id.flowLayout)
    FlowLayout flowLayout;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_search_friends, this);
        unbinder = ButterKnife.bind(this);
        initUi();
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

    private void addImageToFlow() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_alien));
        flowLayout.addView(imageView);
    }

    private void initUi() {
        for (int i = 0; i < 10; i++) {
            addImageToFlow();
        }
    }

}
