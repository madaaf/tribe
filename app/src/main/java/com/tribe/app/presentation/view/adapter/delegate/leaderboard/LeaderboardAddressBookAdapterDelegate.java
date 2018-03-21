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
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter.LEADERBOARD_ADDRESS;
import static com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter.LEADERBOARD_ITEM_ADDRESS_BOOK;
import static com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter.LEADERBOARD_ITEM_FACEBOOK;

/**
 * Created by tiago on 12/08/17.
 */
public class LeaderboardAddressBookAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  // RX SUBSCRIPTIONS / SUBJECTS
  @Inject User user;

  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  protected PublishSubject<Score> onClick = PublishSubject.create();

  public LeaderboardAddressBookAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    if (items.get(position) instanceof Score) {
      Score score = items.get(position);
      return score.getId().startsWith(LEADERBOARD_ADDRESS);
    }
    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_address, parent, false));
    return vh;
  }

  public static Integer randInt(int low, int high) {
    Random r = new Random();
    return r.nextInt(high - low) + low;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    Score score = items.get(position);

    if (score.getId().equals(LEADERBOARD_ITEM_ADDRESS_BOOK)) {
      vh.txtName.setText(context.getString(R.string.leaderboard_address_book_title));
      vh.logo.setImageResource(R.drawable.picto_address_book_white);
    }
    if (score.getId().equals(LEADERBOARD_ITEM_FACEBOOK)) {
      vh.txtName.setText(context.getString(R.string.leaderboard_facebook_title));
      vh.logo.setImageResource(R.drawable.com_facebook_button_icon_white);
    }

    score.setPosition(position);
    vh.itemView.setOnClickListener(v -> onClick.onNext(score));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtDetails) TextViewFont txtDetails;
    @BindView(R.id.logo) ImageView logo;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<Score> onClick() {
    return onClick;
  }
}
