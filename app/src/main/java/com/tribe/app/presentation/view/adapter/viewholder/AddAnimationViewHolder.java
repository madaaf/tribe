package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;

import butterknife.BindView;

/**
 * Created by tiago on 01/10/2016.
 */
public class AddAnimationViewHolder extends RecyclerView.ViewHolder {

  public AddAnimationViewHolder(View itemView) {
    super(itemView);
  }

  @BindView(R.id.btnAdd) public View btnAdd;

  @BindView(R.id.btnAddBG) public View btnAddBG;

  @BindView(R.id.imgPicto) public ImageView imgPicto;

  @BindView(R.id.progressBarAdd) public CircularProgressView progressBarAdd;
}
