package com.tribe.app.presentation.view.component.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.tribe.app.R;
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
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 12/14/16.
 */
public class LoadFriendsView extends LinearLayout {

    public static final int FB = 0;
    public static final int ADDRESSBOOK = 1;

    @IntDef({FB, ADDRESSBOOK})
    public @interface Type{}

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtBody)
    TextViewFont txtBody;

    @Nullable
    @BindView(R.id.avatarView)
    AvatarView avatarView;

    // VARIABLES
    private Unbinder unbinder;
    private int type;
    private String imageUrl;
    private String body;
    private String title;

    // OBSERVABLES
    private PublishSubject<Void> onClick = PublishSubject.create();

    public LoadFriendsView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadFriendsView);

        setType(a.getInt(R.styleable.LoadFriendsView_loadType, FB));

        int layout = 0;

        if (type == FB) {
            layout = R.layout.view_load_friends_fb;
        } else if (type == ADDRESSBOOK) {
            layout = R.layout.view_load_friends_addressbook;
        }

        LayoutInflater.from(getContext()).inflate(layout, this);
        unbinder = ButterKnife.bind(this);

        if (a.hasValue(R.styleable.LoadFriendsView_loadTitle)) {
            setTitle(getResources().getString(a.getResourceId(R.styleable.LoadFriendsView_loadTitle, R.string.search_add_addressbook_title)));
        }

        if (a.hasValue(R.styleable.LoadFriendsView_loadBody)) {
            setBody(getResources().getString(a.getResourceId(R.styleable.LoadFriendsView_loadBody, R.string.group_details_settings_subtitle)));
        } else {
            computeBody();
        }

        a.recycle();

        setClickable(true);
        setOrientation(HORIZONTAL);
        setMinimumHeight(screenUtils.dpToPx(72));
        setOnClickListener(v -> onClick.onNext(null));
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

    private void computeImageView() {
        if (avatarView != null && !StringUtils.isEmpty(imageUrl)) {
            avatarView.load(imageUrl);
        }
    }

    // OBSERVABLES
    public Observable<Void> onClick() {
        return onClick;
    }
}
