package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.entity.PendingType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasRatedApp;
import com.tribe.app.presentation.internal.di.scope.TribeSentCount;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.mvp.view.UpdateScore;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.activity.BaseActionActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.PullToSearchContainer;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.dialog_fragment.PointsDialogFragment;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of Recipients.
 */
public class HomeGridFragment extends BaseFragment implements HomeGridView, UpdateScore {

    private static final int TIME_MIN_RECORDING = 1500; // IN MS

    @Inject
    HomeGridPresenter homeGridPresenter;

    @Inject
    HomeGridAdapter homeGridAdapter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    @TribeSentCount
    Preference<Integer> tribeSentCount;

    @Inject
    @HasRatedApp
    Preference<Boolean> hasRatedApp;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    @BindView(R.id.pullToSearchContainer)
    PullToSearchContainer pullToSearchContainer;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Recipient> clickOpenTribes = PublishSubject.create();
    private PublishSubject<Recipient> clickChatViewSubject = PublishSubject.create();
    private PublishSubject<String> onRecordStart = PublishSubject.create();
    private PublishSubject<Recipient> onRecordEnd = PublishSubject.create();
    private PublishSubject<Integer> onPendingTribes = PublishSubject.create();
    private PublishSubject<List<TribeMessage>> onPendingTribesSelected = PublishSubject.create();
    private PublishSubject<List<Message>> onNewMessages = PublishSubject.create();
    private PublishSubject<View> clickOpenPoints = PublishSubject.create();
    private PublishSubject<View> clickOpenSettings = PublishSubject.create();
    private PublishSubject<Boolean> activePullToSearch = PublishSubject.create();

