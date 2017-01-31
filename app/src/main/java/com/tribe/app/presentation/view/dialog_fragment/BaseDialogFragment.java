package com.tribe.app.presentation.view.dialog_fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;

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

  @Override public void onDestroy() {
    unbinder.unbind();

    removeSubscriptions();

    super.onDestroy();
  }

  @Override public void onStart() {
    super.onStart();
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
