package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;

/**
 * Created by madaaflak on 29/06/2017.
 */

public class BaseNotifViewHolder extends RecyclerView.ViewHolder {

  @Nullable @BindView(R.id.layoutContentok) public ViewGroup layoutContent;

  @Nullable @BindView(R.id.txtName) public TextViewFont txtName;

  @Nullable @BindView(R.id.txtUsername) public TextViewFont txtDescription;

  @Nullable @BindView(R.id.btnAdd) public ImageView btnAdd;

  @Nullable @BindView(R.id.viewNewAvatar) public NewAvatarView viewAvatar;

  @Nullable @BindView(R.id.btnMore) public ImageView btnMore;

  @Nullable @BindView(R.id.progressView) public CircularProgressView progressView;

  @Nullable @BindView(R.id.txtAction) public TextViewFont txtAction;

  @Nullable @BindView(R.id.txtPoints) public TextViewFont txtPoints;

  @Nullable @BindView(R.id.imgIcon) public ImageView imgIcon;

  //@Nullable @BindView(R.id.txtRanking) public TextViewFont txtRanking;

  @Nullable @BindView(R.id.txtPointsSuffix) public TextViewFont txtPointsSuffix;

  @Nullable @BindView(R.id.separator) public View separator;

  @Nullable @BindView(R.id.txtBestScore) public TextViewFont txtBestScore;

  @Nullable @BindView(R.id.layoutUser) public ViewGroup layoutUser;

  @Nullable @BindView(R.id.layoutGame) public ViewGroup layoutGame;

  public BaseNotifViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }
}