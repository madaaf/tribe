package com.tribe.app.presentation.view.adapter.helper;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import timber.log.Timber;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

  // The minimum amount of items to have below your current scroll position
  // before loading more.
  private int visibleThreshold = 5;
  // The current offset index of data you have loaded
  private int currentPage = 0;
  // The total number of items in the dataset after the last load
  private int previousTotalItemCountBottom = 0, previousTotalItemCountTop = 0;
  // True if we are still waiting for the last set of data to load.
  private boolean loadingBottom = true, loadingTop = true;
  // Sets the starting page index
  private int startingPageIndex = 0;

  RecyclerView.LayoutManager layoutManager;

  public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
  }

  public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
  }

  public int getLastVisibleItem(int[] lastVisibleItemPositions) {
    int maxSize = 0;
    for (int i = 0; i < lastVisibleItemPositions.length; i++) {
      if (i == 0) {
        maxSize = lastVisibleItemPositions[i];
      } else if (lastVisibleItemPositions[i] > maxSize) {
        maxSize = lastVisibleItemPositions[i];
      }
    }
    return maxSize;
  }

  // This happens many times a second during a scroll, so be wary of the code you place here.
  // We are given a few useful parameters to help us work out if we need to load some more data,
  // but first we check if we are waiting for the previous load to finish.
  @Override public void onScrolled(RecyclerView view, int dx, int dy) {
    int lastVisibleItemPosition = 0;
    int firstVisibleItemPosition = 0;
    boolean downwards = dy > 0;
    int totalItemCount = layoutManager.getItemCount();

    if (layoutManager instanceof StaggeredGridLayoutManager) {
      int[] lastVisibleItemPositions =
          ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
      lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
    } else if (layoutManager instanceof GridLayoutManager) {
      lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
    } else if (layoutManager instanceof LinearLayoutManager) {
      firstVisibleItemPosition =
          ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
      lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
    }

    // If the total item count is zero and the previous isn't, assume the
    // list is invalidated and should be reset back to initial state
    if (totalItemCount < previousTotalItemCountBottom) {
      this.currentPage = this.startingPageIndex;
      this.previousTotalItemCountBottom = totalItemCount;
      if (totalItemCount == 0) {
        this.loadingBottom = true;
        //Timber.d("Bad state loadingBottom");
      }
    }

    if (totalItemCount < previousTotalItemCountTop) {
      this.currentPage = this.startingPageIndex;
      this.previousTotalItemCountTop = totalItemCount;
      if (totalItemCount == 0) {
        this.loadingTop = true;
        //Timber.d("Bad state loadingTop");
      }
    }

    // If it’s still loading, we check to see if the dataset count has
    // changed, if so we conclude it has finished loading and update the current page
    // number and total item count.
    if (loadingBottom && (totalItemCount > previousTotalItemCountBottom)) {
      //Timber.d("loadingBottom false");
      loadingBottom = false;
      previousTotalItemCountBottom = totalItemCount;
    }

    if (loadingTop && (totalItemCount > previousTotalItemCountTop)) {
      //Timber.d("loadingTop false");
      loadingTop = false;
      previousTotalItemCountTop = totalItemCount;
    }

    // If it isn’t currently loading, we check to see if we have breached
    // the visibleThreshold and need to reload more data.
    // If we do need to reload some more data, we execute onLoadMore to fetch the data.
    // threshold should reflect how many total columns there are too
    if (!loadingBottom &&
        downwards &&
        (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
      //Timber.d("Loadingbottom loadmore");
      onLoadMore(currentPage, totalItemCount, view, downwards);
      loadingBottom = true;
    }

    if (!loadingTop && !downwards && (firstVisibleItemPosition - visibleThreshold) <= 0) {
      //Timber.d("Loadingtop loadmore");
      onLoadMore(currentPage, totalItemCount, view, downwards);
      loadingTop = true;
    }
  }

  // Call this method whenever performing new searches
  public void resetState() {
    this.currentPage = this.startingPageIndex;
    this.previousTotalItemCountBottom = 0;
    this.previousTotalItemCountTop = 0;
    this.loadingTop = true;
    this.loadingBottom = true;
  }

  // Defines the process for actually loading more data based on page
  public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view,
      boolean downwards);
}