package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface SettingMVPView extends UpdateUserMVPView {

    void goToLauncher();
    void onFBContactsSync(int count);
    void onAddressBookContactSync(int count);
    void onSuccessSync();
}
