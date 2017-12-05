package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface SettingsMVPView extends UpdateUserMVPView {

  void goToLauncher();

  void onFBContactsSync(int count);

  void onAddressBookContactSync(int count);

  void onSuccessSync();
}
