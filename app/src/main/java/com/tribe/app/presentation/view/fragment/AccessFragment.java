package com.tribe.app.presentation.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.github.jinatonic.confetti.CommonConfetti;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.component.AccessBottomBarView;
import com.tribe.app.presentation.view.component.AccessLockView;
import com.tribe.app.presentation.view.component.TextFriendsView;
import com.tribe.app.presentation.view.dialog_fragment.GetNotifiedDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * AccessFragment.java
 * Created by horatiothomas on 8/18/16.
 * Third and final fragment in onboarding fragment view pager.
 * Responsible for making sure user has enough friends on tribe before giving them access to the app.
 * A lot of fancy UI stuff going on here.
 */
public class AccessFragment extends Fragment {

    public static AccessFragment newInstance() {

        Bundle args = new Bundle();

        AccessFragment fragment = new AccessFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Globals
     */

    @Inject
    ScreenUtils screenUtils;

    @Inject
    Navigator navigator;

    @BindView(R.id.txtAccessTitle)
    TextViewFont txtAccessTitle;

    @BindView(R.id.txtAccessDesc)
    TextViewFont txtAccessDesc;

    @BindView(R.id.bottom_bar_view)
    AccessBottomBarView accessBottomBarView;

    @BindView(R.id.accessLockView)
    AccessLockView accessLockView;

    @BindView(R.id.textFriendsView)
    TextFriendsView textFriendsView;

    @BindView(R.id.confettiLayout)
    FrameLayout confettiLayout;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    Context context;

    private int viewState;
    private static final int STATE_GET_ACCESS = 0,
            STATE_HANG_TIGHT = 1,
            STATE_SORRY = 2,
            STATE_CONGRATS = 3;

    //for UI testing
    private boolean tryAgain = true;
    private boolean isActive = false;
    private boolean isActive2 = false;

    /**
     * View Lifecycle
     */

    // TODO: fix cancel bug
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_access, container, false);

        // TODO: change to dynamic on open
        viewState = STATE_GET_ACCESS;
        initDependencyInjector();
        initUi(fragmentView);
        initLockViewSize();

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

    /**
     * Initialize Views
     */

