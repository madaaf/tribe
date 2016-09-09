package com.tribe.app.presentation.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.FacebookView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import javax.inject.Inject;

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

    @Inject
    ScreenUtils screenUtils;

    @Inject
    Navigator navigator;

    @BindView(R.id.imgNextIcon)
    ImageView imgNextIcon;

    @BindView(R.id.profileInfoView)
    ProfileInfoView profileInfoView;

    @BindView(R.id.facebookView)
    FacebookView facebookView;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public boolean profilePictureSelected = false;
    public boolean textInfoValidated = false;

    /**
     * View Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile_info, container, false);

        initDependencyInjector();
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

    /**
     * View initialization
     */

    public void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

        subscriptions.add(RxView.clicks(imgNextIcon).subscribe(aVoid -> {
            ((IntroActivity) getActivity()).goToAccess();
        }));



        subscriptions.add(RxView.clicks(facebookView).subscribe(aVoid -> {
            if (FacebookUtils.isLoggedIn()) {
                getInfoFromFacebook();
            } else {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
            }
        }));

        subscriptions.add(profileInfoView.infoValid().subscribe(this::enableNext));


    }

    /**
     * Helper methods
     */

    public void setImgProfilePic(Bitmap bitmap) {
         profileInfoView.setImgProfilePic(bitmap);
    }

    public void getInfoFromFacebook() {
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        JSONObject jsonResponse = response.getJSONObject();
                        try {
                            Log.v("Facebook info: ", response.toString());
                            String name = jsonResponse.getString("name");
                            String username = name.replaceAll("\\s", "").toLowerCase();
                            String facebookId = jsonResponse.getString("id");
                            String profilePictureLink = "https://graph.facebook.com/" + facebookId + "/picture?type=large";
                            profileInfoView.setInfoFromFacebook(profilePictureLink, username, name);
                        } catch (JSONException e) {
                            Log.e("JSON exception:", e.toString());
                        }
                    }
                }).executeAsync();
    }







    public void enableNext(boolean enabled) {

        if (enabled) {
            imgNextIcon.setImageDrawable(getContext().getDrawable(R.drawable.picto_next_icon_black));
            imgNextIcon.setClickable(true);
        } else {
            imgNextIcon.setImageDrawable(getContext().getDrawable(R.drawable.picto_next_icon));
            imgNextIcon.setClickable(false);
        }

    }


    /**
     * Begin Dagger setup
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