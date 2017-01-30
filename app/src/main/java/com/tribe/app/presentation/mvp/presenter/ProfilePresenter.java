package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import javax.inject.Inject;

/**
 * Created by madaaflak on 28/01/2017.
 */

public class ProfilePresenter extends UpdateUserPresenter {

    private ProfileMVPView profileView;
    private final RemoveInstall removeInstall;

    @Inject
    ProfilePresenter(UpdateUser updateUser, LookupUsername lookupUsername, RxFacebook rxFacebook, RemoveInstall removeInstall) {
        super(updateUser, lookupUsername, rxFacebook);
        this.removeInstall = removeInstall;
    }

    @Override
    public void onViewDetached() {
        removeInstall.unsubscribe();
        super.onViewDetached();
    }

    @Override
    public void onViewAttached(MVPView v) {
        profileView = (ProfileMVPView) v;
    }

    public void logout() {
        removeInstall.execute(new ProfilePresenter.RemoveInstallSubscriber());
    }

    @Override
    protected UpdateUserMVPView getUpdateUserView() {
        return profileView;
    }

    private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(User user) {
            goToLauncher();
        }
    }

    public void goToLauncher() {
        this.profileView.goToLauncher();
    }
}
