package com.tribe.app.presentation.view.adapter.delegate.group;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMemberAdapterDelegate extends RxAdapterDelegate<List<GroupMember>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickMemberItem = PublishSubject.create();

    private Context context;

    public GroupMemberAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public boolean isForViewType(@NonNull List<GroupMember> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new GroupMemberViewHolder(layoutInflater.inflate(R.layout.item_block_friend, parent, false));

        subscriptions.add(RxView.clicks(vh.itemView)
                .takeUntil(RxView.detaches(parent))
                .map(friend -> vh.itemView)
                .subscribe(clickMemberItem));
        subscriptions.add(RxView.clicks(((GroupMemberViewHolder) vh).btnAdd)
                .takeUntil(RxView.detaches(parent))
                .map(friend -> vh.itemView)
                .subscribe(clickMemberItem));;

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<GroupMember> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        GroupMemberViewHolder vh = (GroupMemberViewHolder) holder;
        GroupMember groupMember = items.get(position);
        vh.btnAdd.setVisibility(View.VISIBLE);
        vh.txtDisplayName.setText(groupMember.getDisplayName());
        if (!StringUtils.isEmpty(groupMember.getUsername())) vh.txtUsername.setText("@" + groupMember.getUsername());
//        if (groupMember.isFriend()) vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_connected_icon));
//        else vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_plus_black));
        if (groupMember.isShouldAnimateAddFriend()) {
            animateAddSuccessful(vh);
            groupMember.setShouldAnimateAddFriend(false);
        } else {
            if (groupMember.isFriend()) {
                vh.imgPicto.setVisibility(View.VISIBLE);
                vh.imgPicto.setImageResource(R.drawable.picto_done_white);
                vh.btnAddBg.setVisibility(View.VISIBLE);
                vh.progressBarAdd.setVisibility(View.GONE);
            } else {
                vh.imgPicto.setVisibility(View.VISIBLE);
                vh.imgPicto.setImageResource(R.drawable.picto_add);
                vh.btnAddBg.setVisibility(View.GONE);
                vh.progressBarAdd.setVisibility(View.GONE);
            }
        }
        if (groupMember.isShouldAnimateAddAdmin()) {
            AnimationUtils.scaleOldImageOutNewImageIn(vh.imageFriendPicBadge,
                    ContextCompat.getDrawable(context, R.drawable.picto_badge_more),
                    ContextCompat.getDrawable(context, R.drawable.picto_badge_admin));
            groupMember.setShouldAnimateAddAdmin(false);
        } else if (groupMember.isShouldAnimateRemoveAdmin()) {
            AnimationUtils.scaleOldImageOutNewImageIn(vh.imageFriendPicBadge,
                    ContextCompat.getDrawable(context, R.drawable.picto_badge_admin),
                    ContextCompat.getDrawable(context, R.drawable.picto_badge_more));
            groupMember.setShouldAnimateRemoveAdmin(false);
        } else {
            if (groupMember.isAdmin())
                vh.imageFriendPicBadge.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_badge_admin));
            else
                vh.imageFriendPicBadge.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_badge_more));
        }
        vh.itemView.setTag(R.id.tag_position, position);
        if (groupMember.isCurrentUser())
            vh.layoutFriendItem.setForeground(new ColorDrawable(ContextCompat.getColor(context, R.color.white_opacity_40)));

        try {
            Glide.with(context)
                    .load(groupMember.getProfilePicture())
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(vh.imageFriendPic);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void animateAddSuccessful(GroupMemberViewHolder vh) {
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
        scaleXAnim.setDuration(AnimationUtils.ANIMATION_DURATION_SHORT);
        scaleXAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(vh.imgPicto, "scaleY", 0.2f, 1f);
        scaleYAnim.setDuration(AnimationUtils.ANIMATION_DURATION_SHORT);
        scaleYAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alphaBG = ObjectAnimator.ofFloat(vh.btnAddBg, "alpha", 0f, 1f);
        alphaBG.setDuration(AnimationUtils.ANIMATION_DURATION_SHORT);
        alphaBG.setInterpolator(new DecelerateInterpolator());
        alphaBG.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                vh.btnAddBg.setVisibility(View.VISIBLE);
            }
        });

        animatorSet.play(rotationAnim).with(scaleXAnim).with(scaleYAnim).with(alphaBG);
        animatorSet.start();
    }

    public Observable<View> clickMemberItem() {
        return clickMemberItem;
    }

    static class GroupMemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutFriendItem) public FrameLayout layoutFriendItem;
        @BindView(R.id.imageFriendPic) public ImageView imageFriendPic;
        @BindView(R.id.txtDisplayName) public TextViewFont txtDisplayName;
        @BindView(R.id.txtUsername) public TextViewFont txtUsername;
        @BindView(R.id.imageFriendPicBadge) public ImageView imageFriendPicBadge;
        @BindView(R.id.imgPicto) public ImageView imgPicto;
        @BindView(R.id.btnAddBG) public View btnAddBg;
        @BindView(R.id.progressBarAdd) public CircularProgressView progressBarAdd;
        @BindView(R.id.btnAdd) public FrameLayout btnAdd;
        public boolean selected;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            selected = false;
        }
    }
}
