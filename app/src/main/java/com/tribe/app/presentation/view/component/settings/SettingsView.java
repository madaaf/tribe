package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class SettingsView extends FrameLayout {

    @Inject
    User user;

    @Inject
    Navigator navigator;

    @BindView(R.id.viewActionProfile)
    ActionView viewActionProfile;

    @BindView(R.id.viewActionShareProfile)
    ActionView viewActionShareProfile;

    @BindView(R.id.viewActionFilters)
    ActionView viewActionFilters;

    @BindView(R.id.viewActionParameters)
    ActionView viewActionParameters;

    //@BindView(R.id.viewActionPermissions)
    //ActionView viewActionPermissions;

    //@BindView(R.id.viewActionHideBlock)
    //ActionView viewActionHideBlock;

    @BindView(R.id.viewActionContact)
    ActionView viewActionContact;

    @BindView(R.id.viewActionFollow)
    ActionView viewActionFollow;

    @BindView(R.id.viewActionBlockedHidden)
    ActionView viewActionBlockedHidden;

    @BindView(R.id.viewActionRateUs)
    ActionView viewActionRateUs;

    @BindView(R.id.viewActionLogout)
    ActionView viewActionLogout;

    @BindView(R.id.txtVersion)
    TextViewFont txtVersion;

    // VARIABLES

    // OBSERVABLES
    private CompositeSubscription subscriptions;

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        initDependencyInjector();
        initSubscriptions();
        initUI();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initUI() {
        viewActionShareProfile.setTitle(getContext().getString(R.string.settings_profile_share_title, "@" + user.getUsername()));
        viewActionShareProfile.setBody(getContext().getString(R.string.settings_profile_share_title, BuildConfig.TRIBE_URL + "/@" + user.getUsername()));

        txtVersion.setText(getContext().getString(R.string.settings_version, BuildConfig.VERSION_NAME, String.valueOf(BuildConfig.VERSION_CODE)));
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

    /**
     * OBSERVABLES
     */

    public Observable<Void> onProfileClick() {
        return viewActionProfile.onClick();
    }

    public Observable<Void> onShareProfileClick() {
        return viewActionShareProfile.onClick();
    }

    public Observable<Void> onFiltersClick() {
        return viewActionFilters.onClick();
    }

    public Observable<Void> onParametersClick() {
        return viewActionParameters.onClick();
    }

//    public Observable<Void> onPermissionsClick() {
//        return viewActionPermissions.onClick();
//    }

//    public Observable<Void> onHideBlockClick() {
//        return viewActionHideBlock.onClick();
//    }

    public Observable<Void> onContactClick() {
        return viewActionContact.onClick();
    }

    public Observable<Void> onFollowClick() {
        return viewActionFollow.onClick();
    }

    public Observable<Void> onBlockedHiddenClick() {
        return viewActionBlockedHidden.onClick();
    }

    public Observable<Void> onRateClick() {
        return viewActionRateUs.onClick();
    }

    public Observable<Void> onLogoutClick() {
        return viewActionLogout.onClick();
    }
}
