package com.tribe.app.presentation.view.adapter.filter;

import android.widget.Filter;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.FriendMembersAdapter;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberListFilter extends Filter {

  private List<Object> userList;
  private List<Object> filteredUserList;
  private FriendMembersAdapter adapter;

  public GroupMemberListFilter(List<Object> userList, FriendMembersAdapter adapter) {
    this.userList = userList;
    this.filteredUserList = new ArrayList();
    this.adapter = adapter;
  }

  @Override protected FilterResults performFiltering(CharSequence constraint) {
    filteredUserList.clear();
    final FilterResults results = new FilterResults();

    if (userList != null && userList.size() > 0) {
      for (int i = 0; i < userList.size(); i++) {
        if (userList.get(i) instanceof GroupMember) {
          GroupMember item = (GroupMember) userList.get(i);

          if (item.getUser()
              .getDisplayName()
              .toLowerCase()
              .startsWith(constraint.toString().toLowerCase())) {
            filteredUserList.add(item);
          }
        } else {
          filteredUserList.add(userList.get(i));
        }
      }
    }

    results.values = filteredUserList;
    results.count = filteredUserList.size();
    return results;
  }

  @Override protected void publishResults(CharSequence constraint, FilterResults results) {
    adapter.setFilteredItems(filteredUserList);
    adapter.notifyDataSetChanged();
  }
}