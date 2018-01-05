package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaQuestionsView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // VARIABLES
  private Unbinder unbinder;
  private String title;
  private Drawable icon;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameTriviaQuestionsView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaQuestionsView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameTriviaCategoryView);
    icon = a.getDrawable(R.styleable.GameTriviaCategoryView_categoryIcon);
    title = a.getString(R.styleable.GameTriviaCategoryView_categoryTitle);
    a.recycle();

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_trivia_category, this);
    unbinder = ButterKnife.bind(this);

    setTitle(title);
    setIcon(icon);
  }

  private void initSubscriptions() {

  }

  private void setIcon(Drawable drawable) {
    imgIcon.setImageDrawable(drawable);
  }

  private void setTitle(String title) {
    txtTitle.setText(title);
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */

}
