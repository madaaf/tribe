package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.login.LoginResult;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.ProfileInfoPresenter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.FacebookView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * ProfileInfoFragment.java
 * Created by horatiothomas on 8/18/16.
 * Second fragment in onboarding process.
 * Responsible for collecting user's profile picture, name, and username.
 * Has ability to retrieve this information from Facebook.
 */
public class ProfileInfoFragment extends BaseFragment implements com.tribe.app.presentation.mvp.view.ProfileInfoView {

    private static final String LOGIN_ENTITY = "LOGIN_ENTITY";
    private static final String FACEBOOK_ENTITY = "FACEBOOK_ENTITY";
    private static final String DEEPLINK = "DEEPLINK";
    private static final String URI_PICTURE = "URI_PICTURE";

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
    RxImagePicker rxImagePicker;

    @Inject
    RxFacebook rxFacebook;

    @Inject
    User user;

    @Inject
    ProfileInfoPresenter profileInfoPresenter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    PhoneUtils phoneUtils;

    @BindView(R.id.imgNextIcon)
    ImageView imgNextIcon;

    @BindView(R.id.circularProgressProfile)
    CircularProgressView circularProgressProfile;

    @BindView(R.id.profileInfoView)
    ProfileInfoView profileInfoView;

    @BindView(R.id.facebookView)
    FacebookView facebookView;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private LoginEntity loginEntity;
    private FacebookEntity facebookEntity;
    private Uri deepLink;
    private Uri uriPicture;

