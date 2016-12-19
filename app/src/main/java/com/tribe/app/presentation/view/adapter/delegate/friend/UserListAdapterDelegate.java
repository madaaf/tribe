package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
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
public class UserListAdapterDelegate extends AddAnimationAdapterDelegate<List<User>> {

    @Inject
    User user;

    // VARIABLES
    private int avatarSize;

    public UserListAdapterDelegate(Context context) {
        super(context);
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<User> items, int position) {
        return items.get(position) instanceof User;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new UserListViewHolder(layoutInflater.inflate(R.layout.item_user_list, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<User> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserListViewHolder vh = (UserListViewHolder) holder;
        User user = (User) items.get(position);

        if (animations.containsKey(holder)) {
            animations.get(holder).cancel();
        }

        vh.btnAdd.setVisibility(View.VISIBLE);
        vh.imgGhost.setVisibility(View.GONE);

        if (user.isAnimateAdd()) {
            animateAddSuccessful(vh);
        } else if (user.isFriend()) {
            vh.imgPicto.setVisibility(View.VISIBLE);
            vh.imgPicto.setImageResource(R.drawable.picto_done_white);
            vh.btnAddBG.setVisibility(View.VISIBLE);
            vh.progressBarAdd.setVisibility(View.GONE);
        } else if (user.isInvisibleMode()) {
            vh.btnAdd.setVisibility(View.GONE);
            vh.imgGhost.setVisibility(View.VISIBLE);
        }

        vh.txtName.setText(user.getDisplayName());

        if (!StringUtils.isEmpty(user.getUsername()))
            vh.txtUsername.setText("@" + user.getUsername());
        else
            vh.txtUsername.setText("");

        if (!StringUtils.isEmpty(user.getProfilePicture())) {
            Glide.with(context).load(user.getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        if (!user.equals(user) && !user.isFriend() && !user.isInvisibleMode()) setClicks(vh, null);
    }

    static class UserListViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;

        @BindView(R.id.txtName)
        TextViewFont txtName;

        @BindView(R.id.txtUsername)
        TextViewFont txtUsername;

        @BindView(R.id.imgGhost)
        ImageView imgGhost;

        public UserListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
