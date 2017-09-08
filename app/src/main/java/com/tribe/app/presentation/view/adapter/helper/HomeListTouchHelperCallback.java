package com.tribe.app.presentation.view.adapter.helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import rx.Observable;
import rx.subjects.PublishSubject;

public class HomeListTouchHelperCallback extends ItemTouchHelper.Callback {

  public static final float ALPHA_FULL = 1.0f;

  private final ItemTouchHelperAdapter adapter;

  // OBSERVABLES
  private PublishSubject<Pair<Integer, Float>> onDxChange = PublishSubject.create();
  private PublishSubject<Integer> onSwipedItem = PublishSubject.create();

  public HomeListTouchHelperCallback(ItemTouchHelperAdapter adapter) {
    this.adapter = adapter;
  }

  @Override public boolean isLongPressDragEnabled() {
    return false;
  }

  @Override public boolean isItemViewSwipeEnabled() {
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
      RecyclerView.ViewHolder target) {
    return false;
  }

  @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
    // Notify the adapter of the dismissal
    adapter.onItemDismiss(viewHolder.getAdapterPosition());
    onSwipedItem.onNext(viewHolder.getAdapterPosition());
    onDxChange.onNext(Pair.create(viewHolder.getAdapterPosition(), 0f));
  }

  @Override
  public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
      float dX, float dY, int actionState, boolean isCurrentlyActive) {
    onDxChange.onNext(Pair.create(viewHolder.getAdapterPosition(), dX));

    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
      // Fade out the view as it is swiped out of the parent's bounds
      final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
      viewHolder.itemView.setAlpha(alpha);
      viewHolder.itemView.setTranslationX(dX);
    } else {
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
  }

  @Override public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    // We only want the active item to change
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      if (viewHolder instanceof ItemTouchHelperViewHolder) {
        // Let the view holder know that this item is being moved or dragged
        ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
        itemViewHolder.onItemSelected();
      }
    }

    super.onSelectedChanged(viewHolder, actionState);
  }

  @Override public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    super.clearView(recyclerView, viewHolder);

    viewHolder.itemView.setAlpha(ALPHA_FULL);

    if (viewHolder instanceof ItemTouchHelperViewHolder) {
      // Tell the view holder it's time to restore the idle state
      ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
      itemViewHolder.onItemClear();
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Pair<Integer, Float>> onDxChange() {
    return onDxChange;
  }

  public Observable<Integer> onSwipedItem() {
    return onSwipedItem;
  }
}
