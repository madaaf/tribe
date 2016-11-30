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

/**
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapterDelegate extends AddAnimationAdapterDelegate<List<GroupMember>> {

    @Inject
    User user;

    // RX SUBSCRIPTIONS / SUBJECTS

    // VARIABLES
    private int avatarSize;

    public MemberListAdapterDelegate(Context context) {
        super(context);
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
        RecyclerView.ViewHolder vh = new GroupMemberViewHolder(layoutInflater.inflate(R.layout.item_search, parent, false));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<GroupMember> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        GroupMemberViewHolder vh = (GroupMemberViewHolder) holder;
        GroupMember groupMember = items.get(position);

        if (animations.containsKey(holder)) {
            animations.get(holder).cancel();
        }

        if (groupMember.isAnimateAdd()) {
            animateAddSuccessful(vh);
        } else if (groupMember.isFriend()) {
            vh.imgPicto.setVisibility(View.VISIBLE);
            vh.imgPicto.setImageResource(R.drawable.picto_done_white);
            vh.btnAddBG.setVisibility(View.VISIBLE);
            vh.progressBarAdd.setVisibility(View.GONE);
        }

        vh.txtName.setText(groupMember.getUser().getDisplayName());
        vh.txtUsername.setText("@" + groupMember.getUser().getUsername());

        if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
            Glide.with(context).load(groupMember.getUser().getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        if (!user.equals(groupMember.getUser()) && !groupMember.isFriend()) setClicks(vh, null);
    }

    static class GroupMemberViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        public ImageView imgAvatar;

        @BindView(R.id.txtName)
        public TextViewFont txtName;

        @BindView(R.id.txtUsername)
        public TextViewFont txtUsername;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
