package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 9/7/16.
 */
public class FriendMemberAdapterDelegate extends RxAdapterDelegate<List<User>> {

    // RX SUBSCRIPTIONS / SUBJECTS
    private PublishSubject<Boolean> clickAdd = PublishSubject.create();

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
    public boolean isForViewType(@NonNull List<User> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new FriendMemberViewHolder(layoutInflater.inflate(R.layout.item_friend_member, parent, false));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<User> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        FriendMemberViewHolder vh = (FriendMemberViewHolder) holder;
        User user = items.get(position);

        if (!StringUtils.isEmpty(user.getProfilePicture())) {
            Glide.with(context).load(user.getProfilePicture())
                    .thumbnail(0.25f)
                    .override(avatarSize, avatarSize)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .crossFade()
                    .into(vh.imgAvatar);
        }

        vh.txtName.setText(user.getDisplayName());
        vh.txtUsername.setText(user.getUsername());
    }

    public Observable<Boolean> clickAdd() {
        return clickAdd;
    }

    static class FriendMemberViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;

        @BindView(R.id.txtName)
        TextViewFont txtName;

        @BindView(R.id.txtUsername)
        TextViewFont txtUsername;

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
