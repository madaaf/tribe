package com.tribe.app.presentation.view.adapter.filter;

import android.widget.Filter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.FriendMembersAdapter;
import com.tribe.app.presentation.view.adapter.BlockedFriendAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.filter;

public class UserListFilter extends Filter {

    private List<User> userList;
    private List<User> filteredUserList;
    private FriendMembersAdapter adapter;

    public UserListFilter(List<User> userList, FriendMembersAdapter adapter) {
        this.userList = userList;
        this.filteredUserList = new ArrayList();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredUserList.clear();
        final FilterResults results = new FilterResults();

        if (userList != null && userList.size() > 0) {
            for (final User item : userList) {
                String firstCharacter = StringUtils.getFirstCharacter(item.getDisplayName());

                if (!firstCharacter.isEmpty() && firstCharacter.equalsIgnoreCase(constraint.toString())) {
                    filteredUserList.add(item);
                }
            }
        }

        results.values = filteredUserList;
        results.count = filteredUserList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.setFilteredItems(filteredUserList);
        adapter.notifyDataSetChanged();
    }
}