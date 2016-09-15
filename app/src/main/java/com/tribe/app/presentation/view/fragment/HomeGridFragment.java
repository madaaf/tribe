package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.entity.PendingType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.GradientCircleView;

import java.util.ArrayList;
import java.util.List;

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
public class HomeGridFragment extends BaseFragment implements HomeGridView {

    @Inject HomeGridPresenter homeGridPresenter;
    @Inject HomeGridAdapter homeGridAdapter;
    @Inject Navigator navigator;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

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

    // VARIABLES
    private HomeView homeView;
    private Unbinder unbinder;
    private HomeLayoutManager layoutManager;
    private BottomSheetDialog dialogMore;
    private RecyclerView recyclerViewMore;
    private LabelSheetAdapter moreSheetAdapter;
    private User currentUser;
    private @CameraWrapper.TribeMode String tribeMode;
    private TribeMessage currentTribe; // The tribe currently being recorded / sent
    private long startRecording;
    private List<TribeMessage> pendingTribes;
    private BottomSheetDialog bottomSheetPendingTribeDialog;
    private RecyclerView recyclerViewPending;
    private LabelSheetAdapter labelSheetAdapter;

    // TOUCH HANDLING
    private int activePointerId;
    private float lastDownY, lastDownYTr;
    private static final float DRAG_RATE = 0.5f;
    private float currentDragPercent;
    private int currentOffsetBottom;
    private int thresholdAlphaEnd;
    private int thresholdEnd;
    private Spring springAlpha;
    private Spring springAlphaSwipeDown;
    private SpringSystem springSystem = null;
    private VelocityTracker velocityTracker;


    public HomeGridFragment() {
        setRetainInstance(true);
    }

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
        this.getComponent(UserComponent.class).inject(this);
        this.currentUser = getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_home_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);

        fragmentView.setTag(HomeActivity.GRID_FRAGMENT_PAGE);

        init();
        initRecyclerView();
