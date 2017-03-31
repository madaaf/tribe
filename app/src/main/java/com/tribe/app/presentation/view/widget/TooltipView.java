package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.OvershootInterpolator;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Subscription;

/**
 * Created by tiago on 03/30/2016.
 */
public class TooltipView extends TextViewFont {

  private static final int DURATION = 300;

  @IntDef({ BOTTOM, TOP }) public @interface TooltipType {
  }

  public static final int BOTTOM = 0;
  public static final int TOP = 1;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private @TooltipType int type;

  // OBSERVABLES
  Subscription subscription;

  public TooltipView(Context context) {
    this(context, null);
    init(context, null);
  }

  public TooltipView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TooltipView);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    TextViewCompat.setTextAppearance(this, R.style.Title_2_White);
    setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    setBackgroundResource(a.getResourceId(R.styleable.TooltipView_tooltipBackground,
        R.drawable.bg_tooltip_new_contacts));
    setTextColor(a.getColor(R.styleable.TooltipView_tooltipTextColor, Color.WHITE));
    setType(a.getInt(R.styleable.TooltipView_tooltipType, BOTTOM));
    setGravity(Gravity.CENTER);

    int paddingLarge =
        getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin_large);
    int padding = getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
    setPadding(padding, paddingLarge, padding, padding);
  }

  public void setType(int type) {
    this.type = type;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    float previousTranslationY = getTranslationY();

    int translationY = (int) (previousTranslationY + screenUtils.dpToPx(50));

    setAlpha(0);
    setTranslationY(translationY);
    animate().setInterpolator(new OvershootInterpolator(1.1f))
        .setDuration(DURATION)
        .alpha(1)
        .translationY(previousTranslationY)
        .start();
  }
}
