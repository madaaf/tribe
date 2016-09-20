package com.tribe.app.presentation.view.adapter.delegate.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
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


        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<GroupMember> items, int position, @NonNull RecyclerView.ViewHolder holder) {

        GroupMemberViewHolder vh = (GroupMemberViewHolder) holder;
        GroupMember groupMember = items.get(position);
        vh.txtDisplayName.setText(groupMember.getDisplayName());
        vh.txtUsername.setText("@" + groupMember.getUsername());
        if (groupMember.isFriend()) vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_connected_icon));
        else vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_plus));
        if (groupMember.isAdmin()) vh.imageFriendPicBadge.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_badge_admin));
        else vh.imageFriendPicBadge.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_badge_more));
        vh.itemView.setTag(R.id.tag_position, position);


        try {
            Glide.with(context)
                    .load(groupMember.getProfilePicture())
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(vh.imageFriendPic);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public Observable<View> clickMemberItem() {
        return clickMemberItem;
    }

    static class GroupMemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageFriendPic) public ImageView imageFriendPic;
        @BindView(R.id.txtDisplayName) public TextViewFont txtDisplayName;
        @BindView(R.id.txtUsername) public TextViewFont txtUsername;
        @BindView(R.id.layoutSelected) public FrameLayout layoutSelected;
        @BindView(R.id.imageFriendPicBadge) public ImageView imageFriendPicBadge;
        public boolean selected;


        public GroupMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            selected = false;
        }
    }


}
