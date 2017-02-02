package com.tribe.app.presentation.view.adapter.delegate.base;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/31/17.
 */
public abstract class AddAnimationAdapterDelegate<T> extends RxAdapterDelegate<T> {

  private static final int DURATION = 300;

  protected LayoutInflater layoutInflater;
  protected Context context;
  protected int actionButtonHeight, marginSmall;

  // RX SUBSCRIPTIONS / SUBJECTS
  private final PublishSubject<View> clickAdd = PublishSubject.create();
  private final PublishSubject<View> clickRemove = PublishSubject.create();

  protected Map<AddAnimationViewHolder, AnimatorSet> animations = new HashMap<>();

  public AddAnimationAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    actionButtonHeight = context.getResources().getDimensionPixelSize(R.dimen.action_button_height);
    marginSmall = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
  }

  protected void onClick(AddAnimationViewHolder vh) {
    animations.put(vh, animate(vh));
    clickAdd.onNext(vh.itemView);
  }

  protected AnimatorSet animate(AddAnimationViewHolder vh) {
    AnimatorSet animatorSet = new AnimatorSet();

    ObjectAnimator alphaAnimAdd = ObjectAnimator.ofFloat(vh.txtAction, "alpha", 1f, 0f);

    ObjectAnimator alphaAnimProgress = ObjectAnimator.ofFloat(vh.progressBarAdd, "alpha", 0f, 1f);

    Animator animator =
        AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(), actionButtonHeight);

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(alphaAnimAdd).with(alphaAnimProgress).with(animator);
    animatorSet.start();
    return animatorSet;
  }

  protected void animateAddSuccessful(AddAnimationViewHolder vh) {
    AnimatorSet animatorSet = new AnimatorSet();

    ObjectAnimator alphaAnimAdd = ObjectAnimator.ofFloat(vh.txtAction, "alpha", 0f, 1f);

    ObjectAnimator alphaAnimProgress = ObjectAnimator.ofFloat(vh.progressBarAdd, "alpha", 1f, 0f);

    vh.txtAction.setText(R.string.action_hang_live);
    vh.txtAction.measure(0, 0);

    Animator animator = AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(),
        vh.txtAction.getMeasuredWidth() + (2 * marginSmall));

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(alphaAnimAdd).with(alphaAnimProgress).with(animator);
    animatorSet.start();

    if (vh.btnAdd.getBackground() instanceof TransitionDrawable) {
      ((TransitionDrawable) vh.btnAdd.getBackground()).startTransition(DURATION);
    }

    animations.put(vh, animatorSet);
  }

  public Observable<View> clickAdd() {
    return clickAdd;
  }

  public Observable<View> clickRemove() {
    return clickRemove;
  }
}
