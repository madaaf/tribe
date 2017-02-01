package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class BlockedFriendAdapterDelegate extends AddAnimationAdapterDelegate<List<Friendship>> {

    // RX SUBSCRIPTIONS / SUBJECTS

    // VARIABLES
    private int avatarSize;

    public BlockedFriendAdapterDelegate(Context context) {
        super(context);
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh =
                new BlockFriendViewHolder(layoutInflater.inflate(R.layout.item_search, parent, false));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
        BlockFriendViewHolder vh = (BlockFriendViewHolder) holder;
        Friendship friendship = items.get(position);

        if (animations.containsKey(holder)) {
            animations.get(holder).cancel();
        }

        if (friendship.isShouldAnimateAdd()) {
            animateAddSuccessful(vh);
            friendship.setShouldAnimateAdd(false);
        } else if (friendship.getStatus().equals(FriendshipRealm.DEFAULT)) {
            //vh.imgPicto.setVisibility(View.VISIBLE);
            //vh.imgPicto.setImageResource(R.drawable.picto_done_white);
            //vh.btnAddBG.setVisibility(View.VISIBLE);
            //vh.progressBarAdd.setVisibility(View.GONE);
        }

        vh.txtName.setText(friendship.getDisplayName());
        vh.txtUsername.setText("@" + friendship.getUsername());

        if (!StringUtils.isEmpty(friendship.getProfilePicture())) {
            Glide.with(context)
                    .load(friendship.getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        if (friendship.isBlockedOrHidden()) {
            vh.btnAdd.setOnClickListener(v -> onClick(vh));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items,
                                 @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

    }

    static class BlockFriendViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        public ImageView imgAvatar;

        @BindView(R.id.txtName)
        public TextViewFont txtName;

        @BindView(R.id.txtUsername)
        public TextViewFont txtUsername;

        public BlockFriendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
