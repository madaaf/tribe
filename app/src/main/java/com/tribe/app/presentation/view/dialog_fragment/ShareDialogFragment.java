package com.tribe.app.presentation.view.dialog_fragment;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/12/16.
 */
public class ShareDialogFragment extends DialogFragment {

    public static ShareDialogFragment newInstance() {
        Bundle args = new Bundle();
        ShareDialogFragment fragment = new ShareDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    Unbinder unbinder;

    @BindView(R.id.imageMessenger)
    ImageView imageMessenger;
    @BindView(R.id.imageWhatsapp)
    ImageView imageWhatsapp;
    @BindView(R.id.imageSnapchat)
    ImageView imageSnapchat;
    @BindView(R.id.imageMessage)
    ImageView imageMessage;
    @BindView(R.id.imageSlack)
    ImageView imageSlack;
    @BindView(R.id.imageTelegram)
    ImageView imageTelegram;
    @BindView(R.id.imageMail)
    ImageView imageMail;
    @BindView(R.id.imageMore)
    ImageView imageMore;

    @BindView(R.id.textDone)
    TextViewFont textDone;

    @Inject
    Navigator navigator;

    private CompositeSubscription subscriptions = new CompositeSubscription();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.dialog_fragment_share, container, false);

        initUi(fragmentView);
        initDependencyInjector();

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

        String shareMessage = "test";

        subscriptions.add(RxView.clicks(imageMessenger).subscribe(aVoid -> {
            navigator.openFacebookMessenger(shareMessage, getActivity());
        }));
        subscriptions.add(RxView.clicks(imageWhatsapp).subscribe(aVoid -> {
            navigator.openWhatsApp(shareMessage, getActivity());
        }));
        subscriptions.add(RxView.clicks(imageSnapchat).subscribe(aVoid -> {
            navigator.openSnapchat(shareMessage, getActivity());
        }));
        subscriptions.add(RxView.clicks(imageMessage).subscribe(aVoid -> {
            navigator.sendText(shareMessage, getActivity());
        }));
        subscriptions.add(RxView.clicks(imageSlack).subscribe(aVoid -> {

        }));
        subscriptions.add(RxView.clicks(imageTelegram).subscribe(aVoid -> {
            navigator.openTelegram(shareMessage, getActivity());
        }));

        subscriptions.add(RxView.clicks(imageMail).subscribe(aVoid -> {
            navigator.composeEmail(getActivity(), null, "");
        }));

        subscriptions.add(RxView.clicks(imageMore).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(textDone).subscribe(aVoid -> {
            dismiss();
        }));

    }

    /**
     * Dagger setup
     */

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

}
