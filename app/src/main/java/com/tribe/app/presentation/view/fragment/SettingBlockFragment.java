package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingBlockFragment extends BaseFragment {

    public static SettingBlockFragment newInstance() {

        Bundle args = new Bundle();

        SettingBlockFragment fragment = new SettingBlockFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
