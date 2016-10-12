package com.tribe.app.presentation.view.dialog_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 9/13/16.
 */
public class BaseDialogFragment extends DialogFragment {

    private Unbinder unbinder;

    @Override
    public void onDestroy() {
        unbinder.unbind();

        removeSubscriptions();

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        // window animation
        final View decorView = getDialog()
                .getWindow()
                .getDecorView();

        decorView.animate().translationY(100)
                .setStartDelay(300)
                .setDuration(150)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        decorView.animate().translationY(0)
                                .setInterpolator(new LinearInterpolator())
                                .setStartDelay(50)
                                .setDuration(150)
                                .start();
                    }
                })
                .start();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.GetNotifiedAnimation;
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);
    }

    public void removeSubscriptions() {

    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }
}
