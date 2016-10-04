package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Group;

import java.util.List;

/**
 * Created by horatiothomas on 9/14/16.
 */
public interface GroupView extends LoadDataView {
    void setGroupId(String groupId);
    void setGroupLink(String groupLink);
    void setupGroup(Group group);
    void groupCreatedSuccessfully();
    void groupCreationFailed();
    void groupUpdatedSuccessfully();
    void groupUpdatedFailed();
    void memberAddedSuccessfully();
    void memberAddedFailed();
}
