package com.tribe.app.presentation.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class Test extends FrameLayout {

  private Unbinder unbinder;
  private Context context;
  protected LayoutInflater inflater;
  private static View v;
  private NotificationViewPagerAdapter adapter;
  private GestureDetectorCompat gestureScanner;

  @BindView(R.id.pager) ViewPager pager;
  @BindView(R.id.txtDismiss) TextViewFont textDismiss;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.bgView) View bgView;
  @BindView(R.id.dotsContainer) LinearLayout dotsContainer;

  public Test(@NonNull Context context) {
    super(context);
    this.context = context;
    initView();
  }

  private void initView() {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    v = inflater.inflate(R.layout.activity_test, this, true);
    unbinder = ButterKnife.bind(this);
    textDismiss.setOnTouchListener((v, event) -> gestureScanner.onTouchEvent(event));
    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());
  }

  public void show(Activity activity, List<NotificationModel> list) {
    final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
    adapter = new NotificationViewPagerAdapter(context, list);
    initDots(list.size());
    PageListener pageListener = new PageListener();
    pager.addOnPageChangeListener(pageListener);
    pager.setAdapter(adapter);

    decorView.addView(v);
  }

  protected void hideView() {
    container.setOnTouchListener((v, event) -> true);
    Animation slideOutAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.notif_container_exit_animation);
    setAnimation(slideOutAnimation);
    slideOutAnimation.setFillAfter(false);
    slideOutAnimation.setDuration(700);
    slideOutAnimation.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation);
      }

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        setVisibility(GONE);
      }
    });

    textDismiss.setVisibility(INVISIBLE);

    bgView.animate()
        .setDuration(300)
        .alpha(0f)
        .withEndAction(() -> startAnimation(slideOutAnimation))
        .start();
  }

  private void initDots(int dotsNbr) {
    int sizeDot = context.getResources().getDimensionPixelSize(R.dimen.view_dot_size_chat);
    for (int i = 0; i < dotsNbr; i++) {
      View v = new View(context);
      v.setBackgroundResource(R.drawable.shape_oval_grey);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.setMargins(0, 0, 10, 0);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      //  viewDots.add(v);
      dotsContainer.addView(v);
    }
  }

  ///////////////////
  //  GESTURE IMP  //
  ///////////////////

  public static class PageListener extends ViewPager.SimpleOnPageChangeListener {
    public void onPageSelected(int position) {
      Timber.i("page selected " + position);
    }
  }

  private class TapGestureListener implements GestureDetector.OnGestureListener {

    @Override public boolean onDown(MotionEvent e) {
      hideView();
      return true;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }
}
