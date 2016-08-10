package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserGridAdapterDelegate extends RecipientGridAdapterDelegate {

    public UserGridAdapterDelegate(Context context) {
        super(context);
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return position != 0 && items.get(position) instanceof Friendship;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.item_user_grid;
    }
}
