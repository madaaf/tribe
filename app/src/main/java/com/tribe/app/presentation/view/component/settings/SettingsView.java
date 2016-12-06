package com.tribe.app.presentation.view.component.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.ScoreUtils;

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

    @BindView(R.id.viewActionPoints)
    ActionView viewActionPoints;

    @BindView(R.id.viewActionShareProfile)
    ActionView viewActionShareProfile;

    @BindView(R.id.viewActionFilters)
    ActionView viewActionFilters;

    @BindView(R.id.viewActionPermissions)
    ActionView viewActionPermissions;

    @BindView(R.id.viewActionHideBlock)
    ActionView viewActionHideBlock;

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
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

    }

    private void initUI() {
        ScoreUtils.Level level = ScoreUtils.getLevelForScore(user.getScore());
        viewActionPoints.setTitle(getContext().getString(level.getStringId()));

        viewActionShareProfile.setTitle(getContext().getString(R.string.settings_profile_share_title, "@" + user.getUsername()));
        viewActionShareProfile.setBody(getContext().getString(R.string.settings_profile_share_title, BuildConfig.TRIBE_URL + "/@" + user.getUsername()));
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

    public Observable<Void> onPointsClick() {
        return viewActionPoints.onClick();
    }

    public Observable<Void> onFiltersClick() {
        return viewActionFilters.onClick();
    }
}
