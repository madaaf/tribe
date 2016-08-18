package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class TutorialMessageAdapterDelegate implements AdapterDelegate<List<ChatMessage>> {

    @Inject
    Picasso picasso;

    // VARIABLES
    protected LayoutInflater layoutInflater;

    // RESOURCES
    private int avatarSize;

    public TutorialMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        this.layoutInflater = inflater;

        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        return items.get(position).isTutorial();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new TutorialMessageViewHolder(layoutInflater.inflate(R.layout.item_chat_tutorial, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        TutorialMessageViewHolder vh = (TutorialMessageViewHolder) holder;
        ChatMessage chatMessage = items.get(position);

        vh.txtName.setText(chatMessage.getTo().getDisplayName());
        vh.txtUsername.setText(chatMessage.getTo().getUsername());
        picasso.load(chatMessage.getTo().getProfilePicture())
                .fit()
                .centerCrop()
                .transform(new RoundedCornersTransformation(avatarSize >> 1, 0, RoundedCornersTransformation.CornerType.ALL))
                .into(vh.imgAvatar);
    }

    static class TutorialMessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.txtUsername) public TextViewFont txtUsername;
        @BindView(R.id.imgAvatar) public ImageView imgAvatar;

        public TutorialMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
