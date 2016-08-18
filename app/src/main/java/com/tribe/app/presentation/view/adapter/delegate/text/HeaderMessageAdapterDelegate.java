package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.text.DateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 08/09/2016.
 */
public abstract class HeaderMessageAdapterDelegate extends RxAdapterDelegate<List<ChatMessage>> {

    // VARIABLES
    private DateFormat dateFormat;
    private LayoutInflater layoutInflater;

    // RESOURCES
    private String todayStr;

    public HeaderMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        ApplicationComponent appComponent = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent();
        this.dateFormat = appComponent.fullLetteredDate();
        this.layoutInflater = inflater;
        this.todayStr = context.getString(R.string.chat_date_today);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new HeaderTextViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        HeaderTextViewHolder vh = (HeaderTextViewHolder) holder;
        ChatMessage chatMessage = items.get(position);

        String dateFormatted = dateFormat.format(chatMessage.getCreatedAt());
        dateFormatted = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
        vh.txtMessage.setText(chatMessage.isToday() ? todayStr + " - " + dateFormatted : dateFormatted);
    }

    protected abstract int getLayoutId();

    static class HeaderTextViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtMessage) public TextViewFont txtMessage;

        public HeaderTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
