package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Group;

/**
 * Created by horatiothomas on 9/14/16.
 */
public interface GroupView extends LoadDataView {
    void getGroupMembers(String groupId);
    void setupGroupMembers(Group group);
}
