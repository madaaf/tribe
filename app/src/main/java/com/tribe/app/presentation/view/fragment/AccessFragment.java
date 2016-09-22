package com.tribe.app.presentation.view.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.jinatonic.confetti.CommonConfetti;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.AccessPresenter;
import com.tribe.app.presentation.mvp.view.AccessView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.AccessBottomBarView;
import com.tribe.app.presentation.view.component.AccessLockView;
import com.tribe.app.presentation.view.component.TextFriendsView;
import com.tribe.app.presentation.view.dialog_fragment.GetNotifiedDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
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
public class AccessFragment extends Fragment implements AccessView {

    private static final int DURATION = 300;
    private static final int DURATION_SMALL = 175;
    private static final int DURATION_SHORT = 100;

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

    @Inject
    User currentUser;

    @Inject
    AccessPresenter accessPresenter;

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


    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription lookupSubscription;

    // VARIABLES
    private int numFriends = 0;
    private int viewState;
    private static final int STATE_GET_ACCESS = 0,
            STATE_HANG_TIGHT = 1,
            STATE_SORRY = 2,
            STATE_CONGRATS = 3;

    // RESOURCES
    private int totalTimeSynchro;

    /**
     * View Lifecycle
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_access, container, false);

        // TODO: change to dynamic on open
        viewState = STATE_GET_ACCESS;
        initDependencyInjector();
        initUi(fragmentView);
        initResources();
        initLockViewSize();

        accessPresenter.attachView(this);

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

        textFriendsView.setAlpha(0);
        textFriendsView.setTranslationY(screenUtils.dpToPx(100));

        subscriptions.add(RxView.clicks(accessBottomBarView.getTxtAccessTry()).subscribe(aVoid -> {
            switch (viewState) {
                case STATE_GET_ACCESS:
                    requestPermissions();
                    break;
                case STATE_SORRY:
                    cleanUpSorry();
                    requestPermissions();
                    break;
                case STATE_HANG_TIGHT:
                    numFriends = 0;
                    accessPresenter.cancelLookupContacts();
                    if (lookupSubscription != null) {
                        lookupSubscription.unsubscribe();
                        lookupSubscription = null;
                    }
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

        subscriptions.add(
                textFriendsView
                        .onShareFB()
                        .subscribe(aVoid -> navigator.openFacebookMessenger(getString(R.string.share_onboarding), getContext())));

        subscriptions.add(
                textFriendsView
                        .onShareWhatsapp()
                        .subscribe(aVoid -> navigator.openWhatsApp(getString(R.string.share_onboarding), getContext())));

        subscriptions.add(
                textFriendsView
                        .onShareSMS()
                        .subscribe(aVoid -> navigator.sendText(getString(R.string.share_onboarding), getContext())));
    }

    private void initResources() {
        totalTimeSynchro = getResources().getInteger(R.integer.time_synchro);
    }

    private void initLockViewSize() {
        float lockViewWidthDp, pulseWidthDp, whiteCircleWidthDp;
        int lockViewWidth, pulseWidth, whiteCircleWidth;

        whiteCircleWidthDp = screenUtils.getWidthDp() * 0.4f;
        pulseWidthDp = whiteCircleWidthDp + screenUtils.dpToPx(20);
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

    public void setUser(User user) {
        this.currentUser = user;
    }

    /**
     * Navigation methods
     */

