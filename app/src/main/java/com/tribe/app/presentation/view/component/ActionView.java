package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/21/16.
 */
public class ActionView extends FrameLayout {

    @IntDef({ HIERARCHY, HIERARCHY_WITH_IMAGE, SHARING, TOGGLE, CRITICAL })
    public @interface ActionViewType {
    }

    public static final int HIERARCHY = 0;
    public static final int HIERARCHY_WITH_IMAGE = 1;
    public static final int SHARING = 2;
    public static final int TOGGLE = 3;
    public static final int CRITICAL = 4;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtBody)
    TextViewFont txtBody;

    @Nullable
    @BindView(R.id.imageView)
    ImageView imageView;

    @Nullable
    @BindView(R.id.viewSwitch)
    SwitchCompat viewSwitch;

    // VARIABLES
    private Unbinder unbinder;
    private int type;
    private String imageUrl;
    private String body;
    private String title;

    // OBSERVABLES
    private PublishSubject<Void> onClick = PublishSubject.create();
    private PublishSubject<Boolean> onChecked = PublishSubject.create();

    public ActionView(Context context) {
        super(context);
        init(context, null);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionView);

        setType(a.getInt(R.styleable.ActionView_actionType, HIERARCHY));

        int layout = 0;

        if (type == HIERARCHY) {
            layout = R.layout.view_action_hierarchy;
        } else if (type == HIERARCHY_WITH_IMAGE) {
            layout = R.layout.view_action_hierarchy_with_image;
        } else if (type == SHARING) {
            layout = R.layout.view_action_share;
        } else if (type == TOGGLE) {
            layout = R.layout.view_action_toggle;
        } else if (type == CRITICAL) {
            layout = R.layout.view_action_critical;
        }

        LayoutInflater.from(getContext()).inflate(layout, this);
        unbinder = ButterKnife.bind(this);

        if (a.hasValue(R.styleable.ActionView_actionTitle)) {
            setTitle(getResources().getString(a.getResourceId(R.styleable.ActionView_actionTitle, R.string.group_details_settings_title)));
        }

        if (a.hasValue(R.styleable.ActionView_actionBody)) {
            setBody(getResources().getString(a.getResourceId(R.styleable.ActionView_actionBody, R.string.group_details_settings_subtitle)));
        } else {
            computeBody();
        }

        a.recycle();

        int paddingStart = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        int paddingEnd = getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
        setPadding(paddingStart, paddingStart, paddingEnd, paddingStart);

        setClickable(true);
        setForeground(ContextCompat.getDrawable(context, R.drawable.selectable_button));
        setMinimumHeight(screenUtils.dpToPx(72));
        setOnClickListener(v -> onClick.onNext(null));

        if (type == HIERARCHY_WITH_IMAGE) computeImageView();
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTitle(String str) {
        title = str;
        computeTitle();
    }

    public void setBody(String str) {
        body = str;
        computeBody();
    }

    public void setImage(String url) {
        imageUrl = url;
        computeImageView();
    }

    public void setValue(boolean value) {
        viewSwitch.setChecked(value);
        viewSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onChecked.onNext(isChecked));
    }

    private void computeTitle() {
        if (txtTitle != null && !StringUtils.isEmpty(title)) {
            txtTitle.setText(title);

            if (type == CRITICAL) {
                TextViewCompat.setTextAppearance(txtTitle, R.style.Title_1_Red);
            }
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

    private void computeImageView() {
        if (imageView != null && !StringUtils.isEmpty(imageUrl)) {
            Glide.with(getContext()).load(imageUrl)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .crossFade()
                    .into(imageView);
        }
    }

    // OBSERVABLES
    public Observable<Void> onClick() {
        return onClick;
    }

    public Observable<Boolean> onChecked() { return onChecked; }
}
