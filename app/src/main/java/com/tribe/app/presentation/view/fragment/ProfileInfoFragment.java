package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.camera.gles.Texture;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.FacebookView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * ProfileInfoFragment.java
 * Created by horatiothomas on 8/18/16.
 * Second fragment in onboarding process.
 * Responsible for collecting user's profile picture, name, and username.
 * Has ability to retrieve this information from Facebook.
 */
public class ProfileInfoFragment extends Fragment {

    public static ProfileInfoFragment newInstance() {

        Bundle args = new Bundle();

        ProfileInfoFragment fragment = new ProfileInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Globals
     */

    @BindView(R.id.imgProfilePic)
    ImageView imgProfilePic;

    @BindView(R.id.txtOpenCameraRoll)
    TextViewFont txtOpenCameraRoll;

    @BindView(R.id.txtTakeASelfie)
    TextViewFont txtTakeASelfie;

    @BindView(R.id.editDisplayName)
    EditTextFont editDisplayName;

    @BindView(R.id.editUsername)
    EditTextFont editUsername;

    @BindView(R.id.imgNextIcon)
    ImageView imgNextIcon;

    @BindView(R.id.facebookView)
    FacebookView facebookView;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * View Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile_info, container, false);

        initUi(fragmentView);

        return  fragmentView;
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
     * View initialization
     */

    public void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

        subscriptions.add(RxView.clicks(imgNextIcon).subscribe(aVoid -> {
            ((IntroActivity) getActivity()).goToAccess();
        }));

        subscriptions.add(RxView.clicks(facebookView).subscribe(aVoid -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }));

    }


}