    /**
     * View Lifecycle
     */

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.get(LOGIN_ENTITY) != null) loginEntity = (LoginEntity) savedInstanceState.getSerializable(LOGIN_ENTITY);
            if (savedInstanceState.get(FACEBOOK_ENTITY) != null) facebookEntity = (FacebookEntity) savedInstanceState.getSerializable(FACEBOOK_ENTITY);
            if (savedInstanceState.get(DEEPLINK) != null) deepLink = savedInstanceState.getParcelable(DEEPLINK);
            if (savedInstanceState.get(URI_PICTURE) != null) {
                uriPicture = savedInstanceState.getParcelable(URI_PICTURE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile_info, container, false);

        initDependencyInjector();
        initUi(fragmentView);

        this.profileInfoPresenter.attachView(this);
        refactorNext();

        if (getUserVisibleHint() && getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscriptions.add(Observable
                .timer(500, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    uriPicture = rxImagePicker.getUri();

                    if (uriPicture != null) {
                        profileInfoView.loadUri(uriPicture);
                    }
                }));

        if (profileInfoView != null && uriPicture != null) profileInfoView.loadUri(uriPicture);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (loginEntity != null) outState.putSerializable(LOGIN_ENTITY, loginEntity);
        if (facebookEntity != null) outState.putSerializable(FACEBOOK_ENTITY, facebookEntity);
        if (deepLink != null) outState.putParcelable(DEEPLINK, deepLink);
        if (uriPicture != null) outState.putParcelable(URI_PICTURE, uriPicture);
    }

    @Override
    public void onResume() {
        super.onResume();

        subscriptions.add(Observable
                .timer(2000, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    LoginResult loginResult = rxFacebook.getLoginResult();

                    if (loginResult != null && FacebookUtils.isLoggedIn()) {
                        successFacebookLogin();
                    }
                }));
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        if (profileInfoPresenter != null) {
            profileInfoPresenter.onDestroy();
        }

        super.onDestroy();
    }

    /**
     * View initialization
     */

    public void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

        subscriptions.add(RxView.clicks(facebookView).subscribe(aVoid -> {
            if (FacebookUtils.isLoggedIn()) {
                getInfoFromFacebook();
            } else {
                profileInfoPresenter.loginFacebook();
            }
        }));

        subscriptions.add(profileInfoView.onUsernameInput().subscribe(s -> {
            if (isResumed() && getUserVisibleHint())
                profileInfoPresenter.lookupUsername(s);
        }));

        subscriptions.add(profileInfoView.onInfoValid().subscribe(isValid -> refactorNext()));
    }

    /**
     * Helper methods
     */
    @Override
    public void successFacebookLogin() {
        getInfoFromFacebook();
    }

    @Override
    public void errorFacebookLogin() {

    }

    @Override
    public void loadFacebookInfos(FacebookEntity facebookEntity) {
        this.facebookEntity = facebookEntity;
        profileInfoView.setInfoFromFacebook(facebookEntity);
    }

    @Override
    public void usernameResult(Boolean available) {
        boolean usernameValid = available;
        profileInfoView.setUsernameValid(usernameValid || profileInfoView.getUsername().equals(user.getUsername()));
        refactorNext();
    }

    @Override
    public void userRegistered(User user) {
        this.user.copy(user);

        Bundle bundle = new Bundle();
        if (deepLink != null && !StringUtils.isEmpty(deepLink.getPath())) {
            if (deepLink.getPath().startsWith("/u/")) {
                bundle.putString(TagManagerConstants.TYPE_DEEPLINK, TagManagerConstants.TYPE_DEEPLINK_USER);
            } else if (deepLink.getPath().startsWith("/g/")) {
                bundle.putString(TagManagerConstants.TYPE_DEEPLINK, TagManagerConstants.TYPE_DEEPLINK_GROUP);
            }
        } else {
            bundle.putString(TagManagerConstants.TYPE_DEEPLINK, TagManagerConstants.TYPE_DEEPLINK_NONE);
        }

        if (facebookEntity != null && !StringUtils.isEmpty(facebookEntity.getId())) {
            tagManager.trackEvent(TagManagerConstants.ONBOARDING_REGISTRATION_FACEBOOK, bundle);
        } else {
            tagManager.trackEvent(TagManagerConstants.ONBOARDING_REGISTRATION, bundle);
        }

        tagManager.setProperty(bundle);

        profileInfoPresenter.updateUser(user.getUsername(), user.getDisplayName(), profileInfoView.getImgUri(),
                facebookEntity != null && !StringUtils.isEmpty(facebookEntity.getId()) ? facebookEntity.getId() : null);
    }

    @Override
    public void successUpdateUser(User user) {
        this.user.copy(user);
        ((IntroActivity) getActivity()).goToAccess(this.user);
    }

    public void getInfoFromFacebook() {
        profileInfoPresenter.loadFacebookInfos();
    }

    public void refactorNext() {
        if (profileInfoView.isUsernameSelected() && profileInfoView.isDisplayNameSelected() && profileInfoView.isAvatarSelected()) {
            imgNextIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.picto_next_icon_black));
        } else {
            imgNextIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.picto_next_icon));
        }
    }

    /**
     * On clicks
     */
    @OnClick(R.id.imgNextIcon)
    public void clickNext() {
        if (profileInfoView.isUsernameSelected() && profileInfoView.isDisplayNameSelected() && profileInfoView.isAvatarSelected()) {
            screenUtils.hideKeyboard(getActivity());

            if (StringUtils.isEmpty(user.getId())) {
                profileInfoPresenter.register(profileInfoView.getDisplayName(), profileInfoView.getUsername(), loginEntity);
            } else {
                tagManager.trackEvent(TagManagerConstants.ONBOARDING_CONNECTION);
                showLoading();
                profileInfoPresenter.updateUser(profileInfoView.getUsername(), profileInfoView.getDisplayName(), profileInfoView.getImgUri(),
                        facebookEntity != null && !StringUtils.isEmpty(facebookEntity.getId()) ? facebookEntity.getId() : null);
            }
        } else if (!profileInfoView.isAvatarSelected()) {
            profileInfoView.shakeAvatar();
        } else if (!profileInfoView.isDisplayNameSelected()) {
            profileInfoView.shakeDisplayName();
        } else if (!profileInfoView.isUsernameSelected()) {
            profileInfoView.shakeUsername();
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

    @Override
    public void showLoading() {
        imgNextIcon.setVisibility(View.GONE);
        circularProgressProfile.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        imgNextIcon.setVisibility(View.VISIBLE);
        circularProgressProfile.setVisibility(View.GONE);
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return getActivity();
    }

    public void setLoginEntity(LoginEntity loginEntity) {
        this.loginEntity = phoneUtils.prepareLoginForRegister(loginEntity);
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            profileInfoView.loadAvatar(user.getProfilePicture());
            profileInfoView.setEditDisplayName(user.getDisplayName());
            profileInfoView.setEditUsername(user.getUsername());
        }
    }

    public void setDeepLink(Uri deepLink) {
        this.deepLink = deepLink;
    }
}