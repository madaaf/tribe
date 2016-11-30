package com.tribe.app.presentation.mvp.view;

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
    void showLoading();
    void hideLoading();
}
