package com.tribe.app.presentation.mvp.view;

import java.util.List;

/**
 * Created by horatiothomas on 9/19/16.
 */
public interface GroupMemberView extends LoadDataView {
    void createFriendship();
    void removeFriend();
    void setAdmin();
    void removeAdmin();
    void removeMember();

}
