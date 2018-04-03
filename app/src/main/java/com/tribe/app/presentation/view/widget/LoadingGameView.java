package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

/**
 * Created by madaaflak on 02/04/2018.
 */

public class LoadingGameView extends FrameLayout {

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  public LoadingGameView(@NonNull Context context) {
    super(context);
  }

  public LoadingGameView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    initView(context);
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_dice, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }
}
