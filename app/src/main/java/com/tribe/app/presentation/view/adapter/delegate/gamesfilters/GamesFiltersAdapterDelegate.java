package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.entity.GameFilter;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 06/02/17.
 */
public abstract class GamesFiltersAdapterDelegate extends RxAdapterDelegate<List<GameFilter>> {

  protected static final int DURATION = 100;
  protected static final float OVERSHOOT_LIGHT = 0.45f;

  @Inject ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;
  protected int sizeDisabled, sizeEnabled;

  protected PublishSubject<View> click = PublishSubject.create();

  public GamesFiltersAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    sizeDisabled =
        context.getResources().getDimensionPixelSize(R.dimen.filter_game_size) - screenUtils.dpToPx(
            5);
    sizeEnabled =
        context.getResources().getDimensionPixelSize(R.dimen.filter_game_size_with_border);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final GameFilterViewHolder vh =
        new GameFilterViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

    vh.image.setOnClickListener(v -> click.onNext(vh.itemView));
    UIUtils.changeSizeOfView(vh.bgSelected, sizeDisabled);

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<GameFilter> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GameFilterViewHolder vh = (GameFilterViewHolder) holder;
    GameFilter gameFilter = items.get(position);

    vh.itemView.setVisibility(View.VISIBLE);

    if (gameFilter.isActivated() && vh.bgSelected.getWidth() <= sizeDisabled) {
      scale(vh, true);
    } else if (vh.bgSelected.getWidth() >= sizeEnabled) {
      scale(vh, false);
    }

    new GlideUtils.Builder(context).hasPlaceholder(false)
        .rounded(false)
        .resourceId(gameFilter.getDrawableRes())
        .target(vh.image)
        .load();
  }

  @Override public void onBindViewHolder(@NonNull List<GameFilter> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  protected abstract int getLayoutId();

  private void scale(GameFilterViewHolder vh, boolean up) {
    AnimationUtils.animateSize(vh.bgSelected, up ? sizeDisabled : sizeEnabled,
        up ? sizeEnabled : sizeDisabled, DURATION, new DecelerateInterpolator());

    if (up) {
      AnimationUtils.makeItBounce(vh.itemView, DURATION * 3,
          new OvershootInterpolator(OVERSHOOT_LIGHT));
    }
  }

  static class GameFilterViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.bgSelected) View bgSelected;

    public GameFilterViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
