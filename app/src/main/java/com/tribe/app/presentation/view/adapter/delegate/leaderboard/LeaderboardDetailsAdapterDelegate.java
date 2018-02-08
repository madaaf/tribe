package com.tribe.app.presentation.view.adapter.delegate.leaderboard;

import android.content.Context;
import android.graphics.Color;
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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 12/08/17.
 */
public class LeaderboardDetailsAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  protected PublishSubject<View> click = PublishSubject.create();

  public LeaderboardDetailsAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    if (items.get(position) instanceof Score) {
      Score score = items.get(position);
      return score.getId() != null && !score.getId().equals(Score.ID_PROGRESS);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_details, parent, false));
    vh.itemView.setOnClickListener(v -> click.onNext(vh.itemView));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    Score score = items.get(position);
    score.setRanking(position + 1);
    vh.viewAvatar.load(score.getUser().getProfilePicture());

    vh.txtRanking.setRanking(position + 3);
    vh.txtRanking.setTextColor(Color.parseColor("#" + score.getGame().getPrimary_color()));

    vh.txtName.setText(score.getUser().getDisplayName());
    vh.txtScore.setScore(score.getValue());

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
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;
    @BindView(R.id.txtRanking) TextViewRanking txtRanking;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtScore) TextViewScore txtScore;
    @BindView(R.id.imgConnectTop) ImageView imgConnectTop;
    @BindView(R.id.imgConnectBottom) ImageView imgConnectBottom;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
