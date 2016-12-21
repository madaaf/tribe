package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapterDelegate extends AddAnimationAdapterDelegate<List<GroupMember>> {

    @Inject
    User user;

    // VARIABLES
    private int avatarSize;
    private boolean currentUserAdmin = false;

    // RX SUBSCRIPTIONS / SUBJECTS
    private PublishSubject<View> longClick = PublishSubject.create();

    public MemberListAdapterDelegate(Context context, boolean currentUserAdmin) {
        super(context);
        this.currentUserAdmin = currentUserAdmin;
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<GroupMember> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new GroupMemberViewHolder(layoutInflater.inflate(R.layout.item_member_list, parent, false));

        if (currentUserAdmin) {
            vh.itemView.setOnLongClickListener(v -> {
                longClick.onNext(v);
                return true;
            });
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<GroupMember> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        GroupMemberViewHolder vh = (GroupMemberViewHolder) holder;
        GroupMember groupMember = items.get(position);

        if (animations.containsKey(holder)) {
            animations.get(holder).cancel();
        }

        vh.btnAdd.setVisibility(View.VISIBLE);
        vh.imgGhost.setVisibility(View.GONE);

        if (groupMember.isAnimateAdd()) {
            animateAddSuccessful(vh);
        } else if (groupMember.isFriend()) {
            vh.imgPicto.setVisibility(View.VISIBLE);
            vh.imgPicto.setImageResource(R.drawable.picto_done_white);
            vh.btnAddBG.setVisibility(View.VISIBLE);
            vh.progressBarAdd.setVisibility(View.GONE);
        } else if (groupMember.getUser().isInvisibleMode()) {
            vh.btnAdd.setVisibility(View.GONE);
            vh.imgGhost.setVisibility(View.VISIBLE);
        }

        vh.txtName.setText(groupMember.getUser().getDisplayName());

        if (!StringUtils.isEmpty(groupMember.getUser().getUsername()))
            vh.txtUsername.setText("@" + groupMember.getUser().getUsername());
        else
            vh.txtUsername.setText("");

        if (groupMember.isAdmin()) {
            vh.imgMemberBadge.setVisibility(View.VISIBLE);
            vh.imgMemberBadge.setImageResource(R.drawable.picto_badge_admin);
        } else if (currentUserAdmin) {
            vh.imgMemberBadge.setVisibility(View.VISIBLE);
            vh.imgMemberBadge.setImageResource(R.drawable.picto_badge_more);
        } else {
            vh.imgMemberBadge.setVisibility(View.GONE);
        }

        if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
            Glide.with(context).load(groupMember.getUser().getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        if (!user.equals(groupMember.getUser()) && !groupMember.isFriend() && !groupMember.getUser().isInvisibleMode()) {
            vh.btnAdd.setOnClickListener(v -> onClick(vh));
        }
    }

    public Observable<View> onLongClick() {
        return longClick;
    }

    static class GroupMemberViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;

        @BindView(R.id.txtName)
        TextViewFont txtName;

        @BindView(R.id.txtUsername)
        TextViewFont txtUsername;

        @BindView(R.id.imgGhost)
        ImageView imgGhost;

        @BindView(R.id.imgMemberBadge)
        ImageView imgMemberBadge;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
