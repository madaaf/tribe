package com.tribe.app.presentation.view.adapter.filter;

import android.widget.Filter;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.component.FilterView;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class RecipientFilter extends Filter {

    private List<Recipient> recipientList;
    private List<Recipient> filteredRecipientList;
    private HomeGridAdapter adapter;
    private ScreenUtils screenUtils;
    private String filter;

    public RecipientFilter(ScreenUtils screenUtils, List<Recipient> recipientList, HomeGridAdapter adapter) {
        this.screenUtils = screenUtils;
        this.adapter = adapter;
        this.recipientList = recipientList;
        this.filteredRecipientList = new ArrayList();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filter = constraint.toString();
        filteredRecipientList.clear();
        final FilterResults results = new FilterResults();

        if (recipientList != null && recipientList.size() > 0) {
            for (final Recipient item : recipientList) {
                if (!item.getId().equals(Recipient.ID_EMPTY) && FilterView.shouldFilter(constraint.toString(), item)) {
                    filteredRecipientList.add(item);
                }
            }

            ListUtils.addEmptyItems(screenUtils, filteredRecipientList);
        }

        results.values = filteredRecipientList;
        results.count = filteredRecipientList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.setFilteredItems(filteredRecipientList);
        adapter.notifyDataSetChanged();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}