    private void goToAccess() {
        viewState = STATE_GET_ACCESS;

        fadeTextInOut();

        textFriendsView.animate()
                .alpha(0)
                .translationY(screenUtils.dpToPx(100))
                .setDuration(DURATION_SHORT)
                .setStartDelay(0);

        accessLockView.setToAccess();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .alpha(1)
                .translationY(0)
                .setStartDelay(0)
                .setDuration(DURATION)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changeBaseView(getString(R.string.onboarding_queue_title),
                                android.R.color.black,
                                getString(R.string.onboarding_queue_description),
                                getString(R.string.onboarding_queue_button_title),
                                R.drawable.shape_rect_blue_rounded_bottom);

                        accessBottomBarView.animate()
                                .setListener(null)
                                .translationY(0);

                        accessBottomBarView.setClickable(true);
                    }
                });
    }

    private void goToHangTight() {
        if (RxPermissions.getInstance(getContext()).isGranted(Manifest.permission.READ_CONTACTS)) {
            accessPresenter.lookupContacts();
        } else {
            renderFriendList(new ArrayList<>());
        }

        viewState = STATE_HANG_TIGHT;

        fadeTextInOut();
        textFriendsView.animate()
                .alpha(0)
                .translationY(screenUtils.dpToPx(100))
                .setDuration(DURATION_SHORT)
                .setStartDelay(0);

        accessLockView.setToHangTight(0);
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(DURATION)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changeBaseView(getString(R.string.onboarding_queue_loading_title),
                                android.R.color.black,
                                getString(R.string.onboarding_queue_loading_description),
                                getString(R.string.action_cancel),
                                R.drawable.shape_rect_dark_grey_rounded_bottom);
                        accessBottomBarView.animate()
                                .setDuration(DURATION)
                                .setListener(null)
                                .translationY(0);
                        accessBottomBarView.setClickable(true);
                    }
                }).start();
    }

    private void goToSorry() {
        viewState = STATE_SORRY;

        accessLockView.setToSorry();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(DURATION)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textFriendsView.animate()
                                .alpha(1)
                                .setDuration(DURATION)
                                .translationY(0)
                                .setStartDelay(0);

                        AnimationUtils.animateBottomMargin(accessLockView, screenUtils.dpToPx(80), DURATION);
                        AnimationUtils.animateBottomMargin(txtAccessDesc, screenUtils.dpToPx(170), DURATION);

                        changeBaseView(getString(R.string.onboarding_queue_declined_title),
                                R.color.red_deep,
                                getString(R.string.onboarding_queue_declined_description),
                                getString(R.string.onboarding_queue_button_title),
                                R.drawable.shape_rect_blue_rounded_bottom);

                        accessBottomBarView.animate()
                                .setDuration(DURATION)
                                .setListener(null)
                                .translationY(0)
                                .start();

                        accessBottomBarView.setClickable(true);
                    }
                });
    }

    private void goToCongrats() {
        viewState = STATE_CONGRATS;

        CommonConfetti.rainingConfetti(confettiLayout, new int[] {
                ContextCompat.getColor(getContext(), R.color.confetti_1),
                ContextCompat.getColor(getContext(), R.color.confetti_2),
                ContextCompat.getColor(getContext(), R.color.confetti_3),
                ContextCompat.getColor(getContext(), R.color.confetti_4),
                ContextCompat.getColor(getContext(), R.color.confetti_5)
        })
        .infinite();

        fadeTextInOut();
        accessBottomBarView.setClickable(false);
        accessBottomBarView.animate()
                .setDuration(DURATION)
                .translationY(accessBottomBarView.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changeBaseView(getString(R.string.onboarding_queue_valid_title),
                                R.color.blue_text_access,
                                getString(R.string.onboarding_queue_valid_description),
                                getString(R.string.onboarding_queue_valid_button_title),
                                R.drawable.shape_rect_blue_rounded_bottom);
                        accessLockView.setToCongrats();

                        accessBottomBarView.animate()
                                .setDuration(DURATION)
                                .translationY(0)
                                .setListener(null);
                        accessBottomBarView.setClickable(true);
                    }
                });
    }

    private void requestPermissions() {
        RxPermissions.getInstance(getContext())
                .request(Manifest.permission.READ_CONTACTS)
                .subscribe(hasPermission -> {
                    if (hasPermission) {
                        goToHangTight();
                    }
//                    else if (currentUser.getFriendshipList().size() > 0) {
//                        goToHangTight();
//                    }
                    else {
                        goToSorry();
                    }
                });
    }

    private void goToHome() {
        navigator.navigateToHome(getActivity(), false);
    }

    /**
     * Utils
     */

    private void cleanUpSorry() {
        AnimationUtils.animateBottomMargin(accessLockView, screenUtils.dpToPx(50), DURATION);
        AnimationUtils.animateBottomMargin(txtAccessDesc, screenUtils.dpToPx(112), DURATION);

        textFriendsView.animate()
                .alpha(0)
                .translationY(screenUtils.dpToPx(100))
                .setDuration(DURATION_SHORT)
                .setStartDelay(0);
    }

    private void changeBaseView(String titleTxt, int titleTxtColor, String descTxt, String tryAgainTxt, int tryAgainBackground) {
        txtAccessTitle.setText(titleTxt);
        txtAccessTitle.setTextColor(ContextCompat.getColor(getContext(), titleTxtColor));
        txtAccessDesc.setText(descTxt);
        accessBottomBarView.setText(tryAgainTxt);
        accessBottomBarView.setBackground(ContextCompat.getDrawable(getContext(), tryAgainBackground));
    }

    private void showGetNotifiedDialog() {
        Observable.timer(4000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    GetNotifiedDialogFragment getNotifiedDialogFragment = GetNotifiedDialogFragment.newInstance();
                    getNotifiedDialogFragment.show(getFragmentManager(), GetNotifiedDialogFragment.class.getName());
                });

    }

    private void fadeTextInOut() {
        int txtDistance = screenUtils.dpToPx(25);
        AnimationUtils.fadeViewInOut(txtAccessDesc, txtDistance);
        AnimationUtils.fadeViewInOut(txtAccessTitle, txtDistance);
    }

    public void fadeBigLockIn() {
        accessLockView.fadeBigLockIn();
    }

    /**
     * AccessView methods
     */

    @Override
    public void renderFriendList(List<User> userList) {
        Map<String, Object> relationsInApp = new HashMap<>();

        for (User user : userList) {
            if (!user.isInvisibleMode()) relationsInApp.put(user.getId(), user);
        }

        if (currentUser.getFriendships() != null) {
            for (Friendship fr : currentUser.getFriendships()) {
                if (!relationsInApp.containsKey(fr.getFriend().getId())) {
                    relationsInApp.put(fr.getFriend().getId(), fr.getFriend());
                }
            }
        }

        if (currentUser.getGroupList() != null) {
            for (Group group : currentUser.getGroupList()) {
                if (!relationsInApp.containsKey(group.getId())) {
                    relationsInApp.put(group.getId(), group);
                }
            }
        }

        if (relationsInApp.values() != null && relationsInApp.values().size() > 0) {
            accessLockView.animateProgress();

            lookupSubscription =
                    Observable.zip(
                            Observable.from(relationsInApp.values()),
                            Observable.interval(0, totalTimeSynchro / relationsInApp.values().size(), TimeUnit.MILLISECONDS),
                            (contact, aLong) -> contact
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(relation -> {
                        numFriends++;
                        accessLockView.setToHangTight(numFriends);

                        if (numFriends == relationsInApp.values().size()) {
                            subscriptions.add(Observable.timer(750, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(time -> {
                                        goToCongrats();
                                    }));
                        }
                    });
        } else {
            goToSorry();
        }
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
