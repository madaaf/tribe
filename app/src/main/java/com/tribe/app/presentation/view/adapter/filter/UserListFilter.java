package com.tribe.app.presentation.view.adapter.filter;

import android.widget.Filter;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.UserListAdapter;
import java.util.ArrayList;
import java.util.List;

public class UserListFilter extends Filter {

  private List<Object> userList;
  private List<Object> filteredUserList;
  private UserListAdapter adapter;

  public UserListFilter(List<Object> userList, UserListAdapter adapter) {
    this.userList = userList;
    this.filteredUserList = new ArrayList();
    this.adapter = adapter;
  }

  @Override protected FilterResults performFiltering(CharSequence constraint) {
    filteredUserList.clear();
    final FilterResults results = new FilterResults();

    if (userList != null && userList.size() > 0) {
      for (final Object item : userList) {
        if (item instanceof User) {
          User user = (User) item;

          if (user.getDisplayName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
            filteredUserList.add(item);
          }
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