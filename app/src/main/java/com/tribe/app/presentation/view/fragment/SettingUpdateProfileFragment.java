package com.tribe.app.presentation.view.fragment;

import android.graphics.Bitmap;
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

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Unbinder unbinder;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_setting_update_profile, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);

        initUi();

        return fragmentView;
    }

    private void initUi() {
        user = getCurrentUser();

        try {
            profileInfoView.setUrlProfilePic(user.getProfilePicture());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        profileInfoView.setEditDisplayName(user.getDisplayName());
        profileInfoView.setEditUsername(user.getUsername());

        subscriptions.add(profileInfoView.infoValid().subscribe(isValid -> {
            ((SettingActivity) getActivity()).setImgDoneEnabled(isValid);
        }));

    }

    public void setImgProfilePic(Bitmap bitmap) {
        profileInfoView.setImgProfilePic(bitmap);
    }

    public String getUsername() {
        return profileInfoView.getUsername();
    }

    public String getDisplayName() {
        return profileInfoView.getDisplayName();
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

}
