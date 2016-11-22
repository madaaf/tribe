package com.tribe.app.presentation.view.adapter.delegate.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 9/7/16.
 */
public abstract class AddAnimationAdapterDelegate<T> extends RxAdapterDelegate<T> {

    protected LayoutInflater layoutInflater;
    protected Context context;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickAdd = PublishSubject.create();
    private final PublishSubject<View> clickRemove = PublishSubject.create();

    protected Map<AddAnimationViewHolder, AnimatorSet> animations = new HashMap<>();

    public AddAnimationAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    protected void setClicks(AddAnimationViewHolder vh, Friendship friendship) {
        vh.btnAdd.setOnClickListener(v -> {
            if (friendship == null || friendship.isBlockedOrHidden()) {
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(vh.imgPicto, "rotation", 0f, 45f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(new DecelerateInterpolator());

                ObjectAnimator alphaAnimAdd = ObjectAnimator.ofFloat(vh.imgPicto, "alpha", 1f, 0f);
                alphaAnimAdd.setDuration(300);
                alphaAnimAdd.setInterpolator(new DecelerateInterpolator());
                alphaAnimAdd.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        vh.imgPicto.setRotation(0);
                        vh.imgPicto.setAlpha(1f);
                        vh.imgPicto.setVisibility(View.GONE);
                    }
                });

                ObjectAnimator alphaAnimProgress = ObjectAnimator.ofFloat(vh.progressBarAdd, "alpha", 0f, 1f);
                alphaAnimProgress.setDuration(300);
                alphaAnimProgress.setStartDelay(150);
                alphaAnimProgress.setInterpolator(new DecelerateInterpolator());
                alphaAnimProgress.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        vh.progressBarAdd.setAlpha(0f);
                        vh.progressBarAdd.setVisibility(View.VISIBLE);
                    }
                });

                animatorSet.play(rotationAnim).with(alphaAnimAdd).with(alphaAnimProgress);
                animatorSet.start();
                animations.put(vh, animatorSet);
                clickAdd.onNext(vh.itemView);
            }
        });
    }

    protected void animateAddSuccessful(AddAnimationViewHolder vh) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(vh.imgPicto, "rotation", -45f, 0f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(new DecelerateInterpolator());
        rotationAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                vh.imgPicto.setImageResource(R.drawable.picto_done_white);
                vh.imgPicto.setVisibility(View.VISIBLE);
                vh.progressBarAdd.setVisibility(View.GONE);
            }
        });

        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(vh.imgPicto, "scaleX", 0.2f, 1f);
        scaleXAnim.setDuration(300);
        scaleXAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(vh.imgPicto, "scaleY", 0.2f, 1f);
        scaleYAnim.setDuration(300);
        scaleYAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alphaBG = ObjectAnimator.ofFloat(vh.btnAddBG, "alpha", 0f, 1f);
        alphaBG.setDuration(300);
        alphaBG.setInterpolator(new DecelerateInterpolator());
        alphaBG.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                vh.btnAddBG.setVisibility(View.VISIBLE);
            }
        });

        animatorSet.play(rotationAnim).with(scaleXAnim).with(scaleYAnim).with(alphaBG);
        animatorSet.start();
        animations.put(vh, animatorSet);
    }

    public Observable<View> clickAdd() {
        return clickAdd;
    }

    public Observable<View> clickRemove() {
        return clickRemove;
    }
}
