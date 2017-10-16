package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.helper.ItemTouchHelperViewHolder;
import com.tribe.app.presentation.view.component.common.ShortcutListView;
import timber.log.Timber;

public class RecipientHomeViewHolder extends RecyclerView.ViewHolder
    implements ItemTouchHelperViewHolder {

  @BindView(R.id.viewListItem) public ShortcutListView viewListItem;

  public RecipientHomeViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @Override public void onItemSelected() {
    Timber.d("onItemSelected");
  }

  @Override public void onItemClear() {
    Timber.d("onItemClear");
  }
}