package com.tribe.app.presentation.view.adapter.filter;

import android.widget.Filter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.BlockedFriendAdapter;

import java.util.ArrayList;
import java.util.List;

public class BlockedFriendsFilter extends Filter {

    private List<Friendship> friendshipList;
    private List<Friendship> filteredFriendshipList;
    private BlockedFriendAdapter adapter;

    public BlockedFriendsFilter(List<Friendship> friendshipList, BlockedFriendAdapter adapter) {
        this.friendshipList = friendshipList;
        this.filteredFriendshipList = new ArrayList();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredFriendshipList.clear();
        final FilterResults results = new FilterResults();

        if (friendshipList != null && friendshipList.size() > 0) {
            for (final Friendship item : friendshipList) {
                String firstCharacter = StringUtils.getFirstCharacter(item.getDisplayName());

                if (!firstCharacter.isEmpty() && firstCharacter.equalsIgnoreCase(constraint.toString())) {
                    filteredFriendshipList.add(item);
                }
            }
        }

        results.values = filteredFriendshipList;
        results.count = filteredFriendshipList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.setFilteredItems(filteredFriendshipList);
        adapter.notifyDataSetChanged();
    }
}