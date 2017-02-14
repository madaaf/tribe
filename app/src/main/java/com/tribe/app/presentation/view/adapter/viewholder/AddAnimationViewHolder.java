package com.tribe.app.presentation.view.adapter.viewholder;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by tiago on 01/10/2016.
 */
public class AddAnimationViewHolder extends RecyclerView.ViewHolder {

  public AddAnimationViewHolder(View itemView) {
    super(itemView);
  }

  @BindView(R.id.btnAdd) public View btnAdd;

  @BindView(R.id.txtAction) public TextViewFont txtAction;

  @Nullable @BindView(R.id.progressBarAdd) public CircularProgressView progressBarAdd;

  public GradientDrawable gradientDrawable;
}
