package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface SettingView extends LoadDataView {

    void changeUsername(String username);
    void changeDisplayName(String displayName);

}
