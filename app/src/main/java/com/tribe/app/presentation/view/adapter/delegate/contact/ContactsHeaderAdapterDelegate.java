package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;

/**
 * Created by tiago on 20/09/2016.
 */
public class ContactsHeaderAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;

  public ContactsHeaderAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof Integer);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ContactsHeaderViewHolder vh = new ContactsHeaderViewHolder(
        layoutInflater.inflate(R.layout.item_contacts_header, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ContactsHeaderViewHolder vh = (ContactsHeaderViewHolder) holder;
    vh.txtSubLabel.setText((Integer) items.get(position));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  public static class ContactsHeaderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtSubLabel) TextViewFont txtSubLabel;

    public ContactsHeaderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}