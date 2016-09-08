package com.tribe.app.presentation.view.adapter.delegate.block_friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class BlockFriendAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickFriendItem = PublishSubject.create();

    private Context context;

    public BlockFriendAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new BlockFriendViewHolder(layoutInflater.inflate(R.layout.item_block_friend, parent, false));

        subscriptions.add(RxView.clicks(vh.itemView)
        .takeUntil(RxView.detaches(parent))
        .map(friend -> vh.itemView)
        .subscribe(clickFriendItem));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {

        BlockFriendViewHolder vh = (BlockFriendViewHolder) holder;
        Friendship friendship = items.get(position);

        vh.txtDisplayName.setText(friendship.getDisplayName());
        vh.txtUsername.setText("@" + friendship.getUsername());

        try {
            Picasso.with(context)
                    .load(friendship.getProfilePicture())
                    .transform(new RoundedCornersTransformation(R.dimen.setting_pic_size >> 1, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(vh.imageFriendPic);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public Observable<View> clickFriendItem() {
        return clickFriendItem;
    }

    static class BlockFriendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageFriendPic) public ImageView imageFriendPic;
        @BindView(R.id.txtDisplayName) public TextViewFont txtDisplayName;
        @BindView(R.id.txtUsername) public TextViewFont txtUsername;
        @BindView(R.id.imageBlock) public ImageView imageBlock;


        public BlockFriendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
