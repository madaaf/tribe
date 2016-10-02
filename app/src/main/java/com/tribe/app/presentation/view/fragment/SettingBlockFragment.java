package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.BlockPresenter;
import com.tribe.app.presentation.mvp.view.BlockView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.BlockedFriendAdapter;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingBlockFragment extends BaseFragment implements BlockView {

    public static SettingBlockFragment newInstance() {

        Bundle args = new Bundle();

        SettingBlockFragment fragment = new SettingBlockFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    BlockPresenter blockPresenter;

    @BindView(R.id.editTextSearch)
    EditTextFont editTextSearch;

    @BindView(R.id.blockFriendsRecyclerView)
    RecyclerView blockFriendsRecyclerView;

    // VARIABLES
    private LinearLayoutManager linearLayoutManager;
    private BlockedFriendAdapter friendAdapter;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_setting_block, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();
        initFriendshipList();
        initSearchView();

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        this.blockPresenter.onDestroy();
        this.friendAdapter.releaseSubscriptions();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.blockPresenter.attachView(this);
        this.blockPresenter.onCreate();
    }

    private void initFriendshipList() {
        friendAdapter = new BlockedFriendAdapter(getContext());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        blockFriendsRecyclerView.setLayoutManager(linearLayoutManager);
        blockFriendsRecyclerView.setAdapter(friendAdapter);

        subscriptions.add(
                friendAdapter
                .clickAdd()
                .map(view -> {
                    return friendAdapter.getItemAtPosition(blockFriendsRecyclerView.getChildLayoutPosition(view));
                }).subscribe(fr -> {
                    blockPresenter.updateFriendship(fr.getId());
                }));
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initSearchView() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtils.isEmpty(s.toString())) filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        friendAdapter.filterList(text);
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }


    @Override
    public void friendshipUpdated(Friendship friendship) {
        friendAdapter.updateFriendship(friendship);
    }

    @Override
    public void renderBlockedFriendshipList(List<Friendship> friendshipList) {
        friendAdapter.setItems(friendshipList);
    }
}
