package com.tribe.app.presentation.view.adapter.viewholder;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;

/**
 * Created by tiago on 01/10/2016.
 */

public class BaseListViewHolder extends RecyclerView.ViewHolder {

  public BaseListViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @BindView(R.id.viewNewAvatar) public NewAvatarView viewAvatar;

  @BindView(R.id.txtName) public TextViewFont txtName;

  @BindView(R.id.txtNew) public TextViewFont txtNew;

  @BindView(R.id.layoutInfos) public ViewGroup layoutInfos;

  @BindView(R.id.txtUsername) public TextViewFont txtUsername;

  @BindView(R.id.txtFriend) public TextViewFont txtFriend;

  @BindView(R.id.txtBubble) public TextViewFont txtBubble;

  @BindView(R.id.progressView) public CircularProgressView progressView;

  @BindView(R.id.btnAdd) public ImageView btnAdd;

  public GradientDrawable gradientDrawable;

  public ButtonModel buttonModelFrom;

  public ButtonModel buttonModelTo;
}
