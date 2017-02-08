package com.tribe.app.presentation.view.adapter.viewholder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;

import butterknife.BindView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import org.w3c.dom.Text;

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
}
