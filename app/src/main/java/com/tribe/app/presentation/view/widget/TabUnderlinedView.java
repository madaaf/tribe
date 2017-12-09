package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import rx.Subscription;

/**
 * Created by tiago on 03/30/2016.
 */
public class TabUnderlinedView extends FrameLayout {

  private static final int DURATION = 150;

  @BindView(R.id.txtLabelActive) TextViewFont txtLabelActive;
  @BindView(R.id.txtLabelInactive) TextViewFont txtLabelInactive;

  // VARIABLES
  private boolean active;

  // OBSERVABLES
  Subscription subscription;

  public TabUnderlinedView(Context context) {
    this(context, null);
    init(context, null);
  }

  public TabUnderlinedView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater.from(getContext()).inflate(R.layout.view_tab_underline, this);
    ButterKnife.bind(this);

    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TabUnderlinedView);

    active = a.getBoolean(R.styleable.TabUnderlinedView_tabActive, false);
    String text = a.getString(R.styleable.TabUnderlinedView_tabText);
    txtLabelActive.setText(text);
    txtLabelInactive.setText(text);
    setActive(active);
  }

  /**
   * PUBLIC
   */

  public void setActive(boolean active) {
    this.active = active;

    if (active) {
      AnimationUtils.fadeIn(txtLabelActive, DURATION);
      AnimationUtils.fadeOut(txtLabelInactive, DURATION);
    } else {
      AnimationUtils.fadeIn(txtLabelInactive, DURATION);
      AnimationUtils.fadeOut(txtLabelActive, DURATION);
    }
  }

  public boolean isActive() {
    return active;
  }
}
