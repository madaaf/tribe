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
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class TribeGuestAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;

  // OBSERVABLES
  private PublishSubject<View> onClickInvite = PublishSubject.create();

  public TribeGuestAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof TribeGuest;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    TribeGuestViewHolder vh =
        new TribeGuestViewHolder(layoutInflater.inflate(R.layout.item_guest, parent, false));

    vh.btnAdd.setOnClickListener(v -> onClickInvite.onNext(vh.itemView));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    TribeGuestViewHolder vh = (TribeGuestViewHolder) holder;

    vh.txtName.setText(context.getString(R.string.live_external_user_display_name));
    vh.txtDescription.setText(context.getString(R.string.live_external_user_username));
    vh.viewAvatar.setHasShadow(false);
    vh.viewAvatar.load("");
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  public Observable<View> onClickInvite() {
    return onClickInvite;
  }

  static class TribeGuestViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtDescription;

    @BindView(R.id.btnAdd) View btnAdd;

    @BindView(R.id.viewAvatar) AvatarView viewAvatar;

    public TribeGuestViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