    private void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);
        context = getActivity();

        textFriendsView.setAlpha(0);
        textFriendsView.setTranslationY(100);

        subscriptions.add(RxView.clicks(accessBottomBarView.getTxtAccessTry()).subscribe(aVoid -> {
            isActive2 = false;
            switch (viewState) {
                case STATE_GET_ACCESS:
                    goToHangTight();
                    break;
                case STATE_SORRY:
                    cleanUpSorry();
                    goToHangTight();
                    break;
                case STATE_HANG_TIGHT:
                    goToAccess();
                    break;
                case STATE_CONGRATS:
                    goToHome();
                    break;
                default:
                    goToAccess();
                    break;
            }
        }));

        subscriptions.add(RxView.clicks(textFriendsView).subscribe(aVoid -> {
            navigator.sendText("", getActivity());
        }));



    }

    private void initLockViewSize() {
        float lockViewWidthDp, pulseWidthDp, whiteCircleWidthDp;
        int lockViewWidth, pulseWidth, whiteCircleWidth;

        whiteCircleWidthDp = screenUtils.getWidthDp() / 3;
        pulseWidthDp = whiteCircleWidthDp + 50;
        lockViewWidthDp = (float) (pulseWidthDp * 1.2);

        whiteCircleWidth = screenUtils.dpToPx(whiteCircleWidthDp);
        pulseWidth = screenUtils.dpToPx(pulseWidthDp);
        lockViewWidth = screenUtils.dpToPx(lockViewWidthDp);

        FrameLayout.LayoutParams lockViewLayoutParams = (FrameLayout.LayoutParams) accessLockView.getLayoutParams();
        lockViewLayoutParams.height = lockViewWidth;
        lockViewLayoutParams.width = lockViewWidth;

        accessLockView.setLayoutParams(lockViewLayoutParams);
        accessLockView.setViewWidthHeight(whiteCircleWidth, pulseWidth);

    }

    /**
     * Navigation methods
     */

    private void goToAccess() {
        viewState = STATE_GET_ACCESS;
        isActive = true;
        fadeTextInOut();
        accessLockView.setToAccess();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .alpha(1)
                .setDuration(300)
                .translationY(0)
                .setStartDelay(0)
                .setDuration(300)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isActive) {
                            isActive = false;


                            changeBaseView(getString(R.string.onboarding_queue_title),
                                    android.R.color.black,
                                    getString(R.string.onboarding_queue_description),
                                    getString(R.string.onboarding_queue_button_title),
                                    R.drawable.shape_rect_blue_rounded_bottom);


                            accessBottomBarView.animate()
                                    .translationY(0);
                            accessBottomBarView.setClickable(true);

                        }
                    }
                });
    }

    private void goToHangTight() {
        viewState = STATE_HANG_TIGHT;
        isActive = true;

        fadeTextInOut();
        textFriendsView.animate()
                .alpha(0)
                .setDuration(100)
                .setStartDelay(0);
        textFriendsView.setTranslationY(100);
        accessLockView.setToHangTight(2);
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(300)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isActive) {
                            isActive = false;
                            isActive2 = true;
                            changeBaseView(getString(R.string.onboarding_queue_loading_title),
                                    android.R.color.black,
                                    getString(R.string.onboarding_queue_loading_description),
                                    getString(R.string.action_cancel),
                                    R.drawable.shape_rect_dark_grey_rounded_bottom);
                            accessBottomBarView.animate()
                                    .setDuration(300)
                                    .translationY(0);
                            accessBottomBarView.setClickable(true);

                            // TODO: check if user has enough friends in app

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isActive2) {
                                        isActive2 = false;
                                        if (tryAgain) {
                                            tryAgain = false;
                                            goToSorry();
                                        } else goToCongrats();

                                    }
                                }
                            }, 3000);
                        }
                    }
                }).start();
    }

    private void goToSorry() {
        viewState = STATE_SORRY;
        isActive = true;

        fadeTextInOut();
        textFriendsView.animate()
                .alpha(1)
                .setDuration(300)
                .translationY(0)
                .setStartDelay(0);
        accessLockView.setToSorry();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(300)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isActive) {
                            isActive = false;
                            changeBaseView(getString(R.string.onboarding_queue_declined_title),
                                    R.color.red_deep,
                                    getString(R.string.onboarding_queue_declined_description, "1"),
                                    getString(R.string.onboarding_queue_button_title),
                                    R.drawable.shape_rect_blue_rounded_bottom);


                            setBottomMargin(txtAccessDesc, 157);

                            accessBottomBarView.setImgRedFbVisibility(true);

                            accessBottomBarView.animate()
                                    .setDuration(300)
                                    .translationY(0);
                            accessBottomBarView.setClickable(true);

                            showGetNotifiedDialog();
                        }
                    }
                });
    }

    private void goToCongrats() {
        viewState = STATE_CONGRATS;

        CommonConfetti.rainingConfetti(confettiLayout, new int[]{ContextCompat.getColor(getContext(), R.color.confetti_1),
                ContextCompat.getColor(getContext(), R.color.confetti_2),
                ContextCompat.getColor(getContext(), R.color.confetti_3),
                ContextCompat.getColor(getContext(), R.color.confetti_4),
                ContextCompat.getColor(getContext(), R.color.confetti_5)})
                .infinite();

        isActive = true;
        fadeTextInOut();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(300)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isActive) {
                            isActive = false;

                            changeBaseView(getString(R.string.onboarding_queue_valid_title),
                                    R.color.blue_text_access,
                                    getString(R.string.onboarding_queue_valid_description, "3"),
                                    getString(R.string.onboarding_queue_valid_button_title),
                                    R.drawable.shape_rect_blue_rounded_bottom);
                            accessLockView.setToCongrats();

                            accessBottomBarView.animate()
                                    .setDuration(300)
                                    .translationY(0);
                            accessBottomBarView.setClickable(true);
                        }
                    }
                });
    }

    private void goToHome() {

    }

    /**
     * Utils
     */

    private void cleanUpSorry() {
        accessBottomBarView.setImgRedFbVisibility(false);

        setBottomMargin(txtAccessDesc, 112);

    }

    private void changeBaseView(String titleTxt, int titleTxtColor, String descTxt, String tryAgainTxt, int tryAgainBackground) {
        txtAccessTitle.setText(titleTxt);
        txtAccessTitle.setTextColor(ContextCompat.getColor(context, titleTxtColor));
        txtAccessDesc.setText(descTxt);
        accessBottomBarView.setText(tryAgainTxt);
        accessBottomBarView.setBackground(ContextCompat.getDrawable(context, tryAgainBackground));

    }

    private void setBottomMargin(View view, int margin) {
        FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        float d = context.getResources().getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams viewLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        viewLayoutParams.bottomMargin = (int) (margin * d);
        view.requestLayout();
    }

    private void showGetNotifiedDialog() {
        Observable.timer(4000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    GetNotifiedDialogFragment getNotifiedDialogFragment = GetNotifiedDialogFragment.newInstance();
                    getNotifiedDialogFragment.show(getFragmentManager(), GetNotifiedDialogFragment.class.getName());
                });

    }

    private void fadeTextInOut() {
        int txtDistance = 25;
        AnimationUtils.fadeViewInOut(txtAccessDesc, txtDistance);
        AnimationUtils.fadeViewInOut(txtAccessTitle, txtDistance);
    }



    public void fadeBigLockIn() {
        accessLockView.fadeBigLockIn();
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
