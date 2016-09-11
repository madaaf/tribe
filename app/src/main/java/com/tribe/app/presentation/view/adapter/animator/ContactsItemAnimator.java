package com.tribe.app.presentation.view.adapter.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Miroslaw Stanek on 02.12.2015.
 */
public class ContactsItemAnimator extends DefaultItemAnimator {

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    Map<RecyclerView.ViewHolder, AnimatorSet> addAnimationsMap = new HashMap<>();

    private int lastAddAnimatedItem = -2;

    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
                                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags, @NonNull List<Object> payloads) {
        if (changeFlags == FLAG_CHANGED) {
            for (Object payload : payloads) {
                if (payload instanceof String) {
                    return new ContactHolderInfo((String) payload);
                }
            }
        }

        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preInfo,
                                 @NonNull ItemHolderInfo postInfo) {
        cancelCurrentAnimationIfExists(newHolder);

        if (preInfo instanceof ContactHolderInfo) {
            ContactHolderInfo contactHolderInfo = (ContactHolderInfo) preInfo;
            SearchResultGridAdapterDelegate.SearchResultViewHolder holder = (SearchResultGridAdapterDelegate.SearchResultViewHolder) newHolder;

            if (SearchResultGridAdapterDelegate.ACTION_ADD.equals(contactHolderInfo.updateAction)) {
                animateAddButton(holder);
            }
        }

        return false;
    }

    private void cancelCurrentAnimationIfExists(RecyclerView.ViewHolder item) {
        if (addAnimationsMap.containsKey(item)) {
            addAnimationsMap.get(item).cancel();
        }
    }

    private void animateAddButton(final SearchResultGridAdapterDelegate.SearchResultViewHolder holder) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.imgPicto, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator alphaAnimAdd = ObjectAnimator.ofFloat(holder.imgPicto, "alpha", 1f, 0f);
        alphaAnimAdd.setDuration(300);
        alphaAnimAdd.setInterpolator(DECCELERATE_INTERPOLATOR);
        alphaAnimAdd.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.imgPicto.setAlpha(1f);
                holder.imgPicto.setVisibility(View.GONE);
            }
        });

        ObjectAnimator alphaAnimProgress = ObjectAnimator.ofFloat(holder.progressBarAdd, "alpha", 0f, 1f);
        alphaAnimProgress.setDuration(300);
        alphaAnimProgress.setStartDelay(150);
        alphaAnimProgress.setInterpolator(DECCELERATE_INTERPOLATOR);
        alphaAnimProgress.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                holder.progressBarAdd.setAlpha(0f);
                holder.progressBarAdd.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                addAnimationsMap.remove(holder);
                dispatchChangeFinishedIfAllAnimationsEnded(holder);
            }
        });

        animatorSet.play(rotationAnim).with(alphaAnimAdd).with(alphaAnimProgress);
        animatorSet.start();

        addAnimationsMap.put(holder, animatorSet);
    }

    private void dispatchChangeFinishedIfAllAnimationsEnded(SearchResultGridAdapterDelegate.SearchResultViewHolder holder) {
        if (addAnimationsMap.containsKey(holder)) {
            return;
        }

        dispatchAnimationFinished(holder);
    }

    private void resetAddAnimationState(SearchResultGridAdapterDelegate.SearchResultViewHolder vh) {
        vh.imgPicto.setImageResource(R.drawable.picto_add);
        vh.btnAddBG.setVisibility(View.GONE);
        vh.progressBarAdd.setVisibility(View.GONE);
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        super.endAnimation(item);
        cancelCurrentAnimationIfExists(item);
    }

    @Override
    public void endAnimations() {
        super.endAnimations();

        for (AnimatorSet animatorSet : addAnimationsMap.values()) {
            animatorSet.cancel();
        }
    }

    public static class ContactHolderInfo extends ItemHolderInfo {

        public String updateAction;

        public ContactHolderInfo(String updateAction) {
            this.updateAction = updateAction;
        }
    }
}
