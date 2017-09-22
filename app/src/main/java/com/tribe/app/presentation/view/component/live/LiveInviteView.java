package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
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
import com.tribe.app.presentation.view.adapter.decorator.InviteListDividerDecoration;
import com.tribe.app.presentation.view.adapter.decorator.LiveInviteSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.diff.LiveInviteDiffCallback;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.header.LiveInviteViewHeader;
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

  private static final int RECYCLER_VIEW_ANIMATIONS_DURATION = 200;
  private static final int RECYCLER_VIEW_ANIMATIONS_DURATION_LONG = 300;
  private static final int DURATION = 500;
  private static final float OVERSHOOT = 0.75f;

  @Inject TagManager tagManager;

  @Inject ScreenUtils screenUtils;

  @Inject LiveInviteAdapter adapter;

  @Inject LiveInvitePresenter liveInvitePresenter;

  @Inject User currentUser;

  @BindView(R.id.recyclerViewInvite) RecyclerView recyclerViewInvite;

  // VARIABLES
  private Unbinder unbinder;
  private LiveInviteLayoutManager layoutManager;
  private List<LiveInviteAdapterSectionInterface> itemsList;
  private Live live;
  private Scheduler singleThreadExecutor;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<Shortcut>> onShortcutUpdate = PublishSubject.create();

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
    recyclerViewInvite.setAlpha(0);
    recyclerViewInvite.setTranslationY(translationY);
  }

  private void initResources() {
    translationY = -screenUtils.getHeightPx();
  }

  private void initRecyclerView() {
    layoutManager = new LiveInviteLayoutManager(getContext());
    recyclerViewInvite.setLayoutManager(layoutManager);
    recyclerViewInvite.setHasFixedSize(true);
    recyclerViewInvite.setItemAnimator(null);
    //recyclerViewInvite.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1.5f)));
    //recyclerViewInvite.getItemAnimator().setAddDuration(RECYCLER_VIEW_ANIMATIONS_DURATION_LONG);
    //recyclerViewInvite.getItemAnimator().setRemoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
    //recyclerViewInvite.getItemAnimator().setMoveDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
    //recyclerViewInvite.getItemAnimator().setChangeDuration(RECYCLER_VIEW_ANIMATIONS_DURATION);
    recyclerViewInvite.addItemDecoration(new InviteListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.white_opacity_10), screenUtils.dpToPx(0.5f),
        getSectionCallback(adapter.getItems())));
    adapter.setItems(new ArrayList<>());
    recyclerViewInvite.setAdapter(adapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewInvite.getRecycledViewPool().setMaxRecycledViews(3, 50);

    LiveInviteSectionItemDecoration sectionItemDecoration = new LiveInviteSectionItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.list_live_invite_header_height), false,
        getSectionCallback(adapter.getItems()), screenUtils);
    recyclerViewInvite.addItemDecoration(sectionItemDecoration);

    subscriptions.add(adapter.onInvite()
        .map(view -> adapter.getItemAtPosition(recyclerViewInvite.getChildLayoutPosition(view)))
        .subscribe(item -> {
          User user = (User) item;
          liveInvitePresenter.createInvite(live.getRoomId(), user.getId());
        }));
  }

  private SectionCallback getSectionCallback(final List<LiveInviteAdapterSectionInterface> list) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        return position == 0 ||
            (position > 0 &&
                list.get(position).getSectionType() != list.get(position - 1).getSectionType());
      }

      @Override public int getSectionType(int position) {
        if (position == -1) return LiveInviteViewHeader.CHAT_MEMBERS;
        return itemsList.get(position).getSectionType();
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

  public void setLive(Live live) {
    this.live = live;

    subscriptions.add(Observable.combineLatest(live.onRoomUpdated().onBackpressureBuffer(),
        onShortcutUpdate.onBackpressureBuffer(), (room, listShortcut) -> {
          Set<String> alreadyPresent = new HashSet<>();
          List<LiveInviteAdapterSectionInterface> temp = new ArrayList<>();

          for (User user : room.getLiveUsers()) {
            if (!user.equals(currentUser)) {
              user.setCurrentRoomId(room.getId());
              user.setWaiting(room.isUserWaiting(user.getId()));
              computeUser(temp, user, alreadyPresent);
            }
          }

          for (User user : room.getInvitedUsers()) {
            user.setRinging(true);
            computeUser(temp, user, alreadyPresent);
          }

          temp.add(room);

          for (Shortcut shortcut : listShortcut) {
            User user = shortcut.getSingleFriend();
            computeUser(temp, user, alreadyPresent);
          }

          return temp;
        }).subscribeOn(singleThreadExecutor).map(newListItems -> {
      DiffUtil.DiffResult diffResult = null;
      List<LiveInviteAdapterSectionInterface> temp = new ArrayList<>(newListItems);

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
      //}
    }));
  }

  public void openInvite() {
    setVisibility(View.VISIBLE);

    recyclerViewInvite.animate()
        .alpha(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();

    recyclerViewInvite.animate()
        .translationY(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setDuration(DURATION)
        .start();
  }

  public void closeInvite() {
    recyclerViewInvite.animate()
        .alpha(0)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();

    recyclerViewInvite.animate()
        .translationY(translationY)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
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
  //   OBSERVABLES    //
  //////////////////////
}