    // VARIABLES
    private HomeView homeView;
    private Unbinder unbinder;
    private HomeLayoutManager layoutManager;
    private BottomSheetDialog dialogMore;
    private RecyclerView recyclerViewMore;
    private LabelSheetAdapter moreSheetAdapter;
    private User currentUser;
    private @CameraWrapper.TribeMode String tribeMode;
    private boolean isRecording;
    private long timeRecording;
    private List<TribeMessage> pendingTribes;
    private BottomSheetDialog bottomSheetPendingTribeDialog;
    private RecyclerView recyclerViewPending;
    private LabelSheetAdapter labelSheetAdapter;
    private int verticalScrollOffset = 0;
    private String filter = null;
    private boolean shouldReloadGrid = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof HomeView) {
            this.homeView = (HomeView) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_home_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);

        fragmentView.setTag(HomeActivity.GRID_FRAGMENT_PAGE);

        init();
        initResources();
        initRecyclerView();
        initPullToSearch();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.homeGridPresenter.attachView(this);

        if (savedInstanceState == null) loadData();

        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.ONBOARDING_GRID_VIEW, true);
        tagManager.setPropertyOnce(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.homeGridPresenter.onResume();
        currentUser.addScoreListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.homeGridPresenter.onPause();
        currentUser.removeScoreListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewFriends.setAdapter(null);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.homeGridPresenter.onDestroy();
        this.homeGridAdapter.releaseSubscriptions();

        if (subscriptions != null && subscriptions.hasSubscriptions())
            subscriptions.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.homeGridPresenter = null;
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void renderRecipientList(List<Recipient> recipientList) {
        if (recipientList != null && !isRecording) {
            Bundle bundle = new Bundle();
            bundle.putInt(TagManagerConstants.COUNT_FRIENDS, currentUser.getFriendships().size());
            bundle.putInt(TagManagerConstants.COUNT_GROUPS, currentUser.getFriendships().size());
            tagManager.setProperty(bundle);

            // We remove the current user from the list
            if (pullToSearchContainer != null) pullToSearchContainer.updatePTSList(recipientList.subList(1, recipientList.size() - 1));
            this.homeGridAdapter.setItems(recipientList);
        }
    }

    @Override
    public void updateReceivedMessages(List<Message> messages) {
        onNewMessages.onNext(messages);
    }

    @Override
    public void updatePendingTribes(List<TribeMessage> pendingTribes) {
        this.pendingTribes = pendingTribes;
        onPendingTribes.onNext(pendingTribes.size());
    }

    @Override
    public void scrollToTop() {
        if (homeGridAdapter != null && layoutManager.findFirstVisibleItemPosition() > 15)
            this.recyclerViewFriends.scrollToPosition(10);

        this.recyclerViewFriends.postDelayed(() -> recyclerViewFriends.smoothScrollToPosition(0), 100);
    }

    @Override
    public int getNbItems() {
        return this.homeGridAdapter.getItemCount();
    }

    @Override
    public void refreshGrid() {
        reloadGrid();
    }

    @Override
    public void onFriendshipUpdated(Friendship friendship) {
        reloadGrid();
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context context() {
        if (isAdded() && getActivity() != null) return this.getActivity().getApplicationContext();

        return null;
    }

    @Override
    public void setCurrentTribe(TribeMessage currentTribe) {
    }

    @Override
    public void showPendingTribesMenu() {
        setupBottomSheetPendingTribes(homeGridAdapter.getItems().toArray(new Recipient[homeGridAdapter.getItems().size()]));
    }

    public void setTribeMode(String tribeMode) {
        this.tribeMode = tribeMode;
    }

    public void reloadGrid() {
        filter = null;
        homeGridAdapter.filterList(null);
        this.homeGridPresenter.loadFriendList(null);
    }

    public void updateNewTribes() {
        this.homeGridPresenter.reload();
    }

    private void init() {
        this.getComponent(UserComponent.class).inject(this);
        this.currentUser = getCurrentUser();
    }

    private void initResources() {
        
    }

    private void initRecyclerView() {
        this.layoutManager = new HomeLayoutManager(context());
        this.recyclerViewFriends.setLayoutManager(layoutManager);
        this.recyclerViewFriends.setItemAnimator(null);
        List<Recipient> recipientList = new ArrayList<>();
        Friendship friendship = new Friendship(currentUser.getId());
        friendship.setFriend(currentUser);
        recipientList.add(0, friendship);
        homeGridAdapter.setItems(recipientList);
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

        // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

        Observable<Integer> scrollDetector = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
                        subscriber.onNext(verticalScrollOffset);
                    }
                });
            }
        });

        subscriptions.add(homeGridAdapter.onOpenTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .filter(recipient -> {
                    boolean filter = recipient.getReceivedTribes() != null
                            && recipient.getReceivedTribes().size() > 0
                            && recipient.hasLoadedOrErrorTribes();

                    return filter;
                })
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(clickOpenTribes));

        subscriptions.add(homeGridAdapter.onClickChat()
            .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
            .subscribe(clickChatViewSubject));

        subscriptions.add(homeGridAdapter.onClickMore()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    setupBottomSheetMore(recipient);
                }));

        subscriptions.add(homeGridAdapter.onClickErrorTribes()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    setupBottomSheetPendingTribes(recipient);
                }));

        subscriptions.add(homeGridAdapter.onRecordStart()
                .doOnNext(view -> isRecording = true)
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .map(recipient -> {
                    timeRecording = System.currentTimeMillis();
                    TribeMessage currentTribe = homeGridPresenter.createTribe(currentUser, recipient, tribeMode);
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), currentTribe);
                    recyclerViewFriends.postDelayed(() -> homeGridAdapter.notifyItemChanged(recipient.getPosition()), 200);
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
                    return currentTribe.getLocalId();
                })
                .subscribe(onRecordStart));

        subscriptions.add(homeGridAdapter.onRecordEnd()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .doOnNext(recipient -> {
                    TileView tileView = (TileView) layoutManager.findViewByPosition(recipient.getPosition());
                    if ((System.currentTimeMillis() - timeRecording) > TIME_MIN_RECORDING) {
                        tileView.showTapToCancel(recipient.getTribe(), tribeMode);
                        recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                    } else {
                        cleanupCurrentTribe(recipient);
                        tileView.resetViewAfterTapToCancel(false);
                    }
                })
                .subscribe(onRecordEnd));

        subscriptions.add(homeGridAdapter.onClickTapToCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    isRecording = false;
                    cleanupCurrentTribe(recipient);
                }));

        subscriptions.add(homeGridAdapter.onNotCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .filter(recipient -> {
                    isRecording = false;
                    TribeMessage tr = recipient.getTribe();

                    if (tr == null || tr.getTo() == null) {
                        cleanupCurrentTribe(recipient);
                        return false;
                    }

                    return true;
                })
                .map(recipient -> {
                    TribeMessage tr = recipient.getTribe();

                    homeGridPresenter.sendTribe(recipient.getTribe());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
                    return tr.getLocalId();
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(s -> {
                    int tribeSent = tribeSentCount.get();
                    tribeSentCount.set(++tribeSent);

                    if (tribeSentCount.get() >= Constants.RATING_COUNT && tribeSentCount.get() % Constants.RATING_COUNT == 0 && !hasRatedApp.get())
                        navigator.computeActions(getActivity(), false, BaseActionActivity.ACTION_RATING);
                })
                .delay(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> homeGridPresenter.confirmTribe(s)));

        subscriptions.add(homeGridAdapter.onClickOpenPoints()
                .subscribe(clickOpenPoints));

        subscriptions.add(homeGridAdapter.onClickOpenSettings()
        .subscribe(clickOpenSettings));

        if (homeView != null) homeView.initOpenTribes(clickOpenTribes);
        if (homeView != null) homeView.initClicksOnChat(clickChatViewSubject);
        if (homeView != null) homeView.initOnRecordStart(onRecordStart);
        if (homeView != null) homeView.initOnRecordEnd(onRecordEnd);
        if (homeView != null) homeView.initScrollOnGrid(scrollDetector);
        if (homeView != null) homeView.initPendingTribes(onPendingTribes);
        if (homeView != null) homeView.initPendingTribeItemSelected(onPendingTribesSelected);
        if (homeView != null) homeView.initNewMessages(onNewMessages);
        if (homeView != null) homeView.initClickOnPoints(clickOpenPoints);
        if (homeView != null) homeView.initClickOnSettings(clickOpenSettings);
        if (homeView != null) homeView.initPullToSearchActive(activePullToSearch);
    }

    private void initPullToSearch() {
        subscriptions.add(pullToSearchContainer.pullToSearchActive().doOnNext(active -> {
            recyclerViewFriends.setEnabled(!active);
            layoutManager.setScrollEnabled(!active);
            homeGridAdapter.setAllItemsEnabled(!active);
            recyclerViewFriends.requestDisallowInterceptTouchEvent(active);
            recyclerViewFriends.getParent().requestDisallowInterceptTouchEvent(active);
        }).subscribe(activePullToSearch));

        subscriptions.add(pullToSearchContainer.onLetterSelected().subscribe(s -> {
            filter = s;
            homeGridAdapter.filterList(s);
        }));
    }

    private void cleanupCurrentTribe(Recipient recipient) {
        homeGridPresenter.deleteTribe(recipient.getTribe());
        homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
        homeGridAdapter.notifyItemChanged(recipient.getPosition());
    }

    private void setupBottomSheetMore(Recipient recipient) {
        if (dismissDialogSheetMore()) {
            return;
        }

        List<LabelType> moreTypes = new ArrayList<>();
        moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_clear_tribes), MoreType.CLEAR_MESSAGES));

        if (recipient instanceof Friendship) {
            moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_hide, recipient.getDisplayName()), MoreType.HIDE));
            moreTypes.add(new MoreType(getString(R.string.grid_menu_friendship_block, recipient.getDisplayName()), MoreType.BLOCK_HIDE));
        }

        if (recipient instanceof Membership) {
            Membership membership = (Membership) recipient;
            moreTypes.add(new MoreType(getString(R.string.grid_menu_group_infos), MoreType.GROUP_INFO));
            if (membership.isAdmin())moreTypes.add(new MoreType(getString(R.string.grid_menu_group_delete), MoreType.GROUP_DELETE));
            else  moreTypes.add(new MoreType(getString(R.string.grid_menu_group_leave), MoreType.GROUP_LEAVE));
        }

        prepareBottomSheetMore(recipient, moreTypes);
    }

    private boolean dismissDialogSheetMore() {
        if (dialogMore != null && dialogMore.isShowing()) {
            dialogMore.dismiss();
            return true;
        }

        return false;
    }

    private void prepareBottomSheetMore(Recipient recipient, List<LabelType> items) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_more, null);
        recyclerViewMore = (RecyclerView) view.findViewById(R.id.recyclerViewMore);
        recyclerViewMore.setHasFixedSize(true);
        recyclerViewMore.setLayoutManager(new LinearLayoutManager(getActivity()));
        moreSheetAdapter = new LabelSheetAdapter(context(), items);
        moreSheetAdapter.setHasStableIds(true);
        recyclerViewMore.setAdapter(moreSheetAdapter);
        subscriptions.add(moreSheetAdapter.clickLabelItem()
                .map(labelView -> moreSheetAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    MoreType moreType = (MoreType) labelType;
                    if (moreType.getMoreType().equals(MoreType.CLEAR_MESSAGES)) {
                        homeGridPresenter.markTribeListAsRead(recipient, recipient.getReceivedTribes());
                    } else if (moreType.getMoreType().equals(MoreType.HIDE) || moreType.getMoreType().equals(MoreType.BLOCK_HIDE)) {
                        tagManager.trackEvent(TagManagerConstants.USER_TILE_HIDDEN);
                        homeGridPresenter.updateFriendship((Friendship) recipient, moreType.getMoreType().equals(MoreType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED : FriendshipRealm.HIDDEN);
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_INFO)) {
                        Membership membership = (Membership) recipient;
                        navigator.navigateToGroupInfo(getActivity(), membership.getId(), membership.isAdmin(), membership.getGroup().getId(), membership.getGroup().getName(), membership.getGroup().getPicture(), membership.getLink(), membership.getLink_expires_at());
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_LEAVE)) {
                        homeGridPresenter.leaveGroup(recipient.getId());
                    } else if (moreType.getMoreType().equals(MoreType.GROUP_DELETE)) {
                        homeGridPresenter.removeGroup(recipient.getSubId());
                    }

                    dismissDialogSheetMore();
                }));

        dialogMore = new BottomSheetDialog(getContext());
        dialogMore.setContentView(view);
        dialogMore.show();
        dialogMore.setOnDismissListener(dialog -> {
            moreSheetAdapter.releaseSubscriptions();
            dialogMore = null;
        });
    }

    private void setupBottomSheetPendingTribes(Recipient ... recipientList) {
        if (dismissDialogSheetPendingTribes()) {
            return;
        }

        List<LabelType> pendingTypes = new ArrayList<>();

        for (Recipient recipient : recipientList) {
            if (recipient.getErrorTribes() != null && recipient.getErrorTribes().size() > 0)
                pendingTypes.addAll(recipient.createPendingTribeItems(getContext(), recipientList.length > 1));
        }

        pendingTypes.add(new PendingType(new ArrayList<>(pendingTribes),
                getString(R.string.grid_unsent_tribes_action_resend_all),
                PendingType.RESEND));

        pendingTypes.add(new PendingType(new ArrayList<>(pendingTribes),
                getString(R.string.grid_unsent_tribes_action_delete_all),
                PendingType.DELETE));

        prepareBottomSheetPendingWithList(pendingTypes, recipientList.length > 1);
    }

    private void prepareBottomSheetPendingWithList(List<LabelType> items, boolean isGlobal) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_user_pending, null);
        recyclerViewPending = (RecyclerView) view.findViewById(R.id.recyclerViewPending);
        recyclerViewPending.setHasFixedSize(true);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(getActivity()));
        labelSheetAdapter = new LabelSheetAdapter(context(), items);
        labelSheetAdapter.setHasStableIds(true);
        recyclerViewPending.setAdapter(labelSheetAdapter);
        subscriptions.add(labelSheetAdapter.clickLabelItem()
                .map(pendingTribeView -> labelSheetAdapter.getItemAtPosition((Integer) pendingTribeView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    PendingType pendingType = (PendingType) labelType;

                    List<TribeMessage> messages = new ArrayList<>();
                    for (Message message : pendingType.getPending()) {
                        messages.add((TribeMessage) message);
                    }

                    if (isGlobal) onPendingTribesSelected.onNext(messages);

                    if (pendingType.getPendingType().equals(PendingType.DELETE)) {
                        homeGridPresenter.deleteTribe(pendingType.getPending().toArray(new TribeMessage[pendingType.getPending().size()]));
                    } else {
                        homeGridPresenter.sendTribe(pendingType.getPending().toArray(new TribeMessage[pendingType.getPending().size()]));
                    }

                    dismissDialogSheetPendingTribes();
                }));

        bottomSheetPendingTribeDialog = new BottomSheetDialog(getContext());
        bottomSheetPendingTribeDialog.setContentView(view);
        bottomSheetPendingTribeDialog.show();
        bottomSheetPendingTribeDialog.setOnDismissListener(dialog -> {
            labelSheetAdapter.releaseSubscriptions();
            bottomSheetPendingTribeDialog = null;
        });
    }

    private boolean dismissDialogSheetPendingTribes() {
        if (bottomSheetPendingTribeDialog != null && bottomSheetPendingTribeDialog.isShowing()) {
            bottomSheetPendingTribeDialog.dismiss();
            return true;
        }

        return false;
    }

    /**
     * Loads all friends / tribes.
     */
    private void loadData() {
        subscriptions.add(Observable.timer(100, TimeUnit.MILLISECONDS).onBackpressureDrop().observeOn(AndroidSchedulers.mainThread()).subscribe(t ->  {
            if (isAdded()) this.homeGridPresenter.onCreate();
        }));
    }

    @Override
    public void updateScore(int previousScore, int newScore) {
        if (homeGridAdapter != null && homeGridAdapter.getItems().size() > 0) {
            homeGridAdapter.getItemAtPosition(0).setScore(currentUser.getScore());
            homeGridAdapter.notifyItemChanged(0);

            ScoreUtils.Level previousLevel = ScoreUtils.getLevelForScore(previousScore);
            ScoreUtils.Level level = ScoreUtils.getLevelForScore(newScore);

            if (level.getPoints() != previousLevel.getPoints()) {
                PointsDialogFragment pointsDialogFragment = PointsDialogFragment.newInstance(level);
                pointsDialogFragment.show(getFragmentManager(), PointsDialogFragment.class.getName());
            }
        }
    }
}
