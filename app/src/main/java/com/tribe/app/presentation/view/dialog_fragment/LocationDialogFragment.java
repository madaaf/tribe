package com.tribe.app.presentation.view.dialog_fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.PulsatorLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.subjects.PublishSubject;
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

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    // VARIABLES

    // RESOURCES
    private int avatarSize;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> onClickYes = PublishSubject.create();
    private PublishSubject<Void> onClickNo = PublishSubject.create();

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
            subscriptions.add(RxPermissions.getInstance(getActivity())
                    .request(PermissionUtils.PERMISSIONS_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            subscriptions.add(reactiveLocationProvider
                                    .getLastKnownLocation().subscribe(locationProvided -> {
                                        if (locationProvided != null) {
                                            Location location = new Location(locationProvided.getLongitude(), locationProvided.getLatitude());
                                            location.setLatitude(location.getLatitude());
                                            location.setLongitude(location.getLongitude());
                                            location.setHasLocation(true);
                                            location.setId(user.getId());
                                            user.setLocation(location);
                                        } else {
                                            user.setLocation(null);
                                        }
                                    }));
                        }
                    }));

            onClickYes.onNext(null);
            onClickYes.onCompleted();
            dismiss();
        }));

        subscriptions.add(RxView.clicks(txtNo).subscribe(aVoid -> {
            onClickNo.onNext(null);
            onClickNo.onCompleted();
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

    public Observable<Void> onClickYes() {
        return onClickYes;
    }

    public Observable<Void> onClickNo() {
        return onClickNo;
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
