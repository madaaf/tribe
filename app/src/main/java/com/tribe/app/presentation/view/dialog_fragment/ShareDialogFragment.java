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

    private CompositeSubscription subscriptions = new CompositeSubscription();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.dialog_fragment_share, container, false);

        initUi(fragmentView);

        subscriptions.add(RxView.clicks(imageMessenger).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageWhatsapp).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageSnapchat).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageMessage).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageSlack).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageTelegram).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageMail).subscribe(aVoid -> {

        }));

        subscriptions.add(RxView.clicks(imageMore).subscribe(aVoid -> {

        }));

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

    }

}
