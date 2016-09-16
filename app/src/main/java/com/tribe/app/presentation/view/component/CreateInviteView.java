package com.tribe.app.presentation.view.component;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/16/16.
 */
public class CreateInviteView extends FrameLayout {
    public CreateInviteView(Context context) {
        super(context);
    }

    public CreateInviteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CreateInviteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CreateInviteView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> createPressed = PublishSubject.create();
    private PublishSubject<Void> invitePressed = PublishSubject.create();



    @BindView(R.id.textCreateInvite)
    TextViewFont textCreateInvite;
    @BindView(R.id.textCreateInviteDesc)
    TextViewFont textCreateInviteDesc;
    @BindView(R.id.viewCreateGroupBg1)
    View viewCreateGroupBg1;
    @BindView(R.id.viewCreateGroupBg2)
    View viewCreateGroupBg2;
    @BindView(R.id.imageInvite)
    ImageView imageInvite;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_create_invite, this);
        unbinder = ButterKnife.bind(this);

        subscriptions.add(RxView.clicks(viewCreateGroupBg1).subscribe(aVoid -> {
//            showShareDialogFragment();
            invitePressed.onNext(null);
        }));

        subscriptions.add(RxView.clicks(viewCreateGroupBg2).subscribe(aVoid -> {
            createPressed.onNext(null);
        }));

    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

    public void setInvite(Boolean privateGroup) {
        if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
        else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
        textCreateInvite.setText(getContext().getString(R.string.group_button_share));
        textCreateInviteDesc.setText(getContext().getString(R.string.group_share_description));
    }

    public void enableInvitePress() {
        viewCreateGroupBg1.setEnabled(true);
    }

    public void switchColors(Boolean privateGroup) {
        if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
        else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
    }

    public void disable() {
        imageInvite.setScaleX(AnimationUtils.SCALE_INVISIBLE);
        imageInvite.setScaleY(AnimationUtils.SCALE_INVISIBLE);
        viewCreateGroupBg2.setEnabled(false);
        viewCreateGroupBg1.setEnabled(false);
    }

    public void enableCreate(Boolean privateGroup) {
        if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
        else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
        viewCreateGroupBg2.setEnabled(true);
    }

    public void loadingAnimation(int animDuration, ScreenUtils screenUtils, Activity activity) {
        screenUtils.hideKeyboard(activity);
        Rect rect = new Rect();
        viewCreateGroupBg2.getLocalVisibleRect(rect);
        Rect from = new Rect(rect);
        Rect to = new Rect(rect);
        from.right = 0;
        viewCreateGroupBg2.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofObject(viewCreateGroupBg2,
                "clipBounds",
                new RectEvaluator(),
                from, to);
        anim.setDuration(animDuration);
        anim.start();
        Observable.timer(animDuration, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    viewCreateGroupBg2.setVisibility(View.INVISIBLE);
                });
    }

    public void scaleInInviteImage(int animDuration) {
        AnimationUtils.scaleIn(imageInvite, animDuration);
    }

    public Observable<Void> createPressed() {
        return createPressed;
    }
    public Observable<Void> invitePressed() {
        return invitePressed;
    }

}
