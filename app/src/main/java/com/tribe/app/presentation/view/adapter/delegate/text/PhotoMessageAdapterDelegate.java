package com.tribe.app.presentation.view.adapter.delegate.text;

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
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 08/09/2016.
 */
public class PhotoMessageAdapterDelegate extends BaseMessageAdapterDelegate {

    @Inject
    Picasso picasso;

    @Inject
    ScreenUtils screenUtils;

    // RESOURCES
    private int marginLeft;
    private int marginRight;
    private int radiusImg;

    // OBSERVABLES
    private final PublishSubject<ImageView> clickPhoto = PublishSubject.create();

    public PhotoMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        super(inflater, context);

        marginLeft = context.getResources().getDimensionPixelSize(R.dimen.horizontal_left_margin_chat);
        marginRight = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_xlarge);
        radiusImg = context.getResources().getDimensionPixelSize(R.dimen.radius_img);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        ChatMessage chatMessage = items.get(position);
        return chatMessage.getType().equals(ChatMessage.PHOTO);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        PhotoViewHolder vh = (PhotoViewHolder) super.onCreateViewHolder(parent);

        ViewGroup.LayoutParams params = vh.imgPhoto.getLayoutParams();
        params.width = screenUtils.getWidthPx() - marginLeft - marginRight;
        params.height = params.width;
        vh.imgPhoto.setLayoutParams(params);

        subscriptions.add(RxView.clicks(vh.imgPhoto)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> vh.imgPhoto)
                .subscribe(clickPhoto));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        super.onBindViewHolder(items, position, holder);
        PhotoViewHolder vh = (PhotoViewHolder) holder;

        ChatMessage chatMessage = items.get(position);

        vh.imgPhoto.setTag(R.id.tag_position, position);

        picasso.load(chatMessage.getContent())
                //.fit()
                //.centerCrop()
                //.transform(new RoundedCornersTransformation(radiusImg, 0, RoundedCornersTransformation.CornerType.ALL))
                .into(vh.imgPhoto);
    }

    @Override
    protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
        return new PhotoViewHolder(layoutInflater.inflate(R.layout.item_text_photo, parent, false));
    }

    public PublishSubject<ImageView> clickPhoto() {
        return clickPhoto;
    }

    static class PhotoViewHolder extends BaseTextViewHolder {

        @BindView(R.id.imgPhoto) public ImageView imgPhoto;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
