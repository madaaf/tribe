package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import timber.log.Timber;

/**
 * Created by tiago on 05/10/2017.
 */

public class RecyclerViewInvite extends RecyclerView {

  @IntDef({ UP, DOWN }) public @interface ScrollDirection {
  }

  public static final int UP = 0;
  public static final int DOWN = 1;

  private float y;
  private int positionToBlock = 0;
  private LiveInviteLayoutManager liveInviteLayoutManager;
  private @ScrollDirection int scrollDirection = 0;
  private boolean isDrawerOpen = false;

  public RecyclerViewInvite(Context context) {
    super(context);
  }

  public RecyclerViewInvite(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public RecyclerViewInvite(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setPositionToBlock(int positionToBlock) {
    this.positionToBlock = positionToBlock;
  }

  public int getScrollDirection() {
    return scrollDirection;
  }

  public void setDrawerOpen(boolean drawerOpen) {
    isDrawerOpen = drawerOpen;
  }

  public boolean isDrawerOpen() {
    return isDrawerOpen;
  }

  @Override public boolean onTouchEvent(MotionEvent e) {
    if (isDrawerOpen) return super.onTouchEvent(e);

    int action = e.getAction();
    float y = e.getY();
    int topOfView = getChildAt(0).getTop();
    boolean shouldCare = false;

    if (liveInviteLayoutManager == null) {
      liveInviteLayoutManager = (LiveInviteLayoutManager) getLayoutManager();
    }

    if (liveInviteLayoutManager.findFirstVisibleItemPosition() == positionToBlock &&
        topOfView == 0) {
      Timber.d("Should care true");
      shouldCare = true;
    }

    if (action == MotionEvent.ACTION_DOWN) {
      this.y = y;
    } else if (action == MotionEvent.ACTION_MOVE) {
      if (this.y < y) {
        scrollDirection = UP;

        if (shouldCare) {
          Timber.d("Scroll enabled false");
          liveInviteLayoutManager.setScrollEnabled(false);
          return true;
        }
      } else if (this.y > y) {
        Timber.d("Scroll enabled true");
        scrollDirection = DOWN;
        liveInviteLayoutManager.setScrollEnabled(true);
      }
    }

    return super.onTouchEvent(e);
  }
}
