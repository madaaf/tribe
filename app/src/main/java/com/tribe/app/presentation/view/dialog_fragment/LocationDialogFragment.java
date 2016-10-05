package com.tribe.app.presentation.view.dialog_fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.PulsatorLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/16.
 */
public class LocationDialogFragment extends BaseDialogFragment {

    public static LocationDialogFragment newInstance() {
        Bundle args = new Bundle();
        LocationDialogFragment fragment = new LocationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.pulsatorLayout)
    PulsatorLayout pulsatorLayout;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.txtPopupTitle)
    TextViewFont txtPopupTitle;

    @BindView(R.id.txtPopupDesc)
    TextViewFont txtPopupDesc;

    @BindView(R.id.txtPermissionType)
    TextViewFont txtPermissionType;

    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    @BindView(R.id.txtNo)
    TextViewFont txtNo;

    @BindView(R.id.txtYes)
    TextViewFont txtYes;

    @Inject
    Navigator navigator;

    @Inject
    User user;

    // VARIABLES

    // RESOURCES
    private int avatarSize;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.dialog_fragment_location, container, false);

        initResources();
        initDependencyInjector();
        initUi(fragmentView);

        return fragmentView;
    }

    @Override
    public void removeSubscriptions() {
        super.removeSubscriptions();

        if (subscriptions.hasSubscriptions() && subscriptions != null) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    public void initResources() {
        avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size_onboarding);
    }

    @Override
    public void initUi(View view) {
        super.initUi(view);

        pulsatorLayout.start();

        subscriptions.add(RxView.clicks(txtYes).subscribe(aVoid -> {
            //navigator.shareHandle(user.getUsername(), getActivity());
            dismiss();
        }));

        subscriptions.add(RxView.clicks(txtNo).subscribe(aVoid -> {
            dismiss();
        }));

        if (!StringUtils.isEmpty(user.getProfilePicture())) {
            Glide.with(getContext()).load(user.getProfilePicture())
                    .override(avatarSize, avatarSize)
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .crossFade()
                    .into(imgAvatar);
        }

        txtPoints.setText(ScoreUtils.formatFloatingPoint(getContext(), ScoreUtils.Point.LOCATION.getPoints()) + " pts");
    }

    /**
     * Dagger setup
     */
    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}
