package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Message;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class TodayHeaderMessageAdapterDelegate extends HeaderMessageAdapterDelegate {

    public TodayHeaderMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        super(inflater, context);
    }

    @Override
    public boolean isForViewType(@NonNull List<Message> items, int position) {
        return items.get(position).isHeader() && items.get(position).isToday();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_text_header_today;
    }
}
