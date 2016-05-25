package com.tribe.app.presentation.view.fragment;

import android.content.Context;
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
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment that shows a list of Friends.
 */
public class HomeGridFragment extends BaseFragment implements HomeGridView {

    /**
     * Interface for listening friend list events.
     */
    public interface FriendListListener {
        void onTextClicked(final MarvelCharacter friend);
    }

    @Inject HomeGridPresenter homeGridPresenter;
    @Inject HomeGridAdapter homeGridAdapter;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    private Unbinder unbinder;
    private FriendListListener friendListListener;

    public HomeGridFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FriendListListener) {
            this.friendListListener = (FriendListListener) context;
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
    public void onTextClicked(MarvelCharacter friend) {
        if (friendListListener != null) friendListListener.onTextClicked(friend);
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
        homeGridAdapter.setOnFriendClickListener(onFriendClickListener);
    }

    /**
     * Loads all friends.
     */
    private void loadFriendList() {
        this.homeGridPresenter.onCreate();
    }

    private UserGridAdapterDelegate.OnFriendClickListener onFriendClickListener =
            new UserGridAdapterDelegate.OnFriendClickListener() {
                @Override
                public void onTextClickListener(MarvelCharacter friend) {
                    if (HomeGridFragment.this.homeGridPresenter != null) {
                        HomeGridFragment.this.homeGridPresenter.onTextClicked(friend);
                    }
                }
            };
}
