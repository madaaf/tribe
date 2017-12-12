package com.tribe.app.presentation.view.adapter.delegate.leaderboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.component.games.GameUserCardView;
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
      return !score.getId().equals(Score.ID_PROGRESS);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_details, parent, false));
    vh.viewGameUserCard.setOnClickListener(v -> click.onNext(vh.itemView));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    vh.viewGameUserCard.setScore(items.get(position));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewGameUserCard) GameUserCardView viewGameUserCard;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
