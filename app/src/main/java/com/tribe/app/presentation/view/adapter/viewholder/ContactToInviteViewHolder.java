package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

public class ContactToInviteViewHolder extends RecyclerView.ViewHolder {

  public ContactToInviteViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @BindView(R.id.txtName) public TextViewFont txtName;

  @BindView(R.id.txtDetails) public TextViewFont txtDetails;

  @BindView(R.id.btnInvite) public ImageView btnInvite;
}