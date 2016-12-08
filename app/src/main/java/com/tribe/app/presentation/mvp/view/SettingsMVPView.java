package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import java.util.List;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface SettingsMVPView extends UpdateUserMVPView {

    void goToLauncher();
    void onFBContactsSync(int count);
    void onAddressBookContactSync(int count);
    void onSuccessSync();
    void friendshipUpdated(Friendship friendship);
    void renderBlockedFriendshipList(List<Friendship> friendshipList);
}
