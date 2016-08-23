package com.tribe.app.presentation.view.dialog_fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * GetNotifiedDialogFragment.java
 * Created by horatiothomas on 8/20/16.
 */
public class GetNotifiedDialogFragment extends DialogFragment {

    private Unbinder unbinder;

    public static GetNotifiedDialogFragment newInstance() {
        Bundle args = new Bundle();
        GetNotifiedDialogFragment fragment = new GetNotifiedDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.dialog_fragment_get_notified, container, false);

        initUi(fragmentView);

        return fragmentView;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.GetNotifiedAnimation;
        return dialog;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        super.onDestroy();
    }

    private void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

    }

}
