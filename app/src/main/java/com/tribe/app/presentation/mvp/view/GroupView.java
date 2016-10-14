package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Group;

import java.util.Date;
import java.util.List;

/**
 * Created by horatiothomas on 9/14/16.
 */
public interface GroupView extends LoadDataView {
    void setGroupId(String groupId);
    void setMembershipId(String membershipId);
    void setGroupLink(String groupLink);
    void setGroupLinkExpirationDate(Date groupLinkExpirationDate);
    void setupGroup(Group group);
    void failedToGetMembers();
    void linkCreationFailed();
    void groupCreatedSuccessfully();
    void groupCreationFailed();
    void groupUpdatedSuccessfully();
    void groupUpdatedFailed();
    void memberAddedSuccessfully();
    void memberAddedFailed();
}
