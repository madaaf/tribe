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
import com.tribe.app.presentation.view.component.games.LeaderboardUserView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter.LEADERBOARD_ADDRESS;

/**
 * Created by tiago on 12/08/17.
 */
public class LeaderboardDetailsAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  private static final int DURATION = 300;
  private static final int TRANSLATION = 200;

  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  // OBSERVABLES
  protected PublishSubject<Score> onClick = PublishSubject.create();
  protected PublishSubject<Score> onClickPoke = PublishSubject.create();

  public LeaderboardDetailsAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    if (items.get(position) instanceof Score) {
      Score score = items.get(position);
      return score.getId() != null &&
          !score.getId().equals(Score.ID_PROGRESS) &&
          !score.getId().startsWith(LEADERBOARD_ADDRESS);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_details, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    Score score = items.get(position);
    vh.viewLeaderboardUser.initCell(score, position);
    vh.viewLeaderboardUser.initPoke(score);
    vh.viewLeaderboardUser.manageListItems(position, items.size() - 1);
    subscriptions.add(vh.viewLeaderboardUser.onClick().subscribe(onClick));
    subscriptions.add(vh.viewLeaderboardUser.onClickPoke().subscribe(onClickPoke));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewLeaderboardUser) LeaderboardUserView viewLeaderboardUser;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<Score> onClickPoke() {
    return onClickPoke;
  }

  public Observable<Score> onClick() {
    return onClick;
  }
}
