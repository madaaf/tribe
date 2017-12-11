package com.tribe.app.presentation.view.adapter.delegate.leaderboard;

import android.content.Context;
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
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 12/08/17.
 */
public class LeaderboardUserAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  @Inject ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;
  protected boolean canClick = true;

  protected PublishSubject<View> click = PublishSubject.create();

  public LeaderboardUserAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    return items.get(position) != null;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_user, parent, false));
    if (canClick) vh.layoutContent.setOnClickListener(v -> click.onNext(vh.itemView));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    Score score = items.get(position);

    vh.txtName.setText(score.getGame().getTitle());

    new GlideUtils.GameImageBuilder(context, screenUtils).url(score.getGame().getIcon())
        .hasBorder(true)
        .hasPlaceholder(true)
        .rounded(true)
        .target(vh.imgIcon)
        .load();

    vh.txtPoints.setText("" + score.getValue());

    if (!canClick) vh.imgArrow.setVisibility(View.GONE);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  public void setCanClick(boolean canClick) {
    this.canClick = canClick;
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.layoutContent) ViewGroup layoutContent;
    @BindView(R.id.imgIcon) ImageView imgIcon;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtPoints) TextViewFont txtPoints;
    @BindView(R.id.txtPointsSuffix) TextViewFont txtPointsSuffix;
    @BindView(R.id.imgArrow) ImageView imgArrow;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
