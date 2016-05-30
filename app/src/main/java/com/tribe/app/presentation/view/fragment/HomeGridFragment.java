package com.tribe.app.presentation.view.fragment;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.ChatInputView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

/**
 * Fragment that shows a list of Friends.
 */
public class HomeGridFragment extends BaseFragment implements HomeGridView {

    @Inject HomeGridPresenter homeGridPresenter;
    @Inject HomeGridAdapter homeGridAdapter;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    private HomeView homeView;
    private Subscription onClickChatSubscription;
    private PublishSubject<MarvelCharacter> clickChatViewSubject = PublishSubject.create();
    private Unbinder unbinder;

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
        this.getComponent(FriendshipComponent.class).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_home_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        setupRecyclerView();
        fragmentView.setTag(HomeActivity.GRID_FRAGMENT_PAGE);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.homeGridPresenter.attachView(this);
        if (savedInstanceState == null) {
            this.loadFriendList();
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

        if (onClickChatSubscription != null)
            onClickChatSubscription.unsubscribe();
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
    public void renderFriendList(List<MarvelCharacter> friendCollection) {
        if (friendCollection != null) {
            this.homeGridAdapter.setItems(friendCollection);
        }
    }

    @Override
    public void scrollToTop() {
        this.recyclerViewFriends.smoothScrollToPosition(0);
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public Context context() {
        return this.getActivity().getApplicationContext();
    }

    private void setupRecyclerView() {
        this.recyclerViewFriends.setLayoutManager(new HomeLayoutManager(context()));
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

        Observable<Integer> scrollDetector = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                recyclerViewFriends.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        subscriber.onNext(recyclerView.computeVerticalScrollOffset());
                    }
                });
            }
        });

        onClickChatSubscription = homeGridAdapter.onClickChat()
            .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
            .subscribe(clickChatViewSubject);

        if (homeView != null) homeView.initializeClicksOnChat(clickChatViewSubject);
        if (homeView != null) homeView.initializeScrollOnGrid(scrollDetector);
    }

    /**
     * Loads all friends.
     */
    private void loadFriendList() {
        this.homeGridPresenter.onCreate();
    }
}
