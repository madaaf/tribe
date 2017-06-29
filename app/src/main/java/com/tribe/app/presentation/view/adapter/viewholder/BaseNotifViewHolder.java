package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;

/**
 * Created by madaaflak on 29/06/2017.
 */

public class BaseNotifViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.txtName) public TextViewFont txtName;

  @BindView(R.id.txtUsername) public TextViewFont txtDescription;

  @BindView(R.id.btnAdd) public View btnAdd;

  @BindView(R.id.viewAvatar) public AvatarView viewAvatar;

  @BindView(R.id.btnMore) public ImageView btnMore;

  @BindView(R.id.iconAdd) public ImageView iconAdd;

  @BindView(R.id.txtAction) public TextViewFont txtAction;

  @BindView(R.id.layoutAddFriend) public FrameLayout layoutAddFriend;

  @BindView(R.id.AddBtnBg) public FrameLayout addBtnBg;

  public BaseNotifViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }
}