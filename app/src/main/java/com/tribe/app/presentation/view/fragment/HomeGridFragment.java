package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of Friendships.
 */
public class HomeGridFragment extends BaseFragment implements HomeGridView {

    @Inject HomeGridPresenter homeGridPresenter;
    @Inject HomeGridAdapter homeGridAdapter;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Friendship> clickOpenTribes = PublishSubject.create();
    private PublishSubject<Friendship> clickChatViewSubject = PublishSubject.create();
    private PublishSubject<String> onRecordStart = PublishSubject.create();
    private PublishSubject<Friendship> onRecordEnd = PublishSubject.create();
    private PublishSubject<Integer> onPendingTribes = PublishSubject.create();
    private PublishSubject<List<Tribe>> onNewTribes = PublishSubject.create();

    // VARIABLES
    private HomeView homeView;
    private Unbinder unbinder;
    private HomeLayoutManager layoutManager;
    private BottomSheetDialog dialog;
    private User currentUser;
    private @CameraWrapper.TribeMode String tribeMode;
    private Tribe currentTribe;

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

        setupRecyclerView();
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
    public void renderFriendshipList(List<Friendship> friendCollection) {
        if (friendCollection != null) {
            this.homeGridAdapter.setItems(friendCollection);
        }
    }

    @Override
    public void updateTribes(List<Tribe> tribes) {
        subscriptions.add(Observable.zip(
            Observable.create(new Observable.OnSubscribe<List<Friendship>>() {
                @Override
                public void call(final Subscriber<? super List<Friendship>> subscriber) {
                    subscriber.onNext(homeGridAdapter.getItems());
                    subscriber.onCompleted();
                }
            }),
            Observable.create(new Observable.OnSubscribe<List<Tribe>>() {
                @Override
                public void call(final Subscriber<? super List<Tribe>> subscriber) {
                    subscriber.onNext(tribes);
                    subscriber.onCompleted();
                }
            }), (friendships, obsTribes) -> {
                List<Friendship> result = new ArrayList<>();
                Friendship me = friendships.remove(0);

                for (Friendship friendship : friendships) {
                    List<Tribe> newTribes = new ArrayList<>();

                    for (Tribe tribe : tribes) {
                        if (tribe.isToGroup() && tribe.getTo().getId().equals(friendship.getId())
                                || !tribe.isToGroup() && tribe.getFrom().getId().equals(friendship.getId())) {
                            newTribes.add(tribe);
                        }
                    }

                    friendship.setTribes(newTribes);
                }

                Collections.sort(friendships, (lhs, rhs) -> {
                    int res = Tribe.nullSafeComparator(lhs.getMostRecentTribe(), rhs.getMostRecentTribe());
                    if (res != 0) {
                        return res;
                    }

                    return Friendship.nullSafeComparator(lhs, rhs);
                });

                result.addAll(friendships);
                result.add(0, me);

                return result;
            }).observeOn(new UIThread().getScheduler())
            .subscribeOn(Schedulers.computation())
            .subscribe(friendshipList -> {
                homeGridAdapter.setItems(friendshipList);
            })
        );
    }

    @Override
    public void futureUpdateTribes(List<Tribe> tribes) {
        onNewTribes.onNext(tribes);
    }

    @Override
    public void updatePendingTribes(List<Tribe> pendingTribes) {
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
    public void setCurrentTribe(Tribe currentTribe) {
        this.currentTribe = currentTribe;
    }

    public void setTribeMode(String tribeMode) {
        this.tribeMode = tribeMode;
    }

    private void setupRecyclerView() {
        this.layoutManager = new HomeLayoutManager(context());
        this.recyclerViewFriends.setLayoutManager(layoutManager);
        this.recyclerViewFriends.setItemAnimator(null);
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

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
                .subscribe(clickOpenTribes));

        subscriptions.add(homeGridAdapter.onClickChat()
            .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
            .subscribe(clickChatViewSubject));

        subscriptions.add(homeGridAdapter.onClickMore()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(friendship -> {
                    setupBottomSheet();
                }));

        subscriptions.add(homeGridAdapter.onRecordStart()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .map(friendship -> homeGridPresenter.createTribe(currentUser, friendship, tribeMode))
                .doOnNext(str -> recyclerViewFriends.requestDisallowInterceptTouchEvent(true))
                .subscribe(onRecordStart));

        subscriptions.add(homeGridAdapter.onRecordEnd()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .doOnNext(friendship -> {
                    TileView tileView = (TileView) layoutManager.findViewByPosition(friendship.getPosition());
                    tileView.showTapToCancel(currentTribe);
                    homeGridAdapter.updateItemWithTribe(friendship.getPosition(), currentTribe);
                    recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
                })
                .subscribe(onRecordEnd));

        subscriptions.add(homeGridAdapter.onClickTapToCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(friendship -> {
                    FileUtils.deleteTribe(currentTribe.getLocalId());
                    homeGridPresenter.deleteTribe(currentTribe);
                    currentTribe = null;
                }));

        subscriptions.add(homeGridAdapter.onNotCancel()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(friendship -> {
                    homeGridPresenter.sendTribe(currentTribe);
                    currentTribe = null;
                }));

        if (homeView != null) homeView.initOpenTribes(clickOpenTribes);
        if (homeView != null) homeView.initClicksOnChat(clickChatViewSubject);
        if (homeView != null) homeView.initOnRecordStart(onRecordStart);
        if (homeView != null) homeView.initOnRecordEnd(onRecordEnd);
        if (homeView != null) homeView.initScrollOnGrid(scrollDetector);
        if (homeView != null) homeView.initPendingTribes(onPendingTribes);
        if (homeView != null) homeView.initNewTribes(onNewTribes);
    }

    private void setupBottomSheet() {
        if (dismissDialog()) {
            return;
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_user, null);
        dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(view);
        dialog.show();
    }

    private boolean dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
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
