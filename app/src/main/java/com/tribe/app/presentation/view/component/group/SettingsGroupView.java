package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.ViewStackHelper;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class SettingsGroupView extends FrameLayout {

    @BindView(R.id.viewActionInfos)
    ActionView viewActionInfos;

    @BindView(R.id.viewActionNotifications)
    ActionView viewActionNotifications;

    @BindView(R.id.viewActionLeaveGroup)
    ActionView viewActionLeaveGroup;

    // VARIABLES
    private Membership membership;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<Void> editGroup = PublishSubject.create();
    private PublishSubject<Boolean> notificationsChange = PublishSubject.create();
    private PublishSubject<Void> leaveGroup = PublishSubject.create();

    public SettingsGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (membership == null) {
            Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

            if (serializable instanceof Membership) {
                membership = (Membership) serializable;
            }

            init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        viewActionInfos.setTitle(membership.getDisplayName());
        viewActionInfos.setImage(membership.getProfilePicture());
        viewActionNotifications.setValue(!membership.isMute());

        subscriptions.add(
                viewActionInfos.onClick()
                        .subscribe(aVoid -> editGroup.onNext(null))
        );
    }

    /**
     * OBSERVABLES
     */
    public Observable<Void> onEditGroup() {
        return editGroup;
    }
}
