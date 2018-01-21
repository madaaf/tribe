package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 06/02/17.
 */
public class FooterAdapterDelegate extends RxAdapterDelegate<List<GameFooter>> {

  protected static final int DURATION = 100;
  protected static final float OVERSHOOT_LIGHT = 0.45f;

  @Inject ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;
  private int radius;

  protected PublishSubject<View> click = PublishSubject.create();

  public FooterAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    radius = screenUtils.dpToPx(6);
  }

  @Override public boolean isForViewType(@NonNull List<GameFooter> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final GameFooterViewHolder vh =
        new GameFooterViewHolder(layoutInflater.inflate(R.layout.item_footer, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<GameFooter> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GameFooterViewHolder vh = (GameFooterViewHolder) holder;
    GameFooter item = items.get(position);

    if (item.getId().equals(Game.GAME_SUPPORT)) {
      vh.supportItem.setVisibility(View.VISIBLE);
      vh.supportItem.setOnClickListener(v -> click.onNext(vh.itemView));

      return;
    } else {
      vh.supportItem.setVisibility(View.GONE);
    }

    if (item.getId().equals(Game.GAME_LOGO)) {
      vh.imgLogo.setVisibility(View.VISIBLE);
      return;
    } else {
      vh.imgLogo.setVisibility(View.GONE);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<GameFooter> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  static class GameFooterViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.supportItem) TextViewFont supportItem;
    @BindView(R.id.imgLogo) ImageView imgLogo;

    public GameFooterViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
