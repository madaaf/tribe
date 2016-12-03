package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
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
 * Created by tiago on 11/22/16.
 */
public class FriendMemberAdapterDelegate extends RxAdapterDelegate<List<GroupMember>> {

    private static final int DURATION_SCALE = 350;
    private static final float OVERSHOOT = 0.45f;

    // RX SUBSCRIPTIONS / SUBJECTS
    private PublishSubject<View> clickAdd = PublishSubject.create();

    // VARIABLES
    private int avatarSize;
    private Context context;
    private LayoutInflater layoutInflater;

    public FriendMemberAdapterDelegate(Context context) {
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<GroupMember> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new FriendMemberViewHolder(layoutInflater.inflate(R.layout.item_friend_member, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<GroupMember> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        FriendMemberViewHolder vh = (FriendMemberViewHolder) holder;
        GroupMember groupMember = items.get(position);

        if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
            Glide.with(context).load(groupMember.getUser().getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        vh.txtName.setText(groupMember.getUser().getDisplayName());

        if (!StringUtils.isEmpty(groupMember.getUser().getUsername()))
            vh.txtUsername.setText("@" + groupMember.getUser().getUsername());
        else
            vh.txtUsername.setText("");

        if (!groupMember.isOgMember()) {
            vh.viewAdd.setVisibility(View.VISIBLE);
            vh.viewMember.setVisibility(View.GONE);
            vh.txtMember.setVisibility(View.GONE);
            vh.txtBubble.setVisibility(View.GONE);

            if (groupMember.isMember()) {
                vh.viewAdd.setScaleX(1);
                vh.viewAdd.setScaleY(1);
            } else {
                vh.viewAdd.setScaleX(0);
                vh.viewAdd.setScaleY(0);
            }

            vh.itemView.setOnClickListener(v -> {
                if (groupMember.isMember()) {
                    groupMember.setMember(false);
                    AnimationUtils.scaleDown(vh.viewAdd, DURATION_SCALE, 0, new OvershootInterpolator(OVERSHOOT));
                } else {
                    groupMember.setMember(true);
                    AnimationUtils.scaleUp(vh.viewAdd, DURATION_SCALE, 0, new OvershootInterpolator(OVERSHOOT));
                }

                clickAdd.onNext(vh.itemView);
            });
        } else {
            vh.viewAdd.setVisibility(View.GONE);
            vh.viewMember.setVisibility(View.VISIBLE);
            vh.txtMember.setVisibility(View.VISIBLE);
            vh.txtBubble.setVisibility(View.VISIBLE);
            vh.itemView.setOnClickListener(null);
        }
    }

    public Observable<View> clickAdd() {
        return clickAdd;
    }

    static class FriendMemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;

        @BindView(R.id.txtName)
        TextViewFont txtName;

        @BindView(R.id.txtUsername)
        TextViewFont txtUsername;

        @BindView(R.id.txtMember)
        TextViewFont txtMember;

        @BindView(R.id.txtBubble)
        TextViewFont txtBubble;

        @BindView(R.id.viewAdd)
        View viewAdd;

        @BindView(R.id.btnAdd)
        View btnAdd;

        @BindView(R.id.viewMember)
        View viewMember;

        public FriendMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
