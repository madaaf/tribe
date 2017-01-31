package com.tribe.app.presentation.view.adapter.delegate.filterview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.tribe.app.R;
import com.tribe.app.domain.entity.FilterEntity;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class LetterAdapterDelegate extends RxAdapterDelegate<List<FilterEntity>> {

  // VARIABLES
  protected LayoutInflater layoutInflater;
  private Context context;
  private ScreenUtils screenUtils;

  // RESOURCES

  // OBSERVABLES
  private PublishSubject<TextViewFont> clickLetter = PublishSubject.create();

  public LetterAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
  }

  @Override public boolean isForViewType(@NonNull List<FilterEntity> items, int position) {
    return items.get(position).getType().equals(FilterEntity.LETTER);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    LetterViewHolder vh =
        new LetterViewHolder(layoutInflater.inflate(R.layout.item_pts_letter, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<FilterEntity> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    FilterEntity filterEntity = items.get(position);
    LetterViewHolder letterViewHolder = (LetterViewHolder) holder;
    letterViewHolder.txtLetter.setText(filterEntity.getLetter());

    if (filterEntity.isActivated()) {
      letterViewHolder.txtLetter.setAlpha(1);
      letterViewHolder.txtLetter.setBackgroundResource(R.drawable.bg_filter_transition);
    } else {
      letterViewHolder.txtLetter.setAlpha(filterEntity.isActivated() ? 1 : 0.40f);
      letterViewHolder.txtLetter.setBackground(null);
    }

    letterViewHolder.txtLetter.setTag(R.id.tag_position, position);

    if (filterEntity.isActivated()) {
      letterViewHolder.txtLetter.setOnClickListener(v -> {
        TransitionDrawable drawable =
            (TransitionDrawable) letterViewHolder.txtLetter.getBackground();
        drawable.startTransition(200);

        com.tribe.app.presentation.view.utils.AnimationUtils.animateTextColor(
            letterViewHolder.txtLetter, Color.BLACK, Color.WHITE, 200);

        Animation scaleLetterAnimation =
            AnimationUtils.loadAnimation(context, R.anim.scale_letters);
        scaleLetterAnimation.setAnimationListener(new AnimationListenerAdapter() {
          @Override public void onAnimationEnd(Animation animation) {
            clickLetter.onNext(letterViewHolder.txtLetter);
          }
        });

        letterViewHolder.txtLetter.startAnimation(scaleLetterAnimation);
      });
    }
  }

  @Override public void onBindViewHolder(@NonNull List<FilterEntity> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  public Observable<TextViewFont> onClickLetter() {
    return clickLetter;
  }

  static class LetterViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtLetter) public TextViewFont txtLetter;

    public LetterViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