//        initPullToSearch();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.homeGridPresenter.attachView(this);
        if (savedInstanceState == null) {
            this.loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.homeGridPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.homeGridPresenter.onPause();
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
        if (recipientList != null) {
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
        this.recyclerViewFriends.smoothScrollToPosition(0);
    }

    @Override
    public int getNbItems() {
        return this.homeGridAdapter.getItemCount();
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context context() {
        return this.getActivity().getApplicationContext();
    }

    @Override
    public void setCurrentTribe(TribeMessage currentTribe) {
        this.currentTribe = currentTribe;
    }

    @Override
    public void showPendingTribesMenu() {
        setupBottomSheetPendingTribes(homeGridAdapter.getItems().toArray(new Recipient[homeGridAdapter.getItems().size()]));
    }

    public void setTribeMode(String tribeMode) {
        this.tribeMode = tribeMode;
    }

    public void reloadGrid() {
        this.homeGridPresenter.loadFriendList();
    }

//    public void prepareTapToCancel(String localId) {
//        if (currentRecipient != null) {
//            TileView tileView = (TileView) layoutManager.findViewByPosition(currentRecipient.getPosition());
//            tileView.preparePlayer(localId);
//        }
//    }

    private void init() {
    }

    private void initPullToSearch() {
        thresholdEnd = getResources().getDimensionPixelSize(R.dimen.threshold_dismiss);
        thresholdAlphaEnd = thresholdEnd >> 1;

        springSystem = SpringSystem.create();

        springAlpha = springSystem.createSpring();
        springAlphaSwipeDown = springSystem.createSpring();

        velocityTracker = VelocityTracker.obtain();

        recyclerViewFriends.setOnTouchListener((view, event) -> {

            if (layoutManager.findFirstVisibleItemPosition() == 0) {

                final int action = event.getAction();

                switch (action) {


                    case MotionEvent.ACTION_DOWN:
                        lastDownY = event.getRawY();
                        lastDownYTr = view.getTranslationY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        activePointerId = event.getPointerId(0);

//                        if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_DOWN) {
                            final int pointerIndex = event.findPointerIndex(activePointerId);

                            if (pointerIndex != -1) {
                                final int location[] = {0, 0};
                                view.getLocationOnScreen(location);

                                float y = event.getY(pointerIndex) + location[1];

                                float offsetY = y - lastDownY + lastDownYTr;

                                if (offsetY >= 0) {
                                    return applyOffsetBottomWithTension(offsetY, view);
                                }

                            }

//                        }
                        velocityTracker.addMovement(event);
                    default:
                        break;
                }
            }

            return false;
        });
    }


    private boolean applyOffsetBottomWithTension(float offsetY, View view) {
        float totalDragDistance = view.getHeight() / 3;
        final float scrollTop = offsetY * DRAG_RATE;
        currentDragPercent = scrollTop / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetBottom = computeOffsetWithTension(scrollTop, totalDragDistance);
        scrollBottom(currentOffsetBottom);

        springAlpha.setCurrentValue(1 - (offsetY / thresholdAlphaEnd));
        springAlphaSwipeDown.setCurrentValue(offsetY / (thresholdAlphaEnd * 2));
        return true;
    }

    private int computeOffsetWithTension(float scrollDist, float totalDragDistance) {
        float boundedDragPercent = Math.min(1f, Math.abs(currentDragPercent));
        float extraOS = Math.abs(scrollDist) - totalDragDistance;
        float slingshotDist = totalDragDistance;
        float tensionSlingshotPercent = Math.max(0,
                Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (slingshotDist) * tensionPercent / 2;
        return (int) ((slingshotDist * boundedDragPercent) + extraMove);
    }

    private void scrollBottom(float value) {
        recyclerViewFriends.setTranslationY(value);
        //Todo: add pull to search here
    }

    private void initRecyclerView() {
        this.layoutManager = new HomeLayoutManager(context());
        this.recyclerViewFriends.setLayoutManager(layoutManager);
        this.recyclerViewFriends.setItemAnimator(null);
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
                        int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
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
                            && recipient.hasLoadedTribes();

                    if (!filter) homeGridPresenter.downloadMessages(new ArrayList<>(recipient.getReceivedTribes()));

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
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .map(recipient -> {
                    startRecording = System.currentTimeMillis();
                    String tribeId = homeGridPresenter.createTribe(currentUser, recipient, tribeMode);
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), currentTribe);
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                    return tribeId;
                })
                .doOnNext(str -> recyclerViewFriends.requestDisallowInterceptTouchEvent(true))
                .subscribe(onRecordStart));

        subscriptions.add(homeGridAdapter.onRecordEnd()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .doOnNext(recipient -> {
                    TileView tileView = (TileView) layoutManager.findViewByPosition(recipient.getPosition());
                    tileView.showTapToCancel(currentTribe, tribeMode);
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), currentTribe);
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                })
                .subscribe(onRecordEnd));

        subscriptions.add(homeGridAdapter.onClickTapToCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    homeGridPresenter.deleteTribe(recipient.getTribe());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
                    currentTribe = null;
                }));

        subscriptions.add(homeGridAdapter.onNotCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    homeGridPresenter.sendTribe(recipient.getTribe());
                    homeGridAdapter.updateItemWithTribe(recipient.getPosition(), null);
                    currentTribe = null;
                }));

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
    }

    private void setupBottomSheetMore(Recipient recipient) {
        if (dismissDialogSheetMore()) {
            return;
        }

        List<LabelType> moreTypes = new ArrayList<>();
        moreTypes.add(new MoreType(getString(R.string.grid_more_clear_all_messages), MoreType.CLEAR_MESSAGES));
        if (recipient instanceof Friendship) {
            moreTypes.add(new MoreType(getString(R.string.grid_more_hide), MoreType.HIDE));
            moreTypes.add(new MoreType(getString(R.string.grid_more_block_hide), MoreType.BLOCK_HIDE));
        }
        if (recipient instanceof Group) {
            moreTypes.add(new MoreType(getString(R.string.grid_menu_group_infos), MoreType.GROUP_INFO));
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
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_user_more, null);
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
                        homeGridPresenter.markTribeListAsRead(recipient);
                    }
                    if (moreType.getMoreType().equals(MoreType.GROUP_INFO)) {
                        navigator.navigateToGroupInfo(getActivity(),  recipient.getId());
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

        pendingTypes.add(new PendingType(pendingTribes,
                getString(R.string.grid_unsent_tribes_action_resend_all),
                PendingType.RESEND));

        pendingTypes.add(new PendingType(pendingTribes,
                getString(R.string.grid_unsent_tribes_action_delete_all),
                PendingType.DELETE));

        prepareBottomSheetPendingWithList(pendingTypes, recipientList.length > 1);
    }

    private void prepareBottomSheetPendingWithList(List<LabelType> items, boolean isGlobal) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_user_pending, null);
        recyclerViewPending = (RecyclerView) view.findViewById(R.id.recyclerViewPendingTribes);
        recyclerViewPending.setHasFixedSize(true);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(getActivity()));
        labelSheetAdapter = new LabelSheetAdapter(context(), items);
        labelSheetAdapter.setHasStableIds(true);
        recyclerViewPending.setAdapter(labelSheetAdapter);
        subscriptions.add(labelSheetAdapter.clickLabelItem()
                .map(pendingTribeView -> labelSheetAdapter.getItemAtPosition((Integer) pendingTribeView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    PendingType pendingType = (PendingType) labelType;

                    if (isGlobal) onPendingTribesSelected.onNext(pendingType.getPending());

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
        this.homeGridPresenter.onCreate();
    }



}
