package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;

import java.util.List;

/**
 * Created by tiago on 11/29/16.
 */
public class UserListEmptyAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

    // VARIABLES
    private int avatarSize;

    public UserListEmptyAdapterDelegate(Context context) {
        super(context);
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    }

    @Override
    public boolean isForViewType(@NonNull List<Object> items, int position) {
        if (items.get(position) instanceof User) {
            User user = (User) items.get(position);
            return user.getId().equals(User.ID_EMPTY);
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new UserListViewHolder(
                layoutInflater.inflate(R.layout.item_user_empty_list, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {

    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
                                 int position, List<Object> payloads) {

    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {

        public UserListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
