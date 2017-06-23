package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.view.adapter.LiveInviteAdapter;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteView extends FrameLayout {

  public final static int WIDTH = 106;

  @Inject Navigator navigator;

  @Inject TagManager tagManager;

  @Inject ScreenUtils screenUtils;

  @Inject LiveInviteAdapter adapter;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerViewFriends;

  // VARIABLES

  // VARIABLES
  private LiveInviteLayoutManager layoutManager;
  private boolean dragging = false;
  private List<Recipient> friendshipList;

  // RESOURCES

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onScroll = PublishSubject.create();
  private PublishSubject<Integer> onScrollStateChanged = PublishSubject.create();
  protected final PublishSubject<View> onInviteLiveClick = PublishSubject.create();

  public LiveInviteView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveInviteView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_live_invite, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initRecyclerView();

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init(Context context, AttributeSet attrs) {
    friendshipList = new ArrayList<>();
  }

  private void initUI() {
  }

  private void initResources() {

  }

  public void setFadeInEffet(float fadeInEffet) {
    setAlpha(fadeInEffet);
  }

  private void initRecyclerView() {
    layoutManager = new LiveInviteLayoutManager(getContext());
    recyclerViewFriends.setLayoutManager(layoutManager);
    recyclerViewFriends.setItemAnimator(null);
    adapter.setItems(new ArrayList<>());
    recyclerViewFriends.setAdapter(adapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

    recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        onScroll.onNext(dy);
      }

      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        onScrollStateChanged.onNext(newState);
      }
    });

    subscriptions.add(adapter.onInviteLiveClick().subscribe(onInviteLiveClick));
  }

  public TileView findViewByCoords(float rawX, float rawY) {
    // Find the child view that was touched (perform a hit test)
    int[] recyclerViewCoords = new int[2];
    recyclerViewFriends.getLocationOnScreen(recyclerViewCoords);

    View view = ViewUtils.findViewAt(recyclerViewFriends, TileView.class, (int) rawX, (int) rawY);

    if (view instanceof TileView) {
      return (TileView) view;
    }

    return null;
  }

  ////////////
  // PUBLIC //
  ///////////

  public void renderFriendshipList(List<Friendship> friendshipList) {
    this.friendshipList.clear();
    this.friendshipList.addAll(friendshipList);
    if (!dragging) adapter.setItems(this.friendshipList);
  }

  public void removeItemAtPosition(int position) {
    //adapter.removeItem(position);
  }

  public void diceDragued() {
    adapter.diceDragued();
  }

  public void setDragging(boolean dragging) {
    if (!dragging && this.dragging) recyclerViewFriends.getRecycledViewPool().clear();

    this.dragging = dragging;
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Integer> onScroll() {
    return onScroll;
  }

  public Observable<Integer> onScrollStateChanged() {
    return onScrollStateChanged;
  }

  public Observable<View> onInviteLiveClick() {
    return onInviteLiveClick;
  }
}

