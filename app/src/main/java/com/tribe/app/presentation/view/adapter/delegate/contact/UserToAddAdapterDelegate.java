package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;

/**
 * Created by tiago on 01/02/2017.
 */
public class UserToAddAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private Context context;
  private LayoutInflater layoutInflater;

  public UserToAddAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    UserToAddViewHolder vh =
        new UserToAddViewHolder(layoutInflater.inflate(R.layout.item_user_to_add, parent, false));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof User);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    UserToAddViewHolder vh = (UserToAddViewHolder) holder;
    User user = (User) items.get(position);

    vh.txtName.setText(user.getDisplayName());
    vh.txtUsername.setText(user.getUsername());
    vh.viewNewAvatar.load(user.getProfilePicture());
    vh.viewNew.setVisibility(user.isNew() ? View.VISIBLE : View.GONE);
  }

  class UserToAddViewHolder extends RecyclerView.ViewHolder {

    public UserToAddViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @BindView(R.id.viewNewAvatar) public NewAvatarView viewNewAvatar;

    @BindView(R.id.txtName) public TextViewFont txtName;

    @BindView(R.id.txtUsername) public TextViewFont txtUsername;

    @BindView(R.id.viewNew) public View viewNew;

    @BindView(R.id.btnAdd) public ImageView btnAdd;
  }
}
