package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.utils.Constants;

import java.util.List;

/**
 * Created by tiago on 01/19/2017.
 */
public class UserLiveCoInviteAdapterDelegate extends RecipientGridAdapterDelegate {

    public UserLiveCoInviteAdapterDelegate(Context context) {
        super(context);
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return items.get(position) instanceof Friendship
                && !items.get(position).getSubId().equals(Recipient.ID_HEADER)
                && !items.get(position).getSubId().equals(Recipient.ID_EMPTY)
                && (items.get(position).isOnline() || items.get(position).isLive());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecipientGridViewHolder vh = (RecipientGridViewHolder) super.onCreateViewHolder(parent);
        return vh;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_user_live_co_invite;
    }
}
