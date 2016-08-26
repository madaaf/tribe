package com.tribe.app.presentation.view.fragment;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.picasso.Picasso;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.utils.FacebookUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
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
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
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
    public final static int RESULT_LOAD_IMAGE = 5;
    public final static int CAMERA_REQUEST = 6;
    public boolean profilePictureSelected = false;
    public boolean textInfoValidated = false;

    private static final String[] PERMISSIONS_CAMERA = new String[]{ Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

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

        subscriptions.add(RxView.clicks(txtOpenCameraRoll).subscribe(aVoid -> {
            RxPermissions.getInstance(getActivity())
                    .request(PERMISSIONS_CAMERA)
                    .subscribe(granted -> {
                       if (granted) getImageFromCameraRoll();
                       else Toast.makeText(getActivity(), "You must grant permissions to access your pictures", Toast.LENGTH_LONG).show();
                    });
                }));

        subscriptions.add(RxView.clicks(txtTakeASelfie).subscribe(aVoid -> {
            getImageFromCamera();
        }));

        subscriptions.add(RxView.clicks(facebookView).subscribe(aVoid -> {
            if (FacebookUtils.isLoggedIn()) {
                getInfoFromFacebook();
            } else {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
            }
        }));

        Observable.combineLatest(RxTextView.textChanges(editDisplayName).filter(charSequence -> editDisplayName.length() > 1),
                RxTextView.textChanges(editUsername).filter(charSequence -> editUsername.length() > 1),
                (charSequence1, charSequence2) -> {
                    textInfoValidated = true;
                    enableNext(profilePictureSelected);
                    return profilePictureSelected;
                }).subscribe();


        Observable.merge(RxTextView.textChanges(editDisplayName).filter(charSequence -> editDisplayName.length() < 2),
                RxTextView.textChanges(editUsername).filter(charSequence -> editUsername.length() < 2)).subscribe(charSequence1 -> {
            textInfoValidated = false;
            enableNext(false);
        });

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
        picasso.load(profilePicLink)
                .transform(new RoundedCornersTransformation(screenUtils.dpToPx(100), 0))
                .resize(screenUtils.dpToPx(65), screenUtils.dpToPx(65))
                .centerInside()
                .into(imgProfilePic);
        profilePictureSelected = true;
        editDisplayName.setText(realName);
        editUsername.setText(username);

    }

    public void getImageFromCameraRoll() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void getImageFromCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        getActivity().startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void setImgProfilePic(Bitmap bitmap) {
        imgProfilePic.setImageBitmap(bitmap);
    }

    public Bitmap formatBitmapforView(Bitmap thumbnail) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(screenUtils.dpToPx(100), 0, RoundedCornersTransformation.CornerType.ALL);
        thumbnail = ImageUtils.centerCropBitmap(thumbnail);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, screenUtils.dpToPx(65), screenUtils.dpToPx(65), false);
        thumbnail = roundedCornersTransformation.transform(thumbnail);
        return thumbnail;
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
