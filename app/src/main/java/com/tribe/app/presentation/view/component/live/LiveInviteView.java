package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.mvp.presenter.LiveInvitePresenter;
import com.tribe.app.presentation.mvp.view.LiveInviteMVPView;
import com.tribe.app.presentation.mvp.view.RoomMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.view.adapter.LiveInviteAdapter;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.InviteListDividerDecoration;
import com.tribe.app.presentation.view.adapter.diff.LiveInviteDiffCallback;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import com.tribe.app.presentation.view.adapter.model.Header;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.RecyclerViewInvite;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteView extends FrameLayout
    implements LiveInviteMVPView, RoomMVPView, ShortcutMVPView {

  public static final int WIDTH_PARTIAL = 100;
  public static final int WIDTH_FULL = 220;

  private static final int RECYCLER_VIEW_ANIMATIONS_DURATION = 200;
  private static final int RECYCLER_VIEW_ANIMATIONS_DURATION_LONG = 300;
  private static final int DURATION = 500;
  private static final int DURATION_FAST = 100;
  private static final float OVERSHOOT = 0.75f;

  @Inject TagManager tagManager;

  @Inject ScreenUtils screenUtils;

  @Inject LiveInviteAdapter adapter;

  @Inject LiveInvitePresenter liveInvitePresenter;

  @Inject User currentUser;

  @BindView(R.id.recyclerViewInvite) RecyclerViewInvite recyclerViewInvite;

  @BindView(R.id.btnMore) LiveInviteBottomView viewInviteBottom;

  // VARIABLES
  private Unbinder unbinder;
  private LiveInviteLayoutManager layoutManager;
  private List<LiveInviteAdapterSectionInterface> itemsList;
  private Live live;
  private Scheduler singleThreadExecutor;
  private int positionOfFirstShortcut;
  private @LiveContainer.Event int drawerState = LiveContainer.CLOSED;

  // RESOURCES
  private int translationX;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<Shortcut>> onShortcutUpdate = PublishSubject.create();
  private PublishSubject<Integer> onInviteViewWidthChanged = PublishSubject.create();
  private PublishSubject<Void> onClickBottom = PublishSubject.create();

  public LiveInviteView(Context context) {
    super(context);
    init();
  }

  public LiveInviteView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    liveInvitePresenter.onViewAttached(this);
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
    initSubscriptions();
    initRecyclerView();

    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());
    liveInvitePresenter.loadShortcuts();

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {
    itemsList = new ArrayList<>();
  }

  private void initUI() {
    recyclerViewInvite.setTranslationX(translationX);
    setBackgroundColor(Color.WHITE);
  }

  private void initSubscriptions() {
    adapter.initInviteViewWidthChange(onInviteViewWidthChanged);
  }

  private void initResources() {
    translationX = screenUtils.dpToPx(15);
  }

  private void initRecyclerView() {
    layoutManager = new LiveInviteLayoutManager(getContext());
    recyclerViewInvite.setLayoutManager(layoutManager);
    recyclerViewInvite.setHasFixedSize(true);
    recyclerViewInvite.setItemAnimator(null);
    recyclerViewInvite.addItemDecoration(new InviteListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.grey_divider), screenUtils.dpToPx(0.5f),
        getSectionCallback(itemsList)));
    adapter.setItems(new ArrayList<>());
    recyclerViewInvite.setAdapter(adapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(3, 50);

    recyclerViewInvite.addOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (recyclerViewInvite.isDrawerOpen()) return;

        int currentFirstVisible = layoutManager.findFirstVisibleItemPosition();

        if (currentFirstVisible < positionOfFirstShortcut &&
            recyclerViewInvite.getScrollDirection() == RecyclerViewInvite.UP) {
          recyclerViewInvite.stopScroll();
          recyclerViewInvite.post(
              () -> layoutManager.scrollToPositionWithOffset(positionOfFirstShortcut, 0));
        }
      }
    });

    //subscriptions.add(adapter.onInvite()
    //    .map(view -> adapter.getItemAtPosition(recyclerViewInvite.getChildLayoutPosition(view)))
    //    .subscribe(item -> {
    //      User user = (User) item;
    //      liveInvitePresenter.createInvite(live.getRoomId(), user.getId());
    //    }));
  }

  private SectionCallback getSectionCallback(final List<LiveInviteAdapterSectionInterface> list) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        return list.get(position) instanceof Header &&
            (list.get(position).getId().equals(Header.HEADER_DRAG_IN) ||
                list.get(position).getId().equals(Header.HEADER_NAME));
      }

      @Override public int getSectionType(int position) {
        if (position == -1) return BaseSectionItemDecoration.LIVE_CHAT_MEMBERS;
        return list.get(position).getLiveInviteSectionType();
      }
    };
  }

  private void computeUser(List<LiveInviteAdapterSectionInterface> temp, User user,
      Set<String> alreadyPresent) {
    if (!alreadyPresent.contains(user.getId())) {
      temp.add(user);
      alreadyPresent.add(user.getId());
    }
  }

  ////////////
  // PUBLIC //
  ////////////

  public void updateWidth(int width) {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
    params.width = width;
    requestLayout();
    onInviteViewWidthChanged.onNext(Math.min(width, screenUtils.dpToPx(WIDTH_PARTIAL)));
  }

  public void setLive(Live live) {
    this.live = live;

    subscriptions.add(Observable.combineLatest(live.onRoomUpdated().onBackpressureBuffer(),
        onShortcutUpdate.onBackpressureBuffer(), (room, listShortcut) -> {
          Set<String> alreadyPresent = new HashSet<>();
          List<LiveInviteAdapterSectionInterface> temp = new ArrayList<>();

          // TODO find a better way to get the shortcut associated with the room
          temp.add(new Header(Header.HEADER_NAME,
              live.getShortcut() != null ? live.getShortcut().getName() : "",
              R.drawable.picto_live_invite_header_edit));

          temp.add(new Header(Header.HEADER_CHAT_MEMBERS,
              getResources().getString(R.string.live_invite_section_chat_members), 0));

          if (room.getLiveUsers() != null) {
            for (User user : room.getLiveUsers()) {
              if (!user.equals(currentUser)) {
                user.setCurrentRoomId(room.getId());
                user.setWaiting(room.isUserWaiting(user.getId()));
                computeUser(temp, user, alreadyPresent);
              }
            }
          }

          if (room.getInvitedUsers() != null) {
            for (User user : room.getInvitedUsers()) {
              user.setRinging(true);
              computeUser(temp, user, alreadyPresent);
            }
          }

          temp.add(room);

          positionOfFirstShortcut = temp.size();
          recyclerViewInvite.setPositionToBlock(positionOfFirstShortcut);

          temp.add(new Header(Header.HEADER_DRAG_IN,
              getResources().getString(R.string.live_members_invite_friends_section_title), 0));
          temp.add(new Header(Header.HEADER_RECENT,
              getResources().getString(R.string.home_section_recent),
              R.drawable.picto_live_invite_header_recent));

          for (Shortcut shortcut : listShortcut) {
            User user = shortcut.getSingleFriend();
            if (!alreadyPresent.contains(user.getId())) {
              temp.add(shortcut);
            }
          }

          return temp;
        }).subscribeOn(singleThreadExecutor).map(newListItems -> {
      DiffUtil.DiffResult diffResult = null;
      List<LiveInviteAdapterSectionInterface> temp = new ArrayList<>(newListItems);

      ListUtils.addEmptyItemsInvite(temp);

      if (itemsList.size() != 0) {
        diffResult = DiffUtil.calculateDiff(new LiveInviteDiffCallback(itemsList, temp));
        adapter.setItems(temp);
      }

      itemsList.clear();
      itemsList.addAll(temp);
      return diffResult;
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(diffResult -> {
      //if (diffResult != null) {
      //  diffResult.dispatchUpdatesTo(adapter);
      //} else {
      adapter.setItems(itemsList);
      adapter.notifyDataSetChanged();

      recyclerViewInvite.post(
          () -> layoutManager.scrollToPositionWithOffset(positionOfFirstShortcut, 0));
      //}
    }));
  }

  public void initDrawerEventChangeObservable(Observable<Integer> onEventChange) {
    subscriptions.add(onEventChange.subscribe(event -> {
      if (drawerState == LiveContainer.CLOSED && event != LiveContainer.CLOSED) {
        recyclerViewInvite.animate()
            .translationX(0)
            .setDuration(DURATION_FAST)
            .setInterpolator(new DecelerateInterpolator())
            .start();
      }

      if (event == LiveContainer.OPEN_FULL) {
        recyclerViewInvite.setDrawerOpen(true);
        recyclerViewInvite.post(() -> recyclerViewInvite.smoothScrollToPosition(0));
        viewInviteBottom.showLess();
        adapter.setFullMode();
        adapter.notifyDataSetChanged();
      } else if (event == LiveContainer.OPEN_PARTIAL && drawerState != LiveContainer.CLOSED) {
        recyclerViewInvite.setDrawerOpen(false);
        recyclerViewInvite.smoothScrollToPosition(positionOfFirstShortcut);
        viewInviteBottom.showMore();
        adapter.setPartialMode();
        adapter.notifyDataSetChanged();
      } else if (event == LiveContainer.CLOSED) {
        recyclerViewInvite.animate()
            .translationX(translationX)
            .setDuration(DURATION_FAST)
            .setInterpolator(new DecelerateInterpolator())
            .start();
      }

      drawerState = event;
    }));
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {
    onShortcutUpdate.onNext(singleShortcutList);
  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  @Override public Context context() {
    return null;
  }

  @Override public void onRoomInfosError(String str) {

  }

  @Override public void onRoomInfos(Room room) {

  }

  @Override public void onRoomUpdate(Room room) {

  }

  @Override public void randomRoomAssignedSubscriber(String roomId) {

  }

  //////////////////////
  //     ON CLICK     //
  //////////////////////

  @OnClick(R.id.btnMore) void onClickMore() {
    onClickBottom.onNext(null);
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onShareLink() {
    return adapter.onShareLink();
  }

  public Observable<Void> onClickBottom() {
    return onClickBottom;
  }
}

