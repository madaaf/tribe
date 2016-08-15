package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 08/09/2016.
 */
public class RegularMessageAdapterDelegate extends BaseMessageAdapterDelegate {

    public RegularMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        super(inflater, context);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        ChatMessage chatMessage = items.get(position);
        return !chatMessage.isHeader() && chatMessage.getType().equals(ChatMessage.TEXT) && !chatMessage.isOnlyEmoji() && !chatMessage.isLink();
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        super.onBindViewHolder(items, position, holder);

        ChatMessage chatMessage = items.get(position);

        RegularViewHolder vh = (RegularViewHolder) holder;
        vh.txtMessage.setText(chatMessage.getContent());
    }

    @Override
    protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
        return new RegularViewHolder(layoutInflater.inflate(R.layout.item_text, parent, false));
    }

    static class RegularViewHolder extends BaseTextViewHolder {

        @BindView(R.id.txtMessage) public TextViewFont txtMessage;

        public RegularViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}