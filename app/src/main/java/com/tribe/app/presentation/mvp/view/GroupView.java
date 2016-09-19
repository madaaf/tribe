package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Group;

import java.util.List;

/**
 * Created by horatiothomas on 9/14/16.
 */
public interface GroupView extends LoadDataView {
    void getGroupMembers(String groupId);
    void setupGroup(Group group);
    void createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri);
    void backToHome();
}
