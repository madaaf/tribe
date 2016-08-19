package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.AccessLockView;
import com.tribe.app.presentation.view.component.TextFriendsView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 8/18/16.
 */
public class AccessFragment extends Fragment {

    public static AccessFragment newInstance() {

        Bundle args = new Bundle();

        AccessFragment fragment = new AccessFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.txtAccessTitle)
    TextViewFont txtAccessTitle;

    @BindView(R.id.txtAccessDesc)
    TextViewFont txtAccessDesc;

    @BindView(R.id.txtAccessTry)
    TextViewFont txtAccessTry;

    @BindView(R.id.accessLockView)
    AccessLockView accessLockView;

    @BindView(R.id.textFriendsView)
    TextFriendsView textFriendsView;
    @BindView(R.id.imgRedFb)
    ImageView imgRedFb;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    Context context;

    private int viewState;
    private static final int STATE_GET_ACCESS = 0;
    private static final int STATE_HANG_TIGHT = 1;
    private static final int STATE_SORRY = 2;
    private static final int STATE_CONGRATS = 3;

    //for UI testing
    boolean tryAgain = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_access, container, false);

        // TODO: change to dynamic on open
        viewState = STATE_GET_ACCESS;

        initUi(fragmentView);

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
        context = getActivity();

        subscriptions.add(RxView.clicks(txtAccessTry).subscribe(aVoid -> {
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


    }

    private void cleanUpSorry() {
        imgRedFb.setVisibility(View.INVISIBLE);
        textFriendsView.setVisibility(View.INVISIBLE);

        setBottomMargin(txtAccessDesc, 112);

    }

    private void goToAccess() {
        viewState = STATE_GET_ACCESS;
        changeBaseView(getString(R.string.access_get_access_now),
                android.R.color.black,
                getString(R.string.access_a_friend_from_your_address_book_has_to_be_there_to_enter),
                getString(R.string.access_try_to_enter),
                R.color.blue_text);
        accessLockView.setToAccess();
    }


    private void goToHangTight() {
        viewState = STATE_HANG_TIGHT;
        changeBaseView(getString(R.string.access_hang_tight),
                android.R.color.black,
                getString(R.string.access_looking_for_your_contacts),
                getString(R.string.access_cancel),
                R.color.grey_dark);
        accessLockView.setToHangTight();

        // TODO: check if user has enough friends in app
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tryAgain) {
                    tryAgain = false;
                    goToSorry();
                }
                else goToCongrats();
            }
        }, 2000);

    }

    private void goToSorry() {
        viewState = STATE_SORRY;
        changeBaseView(getString(R.string.access_sorry_title),
                R.color.red_deep,
                getString(R.string.access_more_friends_needed, "1"),
                getString(R.string.access_try_again),
                R.color.blue_text);
        accessLockView.setToSorry();

        setBottomMargin(txtAccessDesc, 157);

        textFriendsView.setVisibility(View.VISIBLE);
        imgRedFb.setVisibility(View.VISIBLE);

    }

    private void goToCongrats() {
        viewState = STATE_CONGRATS;
        changeBaseView(getString(R.string.access_congrats_title),
                R.color.blue_text_access,
                getString(R.string.access_congrats_desc, "3"),
                getString(R.string.access_enter_now),
                R.color.blue_text);
    }

    private void goToHome() {

    }

    private void changeBaseView(String titleTxt, int titleTxtColor, String descTxt, String tryAgainTxt, int tryAgainBackTextColor) {
        txtAccessTitle.setText(titleTxt);
        txtAccessTitle.setTextColor(ContextCompat.getColor(context, titleTxtColor));
        txtAccessDesc.setText(descTxt);
        txtAccessTry.setText(tryAgainTxt);
        txtAccessTry.setBackgroundColor(ContextCompat.getColor(context, tryAgainBackTextColor));

    }

    private void setBottomMargin(View view, int margin) {
                FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        float d = context.getResources().getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams viewLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        viewLayoutParams.bottomMargin = (int) (margin * d);
        view.requestLayout();
    }




}
