package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserMessageAdapterDelegate implements AdapterDelegate<List<Message>> {

    private LayoutInflater layoutInflater;

    public UserMessageAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Message> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new UserTextViewHolder(layoutInflater.inflate(R.layout.item_user_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Message> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserTextViewHolder vh = (UserTextViewHolder) holder;
        Message message = (Message) items.get(position);

        vh.txtMessage.setText(message.getText());
    }

    static class UserTextViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtMessage) public TextViewFont txtMessage;

        public UserTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
