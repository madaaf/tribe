package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class GroupGridAdapterDelegate extends FriendshipGridAdapterDelegate {

    public GroupGridAdapterDelegate(Context context) {
        super(context);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return position != 0 && items.get(position) instanceof Group;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_user_grid;
    }
}
