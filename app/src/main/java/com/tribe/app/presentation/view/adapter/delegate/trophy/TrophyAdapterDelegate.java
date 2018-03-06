package com.tribe.app.presentation.view.adapter.delegate.trophy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 03/02/18.
 */
public class TrophyAdapterDelegate extends RxAdapterDelegate<List<TrophyEnum>> {

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  protected PublishSubject<View> click = PublishSubject.create();

  public TrophyAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<TrophyEnum> items, int position) {
    return items.get(position) != null;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final TrophyViewHolder vh =
        new TrophyViewHolder(layoutInflater.inflate(R.layout.item_trophy, parent, false));
    vh.itemView.setOnClickListener(v -> click.onNext(vh.itemView));

    vh.imgIcon.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            vh.imgIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            vh.cardView.setRadius(((float) vh.imgIcon.getMeasuredWidth() / (float) 4) /
                (float) 1.61803398874989484820);
          }
        });
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<TrophyEnum> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    TrophyViewHolder vh = (TrophyViewHolder) holder;
    TrophyEnum trophyEnum = items.get(position);

    if (trophyEnum.isUnlockedByUser()) {
      Glide.with(context).load(trophyEnum.getIcon()).into(vh.imgIcon);
    } else {
      Glide.with(context).load(trophyEnum.getIconLocked()).into(vh.imgIcon);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<TrophyEnum> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class TrophyViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgIcon) ImageView imgIcon;
    @BindView(R.id.cardView) CardView cardView;

    public TrophyViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
