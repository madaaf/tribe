package com.tribe.app.presentation.view.adapter.delegate.friend;

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
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class FriendAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickFriendItem = PublishSubject.create();

    private boolean selected = false;

    private Context context;

    public FriendAdapterDelegate(Context context) {
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
                .doOnNext(aVoid -> {
                    if (selected) {
                        ((BlockFriendViewHolder) vh).imageSelected.setImageDrawable(null);
                        selected = false;
                    } else {
                        ((BlockFriendViewHolder) vh).imageSelected.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_oval_green_fill));
                        selected = true;
                    }
                })
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
        vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_oval));

        try {
            Glide.with(context)
                    .load(friendship.getProfilePicture())
                    .bitmapTransform(new CropCircleTransformation(context))
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
        @BindView(R.id.layoutSelected) public FrameLayout layoutSelected;
        @BindView(R.id.imageSelected) public ImageView imageSelected;


        public BlockFriendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
