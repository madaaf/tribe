package com.tribe.app.presentation.view.fragment;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.FriendAdapter;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingBlockFragment extends BaseFragment {

    public static SettingBlockFragment newInstance() {

        Bundle args = new Bundle();

        SettingBlockFragment fragment = new SettingBlockFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Unbinder unbinder;

    @Inject
    FriendAdapter friendAdapter;

    @BindView(R.id.editTextSearchBlockFriends)
    EditTextFont editTextSearchBlockFriends;
    @BindView(R.id.blockFriendsRecyclerView)
    RecyclerView blockFriendsRecyclerView;

    private List<Friendship> friendshipsList;
    private List<Friendship> friendshipsListCopy;
    private LinearLayoutManager linearLayoutManager;


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

    private void initFriendshipList() {
        User user = getCurrentUser();
        List<Friendship> allFriendships = user.getFriendships();
        friendshipsList = new ArrayList<>();
        for(Friendship friendship : allFriendships) {
            if (friendship.isBlocked()) friendshipsList.add(friendship);
        }
        // TODO: remove after ui testing
        //friendshipsList.add(allFriendships.get(1));

        friendshipsListCopy = new ArrayList<>();
        friendshipsListCopy.addAll(friendshipsList);
        friendAdapter.setItems(friendshipsList);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        blockFriendsRecyclerView.setLayoutManager(linearLayoutManager);
        blockFriendsRecyclerView.setAdapter(friendAdapter);

        subscriptions.add(friendAdapter.clickFriendItem().subscribe(view -> {
           // TODO: add networking and update database
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            View bottomSheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_block_friend, null);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

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
        editTextSearchBlockFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        if(text.isEmpty()){
            friendshipsList.clear();
            friendshipsList.addAll(friendshipsListCopy);
        } else{
            ArrayList<Friendship> result = new ArrayList<>();
            text = text.toLowerCase();
            for(Friendship item: friendshipsListCopy){
                if(item.getDisplayName().toLowerCase().contains(text) || item.getDisplayName().toLowerCase().contains(text)){
                    result.add(item);
                }
            }
            friendshipsList.clear();
            friendshipsList.addAll(result);
        }
        friendAdapter.setItems(friendshipsList);
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


}
