package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Membership;

/**
 * Created by tiago on 23/11/16.
 */
public interface GroupView extends View {

    void onGroupInfosSuccess(Group group);
    void onGroupInfosFailed();
    void onMembershipInfosSuccess(Membership membership);
    void onMembershipInfosFailed();
    void onGetMembersFailed();
    void onGroupCreatedSuccess(Membership membership);
    void onGroupCreatedError();
    void onGroupUpdatedSuccess(Group group);
    void onGroupUpdatedError();
    void onMemberAddedSuccess();
    void onMemberAddedError();
    void onLeaveGroupSuccess();
    void onLeaveGroupError();
    void onUserAddSuccess(Friendship friendship);
    void onUserAddError();
    void onMemberRemoveError();
    void onMemberRemoveSuccess();
    void onAddAdminError();
    void onAddAdminSuccess();
    void onRemoveAdminError();
    void onRemoveAdminSuccess();
    void showLoading();
    void hideLoading();
}
