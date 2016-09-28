package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingUpdateProfileFragment extends BaseFragment {

    public static SettingUpdateProfileFragment newInstance() {

        Bundle args = new Bundle();

        SettingUpdateProfileFragment fragment = new SettingUpdateProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.profileInfoView)
    ProfileInfoView profileInfoView;

    // VARIABLES
    private Unbinder unbinder;
    private User user;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> usernameSearch = PublishSubject.create();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_setting_update_profile, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);

        initUi();

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initUi() {
        user = getCurrentUser();

        try {
            profileInfoView.loadAvatar(user.getProfilePicture());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        profileInfoView.setEditDisplayName(user.getDisplayName());
        profileInfoView.setEditUsername(user.getUsername());

        subscriptions.add(profileInfoView.onInfoValid().subscribe(isValid -> {
            ((SettingActivity) getActivity()).setImgDoneEnabled(isValid);
        }));

        subscriptions.add(profileInfoView.onUsernameInput().subscribe(s -> {
            usernameSearch.onNext(s);
        }));
    }

    public String getImgUri() {
        return profileInfoView.getImgUri();
    }

    public String getUsername() {
        return profileInfoView.getUsername();
    }

    public String getDisplayName() {
        return profileInfoView.getDisplayName();
    }

    public void setUsernameValid(boolean valid) {
        profileInfoView.setUsernameValid(valid);
    }

    /**
     * OBSERVABLES
     */
    public Observable<String> onUsernameSearch() {
        return usernameSearch;
    }
}
