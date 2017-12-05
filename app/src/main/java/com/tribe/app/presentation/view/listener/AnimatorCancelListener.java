package com.tribe.app.presentation.view.listener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

/**
 * Created by tiago on 11/10/2017.
 */

public class AnimatorCancelListener extends AnimatorListenerAdapter {
  private boolean canceled;

  @Override public void onAnimationStart(Animator animation) {
    canceled = false;
  }

  @Override public void onAnimationCancel(Animator animation) {
    canceled = true;
    animation.removeAllListeners();
  }

  @Override public void onAnimationEnd(Animator animation) {
    if (!canceled) {
      animation.start();
    }
  }
}
