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
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;

/**
 * Created by tiago on 01/02/2017.
 */
public class ContactToInviteAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private Context context;
  private LayoutInflater layoutInflater;

  public ContactToInviteAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ContactToInviteViewHolder vh = new ContactToInviteViewHolder(
        layoutInflater.inflate(R.layout.item_contact_to_invite, parent, false));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof Contact);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ContactToInviteViewHolder vh = (ContactToInviteViewHolder) holder;
    Contact contact = (Contact) items.get(position);

    vh.txtName.setText(contact.getName());
    vh.txtDetails.setText(context.getString(R.string.contacts_section_addressbook_friends_in_app,
        contact.getHowManyFriends()));
  }

  class ContactToInviteViewHolder extends RecyclerView.ViewHolder {

    public ContactToInviteViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @BindView(R.id.txtName) public TextViewFont txtName;

    @BindView(R.id.txtDetails) public TextViewFont txtDetails;
  }
}
