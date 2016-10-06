package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.utils.StringUtils;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class SupportGridAdapterDelegate extends RecipientGridAdapterDelegate {

    public SupportGridAdapterDelegate(Context context) {
        super(context);
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return position != 0 && items.get(position) instanceof Friendship
                && (!StringUtils.isEmpty(items.get(position).getSubId())
                || items.get(position).getSubId().equals("XSUPPORT"));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.item_support_grid;
    }
}