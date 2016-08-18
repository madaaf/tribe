package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;

/**
 * Created by horatiothomas on 8/18/16.
 */
public class ProfileInfoFragment extends Fragment {

    public static ProfileInfoFragment newInstance() {
        
        Bundle args = new Bundle();
        
        ProfileInfoFragment fragment = new ProfileInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile_info, container, false);
        return  fragmentView;
    }
}
