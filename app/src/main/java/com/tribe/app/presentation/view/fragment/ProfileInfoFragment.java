package com.tribe.app.presentation.view.fragment;

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
import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.utils.FacebookUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.FacebookView;
import com.tribe.app.presentation.view.widget.TextViewFont;

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
    Picasso picasso;

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

        initDependencyInjector();
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
            if (FacebookUtils.isLoggedIn()) {
                getInfoFromFacebook();
            } else {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
            }
        }));

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
                            setInfoFromFacebook(profilePictureLink, username, name);
                        } catch (JSONException e) {
                            Log.e("JSON exception:", e.toString());
                        }
                    }
                }).executeAsync();
    }

    public void setInfoFromFacebook(String profilePicLink, String username, String realName) {
        Log.v("profile pic link", profilePicLink);
        picasso.load(profilePicLink)
                .transform(new RoundedCornersTransformation(screenUtils.dpToPx(100), 0))
                .resize(screenUtils.dpToPx(65), screenUtils.dpToPx(65))
                .centerInside()
                .into(imgProfilePic);

        editDisplayName.setText(realName);
        editUsername.setText(username);

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
