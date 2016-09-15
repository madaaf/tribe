package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 9/14/16.
 */
public interface GroupView extends LoadDataView {
    void getGroupMembers(String groupId);
}
