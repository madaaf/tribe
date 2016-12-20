package com.tribe.app.presentation.view.component.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 12/20/16.
 */
public class PickAllView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    @Inject
    User user;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtBody)
    TextViewFont txtBody;

    @BindView(R.id.viewAvatar)
    AvatarView viewAvatar;

    @BindView(R.id.viewAvatarBis)
    AvatarView viewAvatarBis;

    @BindView(R.id.layoutBG)
    ViewGroup layoutBG;

    // VARIABLES
    private Unbinder unbinder;
    private String body;
    private String title;
    private String imageUrl;
    private String imageUrlBis;

    public PickAllView(Context context) {
        super(context);
        init(context, null);
    }

    public PickAllView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        initDependencyInjector();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PickAllView);

        int layout = R.layout.view_pick_all;

        LayoutInflater.from(getContext()).inflate(layout, this);
        unbinder = ButterKnife.bind(this);

        if (a.hasValue(R.styleable.PickAllView_pickAllTitle)) {
            setTitle(getResources().getString(a.getResourceId(R.styleable.PickAllView_pickAllTitle, R.string.search_add_addressbook_title)));
        }

        if (a.hasValue(R.styleable.PickAllView_pickAllBody)) {
            setBody(getResources().getString(a.getResourceId(R.styleable.PickAllView_pickAllBody, R.string.search_add_addressbook_subtitle)));
        } else {
            computeBody();
        }

        if (a.hasValue(R.styleable.PickAllView_pickAllBGColor))
            setLayoutBGColor(a.getColor(R.styleable.PickAllView_pickAllBGColor, Color.BLACK));

        a.recycle();

        setMinimumHeight(screenUtils.dpToPx(72.5f));
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(((Activity) getContext()));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    public void setTitle(String str) {
        title = str;
        computeTitle();
    }

    public void setBody(String str) {
        body = str;
        computeBody();
    }

    public void setAvatars(String url, String urlBis) {
        this.imageUrl = url;
        this.imageUrlBis = urlBis;
        computeAvatars();
    }

    public void setLayoutBGColor(int colorId) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(colorId);
        layoutBG.setBackground(shape);
        setBackgroundColor(colorId);
    }

    private void computeTitle() {
        if (txtTitle != null && !StringUtils.isEmpty(title)) {
            txtTitle.setText(title);
        }
    }

    private void computeBody() {
        if (txtBody != null && !StringUtils.isEmpty(body)) {
            txtBody.setVisibility(View.VISIBLE);
            txtBody.setText(body);
        } else {
            txtBody.setVisibility(View.GONE);
        }
    }

    private void computeAvatars() {
        if (viewAvatar != null && !StringUtils.isEmpty(imageUrl)) {
            viewAvatar.load(imageUrl);
        }

        if (viewAvatarBis != null && !StringUtils.isEmpty(imageUrlBis)) {
            viewAvatarBis.load(imageUrlBis);
        }
    }
}
