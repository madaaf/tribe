package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 06/02/17.
 */
public class GameAdapterDelegate extends RxAdapterDelegate<List<Game>> {

  protected static final int DURATION = 100;
  protected static final float OVERSHOOT_LIGHT = 0.45f;

  @Inject ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;
  protected int sizeDisabled, sizeEnabled;

  protected PublishSubject<View> click = PublishSubject.create();

  public GameAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    sizeDisabled = context.getResources().getDimensionPixelSize(R.dimen.filter_game_size) -
        screenUtils.dpToPx(5);
    sizeEnabled =
        context.getResources().getDimensionPixelSize(R.dimen.filter_game_size_with_border);
  }

  @Override public boolean isForViewType(@NonNull List<Game> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final GameViewHolder vh =
        new GameViewHolder(layoutInflater.inflate(R.layout.item_game, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Game> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GameViewHolder vh = (GameViewHolder) holder;
    Game game = items.get(position);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Game> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  static class GameViewHolder extends RecyclerView.ViewHolder {

    public GameViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
