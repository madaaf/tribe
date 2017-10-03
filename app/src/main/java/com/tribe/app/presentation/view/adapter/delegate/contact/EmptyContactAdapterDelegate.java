package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import java.util.List;

/**
 * Created by tiago on 10/03/2017.
 */
public class EmptyContactAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private Context context;
  private LayoutInflater layoutInflater;

  public EmptyContactAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    EmptyContactViewHolder vh = new EmptyContactViewHolder(
        layoutInflater.inflate(R.layout.item_contact_empty, parent, false));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) items.get(position);
      return shortcut.getId().equals(Recipient.ID_EMPTY);
    } else {
      return false;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {

  }

  class EmptyContactViewHolder extends RecyclerView.ViewHolder {

    public EmptyContactViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
