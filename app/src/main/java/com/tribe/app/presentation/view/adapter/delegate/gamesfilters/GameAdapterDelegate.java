package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
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
  private int radius;

  protected PublishSubject<View> click = PublishSubject.create();

  public GameAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    radius = screenUtils.dpToPx(6);
  }

  @Override public boolean isForViewType(@NonNull List<Game> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final GameViewHolder vh =
        new GameViewHolder(layoutInflater.inflate(R.layout.item_game, parent, false));

    float[] radiusMatrix = new float[] { 0, 0, 0, 0, radius, radius, radius, radius };
    GradientDrawable sd = new GradientDrawable();
    sd.setColor(ContextCompat.getColor(context, R.color.white_opacity_15));
    sd.setCornerRadii(radiusMatrix);
    ViewCompat.setBackground(vh.viewBackgroundBottom, sd);

    vh.cardView.setOnClickListener(v -> click.onNext(vh.itemView));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Game> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GameViewHolder vh = (GameViewHolder) holder;
    Game game = items.get(position);

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });
    gd.setCornerRadius(radius);

    ViewCompat.setBackground(vh.viewBackground, gd);

    vh.txtBaseline.setText(game.getBaseline());
    vh.txtTitle.setText(game.getTitle());

    vh.imgIcon.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            vh.imgIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            Glide.with(context)
                .load(game.getIcon())
                .thumbnail(0.25f)
                .bitmapTransform(new CropCircleTransformation(context),
                    new RoundedCornersTransformation(context, vh.imgIcon.getMeasuredWidth() >> 1,
                        screenUtils.dpToPx(4), "#FFFFFF", screenUtils.dpToPx(4)))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vh.imgIcon);
          }
        });

    Glide.with(context)
        .load(game.getBanner())
        .thumbnail(0.25f)
        .crossFade()
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(vh.imgBanner);

    if (game.isFeatured()) {
      vh.txtInfo.setText(R.string.new_game_featured);
    } else if (game.isNew()) {
      vh.txtInfo.setText(R.string.new_game_new);
    } else {
      vh.txtInfo.setText("");
    }

    vh.txtPlayCount.setText(context.getString(R.string.new_game_plays, "" + game.getPlays_count()));
  }

  @Override
  public void onBindViewHolder(@NonNull List<Game> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  static class GameViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewBackground) View viewBackground;
    @BindView(R.id.viewBackgroundBottom) View viewBackgroundBottom;
    @BindView(R.id.imgIcon) ImageView imgIcon;
    @BindView(R.id.imgBanner) ImageView imgBanner;
    @BindView(R.id.txtTitle) TextViewFont txtTitle;
    @BindView(R.id.txtBaseline) TextViewFont txtBaseline;
    @BindView(R.id.txtInfo) TextViewFont txtInfo;
    @BindView(R.id.txtPlayCount) TextViewFont txtPlayCount;
    @BindView(R.id.cardView) CardView cardView;

    public GameViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
