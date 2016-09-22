package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import android.util.Pair;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.UpdateUserView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 09/20/16.
 */
public abstract class UpdateUserPresenter implements Presenter {

    protected final UpdateUser updateUser;
    protected final RxFacebook rxFacebook;
    protected final Context context;

    UpdateUserPresenter(UpdateUser updateUser, RxFacebook rxFacebook, Context context) {
        this.context = context;
        this.updateUser = updateUser;
        this.rxFacebook = rxFacebook;
    }

    @Override
    public void onDestroy() {
        updateUser.unsubscribe();
    }

    public void updateUser(String username, String displayName, String pictureUri) {
        List<Pair<String, String>> values = new ArrayList<>();
        values.add(new Pair<>(UserRealm.DISPLAY_NAME, displayName));
        values.add(new Pair<>(UserRealm.USERNAME, username));
        if (!StringUtils.isEmpty(pictureUri))
            values.add(new Pair<>(UserRealm.PROFILE_PICTURE, pictureUri));
        updateUser.prepare(values);
        updateUser.execute(new UpdateUserSubscriber());
    }

    public void updateUserTribeSave(boolean tribeSave) {
        List<Pair<String, String>> values = new ArrayList<>();
        values.add(new Pair<>(UserRealm.TRIBE_SAVE, String.valueOf(tribeSave)));
        updateUser.prepare(values);
        updateUser.execute(new UpdateUserSubscriber());
    }

    public void updateUserInvisibleMode(boolean tribeSave) {
        List<Pair<String, String>> values = new ArrayList<>();
        values.add(new Pair<>(UserRealm.INVISIBLE_MODE, String.valueOf(tribeSave)));
        updateUser.prepare(values);
        updateUser.execute(new UpdateUserSubscriber());
    }

    public void updateUserFacebook(String fbid) {
        List<Pair<String, String>> values = new ArrayList<>();
        values.add(new Pair<>(UserRealm.FBID, String.valueOf(fbid)));
        updateUser.prepare(values);
        updateUser.execute(new UpdateUserSubscriber());
    }

    public void loginFacebook() {
        if (!FacebookUtils.isLoggedIn()) {
            rxFacebook.requestLogin().subscribe(loginResult -> {
                if (FacebookUtils.isLoggedIn()) {
                    getUpdateUserView().successFacebookLogin();
                } else {
                    getUpdateUserView().errorFacebookLogin();
                }
            });
        } else {
            getUpdateUserView().successFacebookLogin();
        }
    }

    protected abstract UpdateUserView getUpdateUserView();

    private final class UpdateUserSubscriber extends DefaultSubscriber<User> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(User user) {
            getUpdateUserView().setProfilePic(user.getProfilePicture());
        }
    }
}
