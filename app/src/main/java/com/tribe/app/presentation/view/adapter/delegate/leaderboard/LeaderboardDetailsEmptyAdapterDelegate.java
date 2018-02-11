package com.tribe.app.presentation.view.adapter.delegate.leaderboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import java.util.List;

/**
 * Created by tiago on 02/07/18
 */
public class LeaderboardDetailsEmptyAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  public LeaderboardDetailsEmptyAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    if (items.get(position) instanceof Score) {
      Score score = items.get(position);
      return score.getId() == null;
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardEmptyViewHolder vh = new LeaderboardEmptyViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_empty_details, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    Score score = items.get(position);
    LeaderboardEmptyViewHolder vh = (LeaderboardEmptyViewHolder) holder;

    if (position == 0) {
      vh.imgConnectTop.setVisibility(View.GONE);
    } else {
      vh.imgConnectTop.setVisibility(View.VISIBLE);
    }

    if (position == items.size() - 1) {
      vh.imgConnectBottom.setVisibility(View.GONE);
    } else {
      vh.imgConnectBottom.setVisibility(View.VISIBLE);
    }

    if (position % 2 == 0) {
      vh.itemView.setBackgroundResource(android.R.color.transparent);
    } else {
      vh.itemView.setBackgroundResource(R.color.black_opacity_5);
    }

    if (vh.drawable == null) {
      GradientDrawable drawable = new GradientDrawable();
      drawable.setShape(GradientDrawable.OVAL);
      drawable.setColor(Color.parseColor("#" + score.getGame().getPrimary_color()));
      vh.imgAvatarEmpty.setImageDrawable(drawable);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardEmptyViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgConnectBottom) ImageView imgConnectBottom;
    @BindView(R.id.imgConnectTop) ImageView imgConnectTop;
    @BindView(R.id.imgAvatarEmpty) ImageView imgAvatarEmpty;

    GradientDrawable drawable;

    public LeaderboardEmptyViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
