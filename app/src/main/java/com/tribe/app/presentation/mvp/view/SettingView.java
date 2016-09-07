package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface SettingView extends LoadDataView{

    void updateUser(String username, String displayName, String pictureUri);
    void goToLauncher();
    void setProfilePic(String profilePicUrl);

}
