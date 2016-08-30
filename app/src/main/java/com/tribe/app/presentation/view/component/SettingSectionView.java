package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 8/29/16.
 */
public class SettingSectionView extends FrameLayout {

    public SettingSectionView(Context context) {
        super(context);
    }

    public SettingSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingSectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingSectionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;

    @BindView(R.id.imgSectionIcon)
    ImageView imgSectionIcon;

    @BindView(R.id.txtSectionTitle)
    TextViewFont txtSectionTitle;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_section, this);
        unbinder = ButterKnife.bind(this);

    }

    public void setTitleIcon(int title, int icon) {
        txtSectionTitle.setText(getContext().getString(title));
        imgSectionIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), icon));
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

}
