package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.BlockedFriendAdapter;
import com.tribe.app.presentation.view.adapter.manager.MemberListLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/21/2016.
 */

public class SettingsBlockedHiddenView extends FrameLayout {

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.editTextSearch)
    EditTextFont editTextSearch;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    // VARIABLES
    private LinearLayoutManager layoutManager;
    private BlockedFriendAdapter friendAdapter;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<String> onUpdateFriendship = PublishSubject.create();

    public SettingsBlockedHiddenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initSearchView();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (friendAdapter != null) friendAdapter.releaseSubscriptions();
    }

    private void init() {
        subscriptions = new CompositeSubscription();

        friendAdapter = new BlockedFriendAdapter(getContext());

        layoutManager = new MemberListLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(friendAdapter);

        subscriptions.add(
                friendAdapter
                        .clickAdd()
                        .map(view -> friendAdapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)).getId())
                        .subscribe(onUpdateFriendship));
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
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        friendAdapter.filterList(text);
    }

    public void friendshipUpdated(Friendship friendship) {
        friendAdapter.updateFriendship(friendship);
    }

    public void renderBlockedFriendshipList(List<Friendship> friendshipList) {
        friendAdapter.setItems(friendshipList);
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(((Activity) getContext()));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    // OBSERVABLES

    public Observable<String> onUpdateFriendship() {
        return onUpdateFriendship;
    }
}
