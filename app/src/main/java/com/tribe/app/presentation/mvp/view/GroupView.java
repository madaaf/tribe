package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Membership;

/**
 * Created by tiago on 23/11/16.
 */
public interface GroupView extends View {
    void onMembershipInfosSuccess(Membership membership);
    void onMembershipInfosFailed();
    void onGetMembersFailed();
    void onGroupCreatedSuccess(Membership membership);
    void onGroupCreatedError();
    void onGroupUpdatedSuccess();
    void onGroupUpdatedError();
    void onMemberAddedSuccess();
    void onMemberAddedError();
    void showLoading();
    void hideLoading();
}
