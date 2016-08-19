package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public abstract class BaseMessageAdapterDelegate extends RxAdapterDelegate<List<ChatMessage>> {

    // VARIABLES
    protected Context context;
    protected User currentUser;
    protected SimpleDateFormat simpleDateFormat;
    protected LayoutInflater layoutInflater;

    // RESOURCES
    protected int marginVerticalSmall;
    protected int marginVerticalXSmall;

    public BaseMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        this.context = context;
        this.currentUser = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().currentUser();
        this.simpleDateFormat = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().simpleDateHoursMinutes();
        this.layoutInflater = inflater;
        this.marginVerticalSmall = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);
        this.marginVerticalXSmall = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_xsmall);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        ChatMessage chatMessage = items.get(position);
        return !chatMessage.isHeader();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return getViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        BaseTextViewHolder vh = (BaseTextViewHolder) holder;
        ChatMessage chatMessage = items.get(position);

        holder.itemView.setTag(R.id.chat_id, chatMessage.getId());

        if (!chatMessage.isShouldDisplayTime() && !chatMessage.isOtherPerson()) {
           vh.layoutInfos.setVisibility(View.GONE);
        } else {
            vh.layoutInfos.setVisibility(View.VISIBLE);

            if (chatMessage.isOtherPerson()) {
                vh.txtName.setVisibility(View.VISIBLE);
                vh.txtName.setText(chatMessage.getFrom().getDisplayName());
                if (chatMessage.getFrom().equals(currentUser)) vh.txtName.setTextColor(context.getResources().getColor(R.color.blue_text));
                else vh.txtName.setTextColor(Color.BLACK);
            } else {
                vh.txtName.setVisibility(View.GONE);
            }

            vh.txtTime.setText(simpleDateFormat.format(chatMessage.getCreatedAt()));
        }

        if (chatMessage.isFirstOfSection() || chatMessage.isLastOfSection() || chatMessage.isOtherPerson()) {
            vh.itemView.setPadding(vh.itemView.getPaddingLeft(),
                    (chatMessage.isFirstOfSection() || chatMessage.isOtherPerson()) ? marginVerticalSmall : marginVerticalXSmall,
                    vh.itemView.getPaddingRight(),
                    chatMessage.isLastOfSection() ? marginVerticalSmall : 0);
        } else {
            vh.itemView.setPadding(vh.itemView.getPaddingLeft(), marginVerticalXSmall, vh.itemView.getPaddingRight(), 0);
        }
    }

    protected abstract BaseTextViewHolder getViewHolder(ViewGroup parent);

    static class BaseTextViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.txtTime) public TextViewFont txtTime;
        @BindView(R.id.layoutInfos) public ViewGroup layoutInfos;

        public BaseTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
