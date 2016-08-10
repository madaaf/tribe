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
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class TutorialMessageAdapterDelegate implements AdapterDelegate<List<Message>> {

    protected LayoutInflater layoutInflater;

    public TutorialMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        this.layoutInflater = inflater;
    }

    @Override
    public boolean isForViewType(@NonNull List<Message> items, int position) {
        //if (position == 0) return true;
        //else
            return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new TutorialMessageViewHolder(layoutInflater.inflate(R.layout.item_chat_tutorial, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Message> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        TutorialMessageViewHolder vh = (TutorialMessageViewHolder) holder;
        Message message = items.get(position);

        vh.txtName.setText(message.getTo().getDisplayName());
    }

    static class TutorialMessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.avatar) public AvatarView avatarView;

        public TutorialMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
