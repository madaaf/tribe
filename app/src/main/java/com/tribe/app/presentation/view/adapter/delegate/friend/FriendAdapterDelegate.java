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
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class FriendAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickFriendItem = PublishSubject.create();

    private Context context;
    private boolean privateGroup;

    public FriendAdapterDelegate(Context context, boolean privateGroup) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.privateGroup = privateGroup;
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new FriendViewHolder(layoutInflater.inflate(R.layout.item_friend, parent, false));
        RxView.detaches(parent).subscribe(aVoid -> {
            if (subscriptions.hasSubscriptions()) {
                subscriptions.unsubscribe();
                subscriptions.clear();
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        FriendViewHolder vh = (FriendViewHolder) holder;
        Friendship friendship = items.get(position);
        vh.txtDisplayName.setText(friendship.getDisplayName());
        if (!StringUtils.isEmpty(friendship.getUsername())) vh.txtUsername.setText("@" + friendship.getUsername());
        vh.layoutSelected.setBackground(ContextCompat.getDrawable(context, R.drawable.picto_oval));
        if (privateGroup) vh.imageSelected.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_oval_green_fill));
        else vh.imageSelected.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.shape_oval_purple_fill));
        if (!friendship.isSelected()) {
            vh.imageSelected.setScaleY(AnimationUtils.SCALE_INVISIBLE);
            vh.imageSelected.setScaleX(AnimationUtils.SCALE_INVISIBLE);
        } else {
            vh.imageSelected.setScaleY(AnimationUtils.SCALE_RESET);
            vh.imageSelected.setScaleX(AnimationUtils.SCALE_RESET);
        }
        vh.itemView.setTag(R.id.tag_position, position);


        if (friendship.getProfilePicture().equals(context.getString(R.string.no_profile_picture_url))) {
            vh.imageFriendPic.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_avatar_placeholder));
        } else {
            Glide.with(context)
                    .load(friendship.getProfilePicture())
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(vh.imageFriendPic);
        }

        subscriptions.add(RxView.clicks(vh.itemView)
                .doOnNext(aVoid -> {
                    vh.itemView.setEnabled(false);
                    Observable.timer(AnimationUtils.ANIMATION_DURATION_SHORT, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                vh.itemView.setEnabled(true);
                            });
                    FriendViewHolder blockFriendViewHolder = (FriendViewHolder) vh;
                    if (friendship.isSelected()) {
                        blockFriendViewHolder.imageSelected.animate()
                                .scaleX(AnimationUtils.SCALE_INVISIBLE)
                                .scaleY(AnimationUtils.SCALE_INVISIBLE)
                                .setDuration(AnimationUtils.ANIMATION_DURATION_SHORT)
                                .setStartDelay(AnimationUtils.NO_START_DELAY)
                                .start();
                        friendship.setSelected(false);
                    } else {
                        blockFriendViewHolder.imageSelected.animate()
                                .scaleX(AnimationUtils.SCALE_RESET)
                                .scaleY(AnimationUtils.SCALE_RESET)
                                .setDuration(AnimationUtils.ANIMATION_DURATION_SHORT)
                                .setStartDelay(AnimationUtils.NO_START_DELAY)
                                .start();
                        friendship.setSelected(true);
                    }
                })
                .map(friend -> vh.itemView)
                .subscribe(clickFriendItem));

    }

    public Observable<View> clickFriendItem() {
        return clickFriendItem;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageFriendPic) public ImageView imageFriendPic;
        @BindView(R.id.imageFriendPicBadge) public ImageView imageFriendPicBadge;
        @BindView(R.id.txtDisplayName) public TextViewFont txtDisplayName;
        @BindView(R.id.txtUsername) public TextViewFont txtUsername;
        @BindView(R.id.layoutSelected) public FrameLayout layoutSelected;
        @BindView(R.id.imageSelected) public ImageView imageSelected;


        public FriendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
