package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;
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
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.view.adapter.LiveInviteAdapter;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.InviteListDividerDecoration;
import com.tribe.app.presentation.view.adapter.diff.LiveInviteDiffCallback;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import com.tribe.app.presentation.view.adapter.model.Header;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;
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
import rx.subjects.BehaviorSubject;
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
  private static final int DURATION_FAST = 200;
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
  private Shortcut selected = null;
  private Live live;
  private Scheduler singleThreadExecutor;
  private int positionOfFirstShortcut;
  private @LiveContainer.Event int drawerState = LiveContainer.CLOSED;
  private boolean dragging = false;

  // RESOURCES
  private int translationX;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private BehaviorSubject<List<Shortcut>> onShortcutUpdate = BehaviorSubject.create();
  private PublishSubject<Integer> onInviteViewWidthChanged = PublishSubject.create();
  private PublishSubject<Void> onClickBottom = PublishSubject.create();
  private PublishSubject<Boolean> onDisplayDropZone = PublishSubject.create();
  private PublishSubject<Integer> onScrollStateChanged = PublishSubject.create();
  private PublishSubject<Integer> onScroll = PublishSubject.create();
  private PublishSubject<View> onClickEdit = PublishSubject.create();
  private PublishSubject<Void> onInviteSms = PublishSubject.create();
  private PublishSubject<Void> onInviteMessenger = PublishSubject.create();
  private PublishSubject<Void> onLaunchSearch = PublishSubject.create();
  private PublishSubject<Void> onLaunchDice = PublishSubject.create();

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
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white_off));
  }

  private void initSubscriptions() {
    adapter.initInviteViewWidthChange(onInviteViewWidthChanged);
  }

  private void initResources() {
    translationX = screenUtils.dpToPx(WIDTH_PARTIAL);
  }

  private void initRecyclerView() {
    layoutManager = new LiveInviteLayoutManager(getContext());
    recyclerViewInvite.setLayoutManager(layoutManager);
    recyclerViewInvite.setHasFixedSize(true);
    recyclerViewInvite.setItemAnimator(null);
    recyclerViewInvite.addItemDecoration(
        new DividerFirstLastItemDecoration(screenUtils.dpToPx(10), 0, 0));
    recyclerViewInvite.addItemDecoration(new InviteListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.grey_divider), screenUtils.dpToPx(0.5f),
        getSectionCallback(itemsList)));
    adapter.setItems(itemsList);
    recyclerViewInvite.setAdapter(adapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(3, 50);

    recyclerViewInvite.addOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        onScrollStateChanged.onNext(newState);
      }

      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy == 0) return;

        onScroll.onNext(dy);

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

    subscriptions.add(adapter.onClick()
        .map(view -> adapter.getItemAtPosition(recyclerViewInvite.getChildLayoutPosition(view)))
        .subscribe(item -> {
          Shortcut shortcut = (Shortcut) item;
          if (shortcut.getId().equals(Shortcut.ID_EMPTY)) {
            subscriptions.add(DialogFactory.dialogMultipleChoices(getContext(),
                EmojiParser.demojizedText(getContext().getString(R.string.empty_call_popup_title)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.empty_call_popup_message)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.empty_call_popup_share_sms_android)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.empty_call_popup_share_messenger)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.empty_call_popup_throw_the_dice)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.empty_call_popup_search_friend)),
                EmojiParser.demojizedText(getContext().getString(R.string.empty_call_popup_cancel)))
                .subscribe(integer -> {
                  switch (integer) {
                    case 0:
                      onInviteSms.onNext(null);
                      break;
                    case 1:
                      onInviteMessenger.onNext(null);
                      break;
                    case 2:
                      onLaunchDice.onNext(null);
                      break;
                    case 3:
                      onLaunchSearch.onNext(null);
                      break;
                  }
                }));
          } else {
            User user = shortcut.getSingleFriend();

            if (selected != null && !shortcut.getId().equals(selected.getId())) {
              int position = itemsList.indexOf(selected);
              selected.getSingleFriend().setSelected(false);
              adapter.notifyItemChanged(position);
            }

            if (user.isSelected()) {
              selected = shortcut;
            } else {
              selected = null;
            }

            onDisplayDropZone.onNext(user.isSelected());
          }
        }));

    subscriptions.add(adapter.onClickEdit().subscribe(onClickEdit));
  }

  private SectionCallback getSectionCallback(final List<LiveInviteAdapterSectionInterface> list) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        if (position < 0 || position > list.size() - 1) return false;
        return list.get(position) instanceof Header &&
            (list.get(position).getId().equals(Header.HEADER_NAME));
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
    subscriptions.add(Observable.combineLatest(this.live.onRoomUpdated()
        .startWith(new Room())
        .defaultIfEmpty(new Room())
        .onBackpressureBuffer(), onShortcutUpdate.onBackpressureBuffer(), (room, listShortcut) -> {
      Set<String> alreadyPresent = new HashSet<>();
      List<LiveInviteAdapterSectionInterface> temp = new ArrayList<>();
      List<String> usersAtBeginningOfCall = null;

      if (live.fromRoom()) {
        usersAtBeginningOfCall = new ArrayList<>();
      } else {
        usersAtBeginningOfCall = live.getUserIdsOfShortcut();
      }

      if (room.getShortcut() != null && !room.getShortcut().isSingle()) {
        temp.add(new Header(Header.HEADER_NAME,
            live.getShortcut() != null ? live.getShortcut().getName() : "",
            R.drawable.picto_live_invite_header_edit, Gravity.START | Gravity.CENTER_VERTICAL));
      }

      temp.add(new Header(Header.HEADER_CHAT_MEMBERS,
          getResources().getString(R.string.live_invite_section_chat_members), 0,
          Gravity.START | Gravity.CENTER_VERTICAL));

      if (room.getLiveUsers() != null) {
        for (User user : room.getLiveUsers()) {
          if (true) {
            //if (!user.equals(currentUser)) {
            user.setCurrentRoomId(room.getId());
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
          getResources().getString(R.string.live_members_invite_friends_section_title), 0,
          Gravity.CENTER));
      for (Shortcut shortcut : listShortcut) {
        User user = shortcut.getSingleFriend();
        user.setSelected(selected != null && selected.getId().equals(shortcut.getId()));
        if (!alreadyPresent.contains(user.getId()) &&
            !usersAtBeginningOfCall.contains(user.getId())) {
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
    }).onBackpressureDrop().observeOn(AndroidSchedulers.mainThread()).subscribe(diffResult -> {
      //if (diffResult != null) {
      //  diffResult.dispatchUpdatesTo(adapter);
      //} else {
      adapter.setItems(itemsList);
      adapter.notifyDataSetChanged();

      if (drawerState == LiveContainer.CLOSED &&
          layoutManager.findFirstCompletelyVisibleItemPosition() != positionOfFirstShortcut) {
        recyclerViewInvite.post(
            () -> layoutManager.scrollToPositionWithOffset(positionOfFirstShortcut, 0));
      }
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

  public void initOnInviteDropped(Observable<TileInviteView> obs) {
    subscriptions.add(obs.subscribe(tile -> {
      if (live.getRoom() == null) {
        Toast.makeText(getContext(),
            EmojiParser.demojizedText(getContext().getString(R.string.error_unknown)),
            Toast.LENGTH_SHORT).show();
      } else {
        liveInvitePresenter.createInvite(live.getRoom().getId(), tile.getUser().getId());
        adapter.removeItem(tile.getPosition());
        viewInviteBottom.showAdded();
      }
    }));
  }

  public TileInviteView findViewByCoords(float rawX, float rawY) {
    // Find the child view that was touched (perform a hit Test)
    int[] recyclerViewCoords = new int[2];
    recyclerViewInvite.getLocationOnScreen(recyclerViewCoords);

    View view =
        ViewUtils.findViewAt(recyclerViewInvite, TileInviteView.class, (int) rawX, (int) rawY);

    if (view instanceof TileInviteView) {
      TileInviteView tileInviteView = (TileInviteView) view;
      if (tileInviteView.getUser() == null) return null;
      return tileInviteView;
    }

    return null;
  }

  public void setDragging(boolean dragging) {
    if (!dragging && this.dragging) recyclerViewInvite.getRecycledViewPool().clear();

    this.dragging = dragging;
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut, BaseListViewHolder viewHolder) {

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

  public Observable<Boolean> onDisplayDropZone() {
    return onDisplayDropZone;
  }

  public Observable<Integer> onScroll() {
    return onScroll;
  }

  public Observable<Integer> onScrollStateChanged() {
    return onScrollStateChanged;
  }

  public Observable<View> onClickEdit() {
    return onClickEdit;
  }

  public Observable<Void> onInviteSms() {
    return onInviteSms;
  }

  public Observable<Void> onInviteMessenger() {
    return onInviteMessenger;
  }

  public Observable<Void> onStopAndLaunchDice() {
    return onLaunchDice;
  }

  public Observable<Void> onLaunchSearch() {
    return onLaunchSearch;
  }
}

