package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/13/16.
 */
public class MemberPhotoView extends FrameLayout {

    public MemberPhotoView(Context context) {
        super(context);
    }

    public MemberPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @BindView(R.id.imageMember)
    ImageView imageMember;

    Unbinder unbinder;
    public CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initUi(R.layout.view_member_photo);
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


    public void initUi(int layout) {
        LayoutInflater.from(getContext()).inflate(layout, this);
        unbinder = ButterKnife.bind(this);
    }

    public void setPicture(String imageUrl) {
        Glide.with(getContext()).load(imageUrl)
                .fitCenter()
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imageMember);
    }
}
