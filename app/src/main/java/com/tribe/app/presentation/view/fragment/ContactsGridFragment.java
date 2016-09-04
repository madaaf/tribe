package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.ContactsGridPresenter;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.ContactsGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of discovery users.
 */
public class ContactsGridFragment extends BaseFragment implements ContactsView {

    @Inject
    ContactsGridPresenter contactsGridPresenter;

    @Inject
    ContactsGridAdapter contactsGridAdapter;

    @BindView(R.id.recyclerViewContacts)
    RecyclerView recyclerViewContacts;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();


    // VARIABLES
    private HomeView homeView;
    private Unbinder unbinder;
    private ContactsLayoutManager layoutManager;
    private User currentUser;

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
        final View fragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);

        fragmentView.setTag(HomeActivity.CONTACTS_FRAGMENT_PAGE);

        init();
        initRecyclerView();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.contactsGridPresenter.attachView(this);
        if (savedInstanceState == null) {
            this.loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.contactsGridPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.contactsGridPresenter.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewContacts.setAdapter(null);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.contactsGridPresenter.onDestroy();
        this.contactsGridAdapter.releaseSubscriptions();

        if (subscriptions != null && subscriptions.hasSubscriptions())
            subscriptions.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.contactsGridPresenter = null;
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
        this.showToastMessage(message);
    }

    @Override
    public Context context() {
        return this.getActivity().getApplicationContext();
    }

    private void init() {
    }

    private void initRecyclerView() {
        this.layoutManager = new ContactsLayoutManager(context());
        this.recyclerViewContacts.setLayoutManager(layoutManager);
        this.recyclerViewContacts.setItemAnimator(null);
        this.recyclerViewContacts.setAdapter(contactsGridAdapter);

        contactsGridAdapter.setItems(new ArrayList<>());
    }


    /**
     * Loads all contacts.
     */
    private void loadData() {
        this.contactsGridPresenter.onCreate();
    }
}
