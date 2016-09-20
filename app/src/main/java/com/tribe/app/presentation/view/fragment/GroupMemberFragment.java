package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GroupMemberPresenter;
import com.tribe.app.presentation.mvp.view.GroupMemberView;
import com.tribe.app.presentation.view.adapter.GroupMemberAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMemberFragment extends BaseFragment implements GroupMemberView {

    public static GroupsGridFragment newInstance(Bundle args) {
        GroupsGridFragment fragment = new GroupsGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupMemberFragment() {
        setRetainInstance(true);
    }

    // Subscriptions
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> imageBackClicked = PublishSubject.create();

    // Bind view
    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;
    @BindView(R.id.imgBack)
    ImageView imgBack;
    @BindView(R.id.editTextSearchGroupMembers)
    EditTextFont editTextSearchGroupMembers;
    @BindView(R.id.recyclerViewGroupMembers)
    RecyclerView recyclerViewGroupMembers;

    // Dagger Dependencies
    @Inject
    GroupMemberPresenter groupMemberPresenter;
    @Inject
    GroupMemberAdapter groupMemberAdapter;

    // Variables
    private String groupId;
    private LabelSheetAdapter labelSheetAdapter;
    private List<GroupMember> groupMemberList = new ArrayList<>();
    private List<GroupMember> groupMemberListCopy = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_group_member, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();
        initUi();
        return fragmentView;
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

    private void initUi() {
        // Subscriptions
        subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
            imageBackClicked.onNext(null);
        }));
    }

    public Observable<Void> imageBackClicked() {
        return imageBackClicked;
    }

    /**
     * Init Search & List
     */

    private void initGroupMemberList() {

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
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return null;
    }

    /**
     * Dependency injection set-up
     */
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
