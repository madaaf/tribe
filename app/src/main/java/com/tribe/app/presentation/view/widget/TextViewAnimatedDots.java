package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.TextViewUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 11/08/2016.
 */
public class TextViewAnimatedDots extends LinearLayout {

  private static final int TIME = 300;

  @Inject ScreenUtils screenUtils;

  @Nullable @BindView(R.id.txtLabel) TextViewFont txtLabel;

  @Nullable @BindView(R.id.txtDots) TextViewFont txtDots;

  // VARIABLES
  Subscription subscription;

  public TextViewAnimatedDots(Context context) {
    this(context, null);
    init(context, null);
  }

  public TextViewAnimatedDots(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_text_animated_dots, this, true);
    ButterKnife.bind(this);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewAnimatedDots);

    if (a.hasValue(R.styleable.TextViewAnimatedDots_textStyle)) {
      TextViewUtils.setTextAppearence(getContext(), txtLabel,
          a.getResourceId(R.styleable.TextViewAnimatedDots_textStyle, 0));
      TextViewUtils.setTextAppearence(getContext(), txtDots,
          a.getResourceId(R.styleable.TextViewAnimatedDots_textStyle, 0));
    }

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  public void setText(String str) {
    txtLabel.setText(str);
  }

  public void setText(int str) {
    txtLabel.setText(str);
  }

  public void startDotsAnimation() {
    txtDots.setText("");
    txtDots.setVisibility(View.VISIBLE);
    subscription = Observable.interval(TIME, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (aLong % 4 == 0) {
            txtDots.setText("");
          } else if (aLong % 4 == 1) {
            txtDots.setText(".");
          } else if (aLong % 4 == 2) {
            txtDots.setText("..");
          } else if (aLong % 4 == 3) {
            txtDots.setText("...");
          }
        });
  }

  public void stopDotsAnimation() {
    if (subscription != null) {
      subscription.unsubscribe();
    }
    txtDots.setVisibility(View.GONE);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }
}
