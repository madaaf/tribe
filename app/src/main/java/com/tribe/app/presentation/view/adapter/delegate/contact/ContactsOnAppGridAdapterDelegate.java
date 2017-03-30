package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 03/29/2017.
 */
public class ContactsOnAppGridAdapterDelegate extends BaseListAdapterDelegate {

  // OBSERVABLES
  private PublishSubject<View> onClickInvite = PublishSubject.create();

  public ContactsOnAppGridAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof Contact) {
      Contact contact = (Contact) items.get(position);
      return (contact.getUserList() != null && contact.getUserList().size() > 0);
    }

    return false;
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    return null;
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    return null;
  }

  @Override protected void setClicks(BaseListInterface baseList, BaseListViewHolder vh) {

  }

  public Observable<View> onClickInvite() {
    return onClickInvite;
  }

  static class ContactViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtDescription) TextViewFont txtDescription;

    @BindView(R.id.btnAdd) View btnAdd;

    public ContactViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
