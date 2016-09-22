package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface UpdateUserView extends LoadDataView {

    void setProfilePic(String profilePicUrl);
    void successFacebookLogin();
    void errorFacebookLogin();
}
