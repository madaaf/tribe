package com.tribe.app.presentation.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.ButtonPoints;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.ContactsGridPresenter;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.ContactsGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.ButtonPointsView;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of discovery users.
 */
public class ContactsGridFragment extends BaseFragment implements ContactsView {

    private static final int DURATION = 300;
    private static final float OVERSHOOT = 0.75f;

    @Inject
    ContactsGridPresenter contactsGridPresenter;

    @Inject
    ContactsGridAdapter contactsGridAdapter;

    @Inject
    PhoneUtils phoneUtils;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.recyclerViewContacts)
    RecyclerView recyclerViewContacts;

    @BindView(R.id.editTextSearchContact)
    EditTextFont editTextSearchContact;

    @BindView(R.id.btnCloseSearch)
    ImageView btnCloseSearch;

    @BindView(R.id.layoutDummy)
    ViewGroup layoutDummy;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private boolean isSearchMode = false;
    private HomeView homeView;
    private Unbinder unbinder;
    private ContactsLayoutManager layoutManager;
    private User currentUser;
    private List<Contact> contactList;
    private List<Contact> searchContactList;
    private SearchResult searchResult;

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

    @OnClick(R.id.btnCloseSearch)
    public void closeSearch() {
        editTextSearchContact.setText("");
        InputMethodManager inputMethodManager = (InputMethodManager) context().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        layoutDummy.requestFocus();
    }

    private void init() {
        this.getComponent(UserComponent.class).inject(this);
        this.currentUser = getCurrentUser();

        RxTextView.textChanges(editTextSearchContact).map(CharSequence::toString)
                .doOnNext(s -> {
                    if (StringUtils.isEmpty(s)) {
                        isSearchMode = false;
                        hideClearSearch();
                        if (contactList != null && contactList.size() > 0)
                            renderContactList(contactList);
                    }
                })
                .filter(s -> !StringUtils.isEmpty(s))
                .doOnNext(s -> {
                    if (!isSearchMode) showClearSearch();
                    isSearchMode = true;
                    searchResult = new SearchResult();
                    searchResult.setUsername(s);
                    updateSearch();
                    contactsGridPresenter.findByValue(s);
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(s -> contactsGridPresenter.findByUsername(s));

        searchContactList = new ArrayList<>();
        btnCloseSearch.setTranslationX(screenUtils.getWidthPx() >> 1);
    }

    private void initRecyclerView() {
        this.layoutManager = new ContactsLayoutManager(context());
        this.recyclerViewContacts.setLayoutManager(layoutManager);
        this.recyclerViewContacts.setItemAnimator(null);
        this.recyclerViewContacts.setAdapter(contactsGridAdapter);

        contactsGridAdapter.setItems(new ArrayList<>());

        subscriptions.add(contactsGridAdapter.onButtonPointsClick()
                .map(view -> contactsGridAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof ButtonPoints) {
                        ButtonPoints buttonPoints = (ButtonPoints) o;
                        if (buttonPoints.getType() == ButtonPointsView.FB_SYNC) {
                            contactsGridPresenter.loginFacebook();
                        } else if (buttonPoints.getType() == ButtonPointsView.FB_NOTIFY) {
                            contactsGridPresenter.notifyFBFriends();
                        }
                    }
                }));

        subscriptions.add(contactsGridAdapter.onButtonPointsFBSyncDone()
                .delay(250, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(v -> {
                    contactsGridAdapter.setItems(contactList);
                }));

        subscriptions.add(contactsGridAdapter.onButtonPointsNotifyDone()
                .delay(250, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(v -> {
                    contactsGridAdapter.setItems(contactList);
                }));

        subscriptions.add(contactsGridAdapter.onClickAdd()
                .map(view -> contactsGridAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof SearchResult) {
                        SearchResult searchResult = (SearchResult) o;
                        if (!searchResult.getUsername().equals(currentUser.getUsername()))
                            contactsGridPresenter.createFriendship(searchResult.getId());
                    }
                }));

        subscriptions.add(contactsGridAdapter.onClickRemove()
                .map(view -> contactsGridAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof SearchResult) {
                        SearchResult searchResult = (SearchResult) o;
                        contactsGridPresenter.removeFriendship(searchResult.getFriendship().getId());
                    }
                }));

        loadData();
    }

    /**
     * Loads all contacts.
     */
    private void loadData() {
        this.contactsGridPresenter.loadContactList();
    }

    private void updateSearch() {
        this.contactsGridAdapter.updateSearch(searchResult, searchContactList);
    }

    private void showClearSearch() {
        if (btnCloseSearch.getTranslationX() > 0) {
            btnCloseSearch.clearAnimation();
            btnCloseSearch.animate().setDuration(DURATION).translationX(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
        }
    }

    private void hideClearSearch() {
        if (btnCloseSearch.getTranslationX() == 0) {
            btnCloseSearch.clearAnimation();
            btnCloseSearch.animate().setDuration(DURATION).translationX(screenUtils.getWidthPx() >> 1).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    public void renderContactList(List<Contact> contactList) {
        this.contactList = contactList;

        if (!isSearchMode)
            contactsGridAdapter.setItems(contactList);
    }

    @Override
    public void renderSearchContacts(List<Contact> contactList) {
        if (isSearchMode) {
            this.searchContactList.clear();
            this.searchContactList.addAll(contactList);
            updateSearch();
        }
    }

    @Override
    public void onAddSuccess(Friendship friendship) {
        subscriptions.add(
                Observable
                        .timer(1000, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            closeSearch();
                        }));
    }

    @Override
    public void onAddError() {
        updateSearch();
    }

    @Override
    public void renderSearchResult(SearchResult searchResult) {
        if (isSearchMode) {
            if (this.searchResult != null && this.searchResult.getFriendship() == null && searchResult.getFriendship() != null
                    && this.searchResult.getUsername().equals(searchResult.getUsername()) && this.searchResult.isSearchDone())
                searchResult.setShouldAnimateAdd(true);

            this.searchResult = searchResult;
            this.searchResult.setMyself(searchResult.getUsername().equals(currentUser.getUsername()));
            updateSearch();
        }
    }

    @Override
    public void successFacebookLogin() {
        contactsGridAdapter.startAnimateFB();
    }

    @Override
    public void notifySuccess() {
        tagManager.trackEvent(TagManagerConstants.USER_FACEBOOK_INVITE);
        contactsGridAdapter.startAnimateFB();
    }

    @Override
    public void errorFacebookLogin() {
        // TODO SHOW ERROR ?
    }
}
