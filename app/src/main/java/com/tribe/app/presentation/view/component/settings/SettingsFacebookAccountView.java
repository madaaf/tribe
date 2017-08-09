package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by remy on 31/07/2017.
 */

public class SettingsFacebookAccountView extends RelativeLayout {

    @Inject
    Navigator navigator;

    @BindView(R.id.viewAvatar) AvatarView viewAvatar;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtStatus) TextViewFont txtStatus;
    @BindView(R.id.viewSwitch) SwitchCompat viewSwitch;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<Boolean> onChecked = PublishSubject.create();

    public SettingsFacebookAccountView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        initDependencyInjector();
        initSubscriptions();

        viewSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onChecked.onNext(isChecked));
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    public void recheck() {
        viewSwitch.setChecked(true);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    public void reloadUserUI(FacebookEntity facebookEntity) {

        if (facebookEntity != null) {
            viewAvatar.setType(AvatarView.FACEBOOK);
            viewAvatar.load(facebookEntity.getProfilePicture());

            txtName.setText(facebookEntity.getName());
            txtStatus.setText(R.string.profile_facebook_account_status_connected);

        } else {
            txtStatus.setText(R.string.profile_facebook_account_status_disconnected);
        }
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
                .build()
                .inject(this);
    }

    // OBSERVABLES

    public Observable<Boolean> onChecked() {
        return onChecked;
    }
}
