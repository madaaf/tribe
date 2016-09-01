package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
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
public class VideoMessageAdapterDelegate extends BaseMessageAdapterDelegate {

    @Inject
    ScreenUtils screenUtils;

    // RESOURCES
    private int marginLeft;
    private int marginRight;

    // OBSERVABLES
    private final PublishSubject<ImageView> clickVideo = PublishSubject.create();

    public VideoMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        super(inflater, context);

        marginLeft = context.getResources().getDimensionPixelSize(R.dimen.horizontal_left_margin_chat);
        marginRight = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_xlarge);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        ChatMessage chatMessage = items.get(position);
        return chatMessage.getType().equals(ChatMessage.VIDEO);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        VideoViewHolder vh = (VideoViewHolder) super.onCreateViewHolder(parent);

        ViewGroup.LayoutParams params = vh.imgVideoThumbnail.getLayoutParams();
        params.width = screenUtils.getWidthPx() - marginLeft - marginRight;
        params.height = params.width;
        vh.imgVideoThumbnail.setLayoutParams(params);

        ViewGroup.LayoutParams paramsImgPlay = vh.imgPlay.getLayoutParams();
        paramsImgPlay.width = screenUtils.getWidthPx() - marginLeft - marginRight;
        paramsImgPlay.height = params.width;
        vh.imgPlay.setLayoutParams(paramsImgPlay);

        subscriptions.add(RxView.clicks(vh.imgVideoThumbnail)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> vh.imgVideoThumbnail)
                .subscribe(clickVideo));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        super.onBindViewHolder(items, position, holder);
        VideoViewHolder vh = (VideoViewHolder) holder;

        ChatMessage chatMessage = items.get(position);

        vh.imgVideoThumbnail.setTag(R.id.tag_position, position);
    }

    @Override
    protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
        return new VideoViewHolder(layoutInflater.inflate(R.layout.item_text_video, parent, false));
    }

    public PublishSubject<ImageView> clickVideo() {
        return clickVideo;
    }

    static class VideoViewHolder extends BaseTextViewHolder {

        @BindView(R.id.imgVideoThumbnail) public ImageView imgVideoThumbnail;
        @BindView(R.id.imgPlay) public ImageView imgPlay;
        @BindView(R.id.layoutContent) public ViewGroup layoutContent;

        public VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected ViewGroup getLayoutContent() {
            return layoutContent;
        }
    }
}
