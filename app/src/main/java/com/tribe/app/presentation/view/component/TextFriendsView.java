package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.DeviceUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * TextFriendsView.java
 * Created by horatiothomas on 8/18/16.
 * Component used in AccessFragment to create the view that a user can tap on to invite their friends to Tribe.
 */
public class TextFriendsView extends FrameLayout {

    @BindView(R.id.imgShareFB)
    ImageView imgShareFB;

    @BindView(R.id.imgShareSms)
    ImageView imgShareSms;

    @BindView(R.id.imgShareWhatsapp)
    ImageView imgShareWhatsapp;

    // OBSERVABLES
    private Unbinder unbinder;
    private PublishSubject<Void> shareFB = PublishSubject.create();
    private PublishSubject<Void> shareSMS = PublishSubject.create();
    private PublishSubject<Void> shareWhatsapp = PublishSubject.create();

    // VARIABLES


    public TextFriendsView(Context context) {
        super(context);
    }

    public TextFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextFriendsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_text_friends, this);
        unbinder = ButterKnife.bind(this);

        boolean installedFB = DeviceUtils.appInstalled(getContext(), "com.facebook.orca");
        if (!installedFB) imgShareFB.setVisibility(View.GONE);

        boolean installedWhatsapp = DeviceUtils.appInstalled(getContext(), "com.whatsapp");
        if (!installedWhatsapp) imgShareWhatsapp.setVisibility(View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    @OnClick(R.id.imgShareFB)
    void shareFB() {
        shareFB.onNext(null);
    }

    @OnClick(R.id.imgShareSms)
    void shareSMS() {
        shareSMS.onNext(null);
    }

    @OnClick(R.id.imgShareWhatsapp)
    void shareWhatsapp() {
        shareWhatsapp.onNext(null);
    }

    /**
     * OBSERVABLES
     */
    public Observable<Void> onShareFB() {
        return shareFB;
    }

    public Observable<Void> onShareSMS() {
        return shareSMS;
    }

    public Observable<Void> onShareWhatsapp() {
        return shareWhatsapp;
    }
}
