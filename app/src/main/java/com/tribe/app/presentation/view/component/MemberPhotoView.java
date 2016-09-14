package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;

/**
 * Created by horatiothomas on 9/13/16.
 */
public class MemberPhotoView extends BaseFrameLayout {

    public MemberPhotoView(Context context) {
        super(context);
    }

    public MemberPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MemberPhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUi(R.layout.dialog_fragment_get_notified);
    }


    @Override
    public void initUi(int layout) {
        super.initUi(layout);
    }
}
