package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tribe.app.presentation.view.component.SettingItemView.SWITCH;

/**
 * Created by tiago on 11/21/16.
 */
public class ActionView extends FrameLayout {

    @IntDef({ HIERARCHY, SHARING, TOGGLE })
    public @interface ActionViewType {
    }

    public static final int HIERARCHY = 0;
    public static final int SHARING = 1;
    public static final int TOGGLE = 2;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtBody)
    TextViewFont txtBody;

    // VARIABLES
    private Unbinder unbinder;
    private int type;

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
        if (unbinder != null) unbinder.unbind();
        super.onDetachedFromWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionView);

        setType(a.getInt(R.styleable.ActionView_actionType, HIERARCHY));

        int layout = 0;

        if (type == HIERARCHY) {
            layout = R.layout.view_action_hierarchy;
        } else if (type == SHARING) {
            layout = R.layout.view_action_share;
        } else if (type == SWITCH) {
            //layout = R.layout.
        }

        LayoutInflater.from(getContext()).inflate(layout, this);
        unbinder = ButterKnife.bind(this);

        if (a.hasValue(R.styleable.ActionView_actionTitle)) {
            setTitle(getResources().getString(a.getResourceId(R.styleable.ActionView_actionTitle, R.string.group_details_settings_title)));
        }

        if (a.hasValue(R.styleable.ActionView_actionBody)) {
            setBody(getResources().getString(a.getResourceId(R.styleable.ActionView_actionBody, R.string.group_details_settings_subtitle)));
        }

        a.recycle();

        int paddingStart = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        int paddingEnd = getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
        setPadding(paddingStart, 0, paddingEnd, 0);

        setClickable(true);
        setForeground(ContextCompat.getDrawable(context, R.drawable.selectable_button));
        setMinimumHeight(screenUtils.dpToPx(72));
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTitle(String str) {
        txtTitle.setText(str);
    }

    public void setBody(String str) {
        txtBody.setText(str);
    }
}
