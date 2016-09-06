package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingUpdateProfileFragment extends BaseFragment {

    public static SettingUpdateProfileFragment newInstance() {

        Bundle args = new Bundle();

        SettingUpdateProfileFragment fragment = new SettingUpdateProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
