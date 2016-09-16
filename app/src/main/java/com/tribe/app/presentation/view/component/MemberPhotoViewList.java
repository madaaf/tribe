package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 9/15/16.
 */
public class MemberPhotoViewList extends FrameLayout {
    public MemberPhotoViewList(Context context) {
        super(context);
    }

    public MemberPhotoViewList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberPhotoViewList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MemberPhotoViewList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @BindView(R.id.layoutMember1)
    FrameLayout layoutMember1;
    @BindView(R.id.layoutMember2)
    FrameLayout layoutMember2;
    @BindView(R.id.layoutMember3)
    FrameLayout layoutMember3;
    @BindView(R.id.layoutMember4)
    FrameLayout layoutMember4;
    @BindView(R.id.layoutMember5)
    FrameLayout layoutMember5;

    int numMembers = 0;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUi(R.layout.view_member_photo_list);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        super.onDetachedFromWindow();
    }


    public void addMemberPhoto(String imageUrl) {

        MemberPhotoView memberPhotoView = new MemberPhotoView(getContext());
        memberPhotoView.initUi(R.layout.view_member_photo);
        memberPhotoView.setPicture(imageUrl);
        switch (numMembers) {
            case 0:
                layoutMember1.addView(memberPhotoView);
                break;
            case 1:
                layoutMember2.addView(memberPhotoView);
                break;
            case 2:
                layoutMember3.addView(memberPhotoView);
                break;
            case 3:
                layoutMember4.addView(memberPhotoView);
                break;
            case 4:
                layoutMember5.addView(memberPhotoView);
                break;

        }
        numMembers++;
    }

    public void initUi(int layout) {
        LayoutInflater.from(getContext()).inflate(layout, this);
    }

}
