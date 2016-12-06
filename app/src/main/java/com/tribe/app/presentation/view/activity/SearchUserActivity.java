package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.InputMethodManager;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/1/2016.
 */
public class SearchUserActivity extends BaseActivity implements SearchMVPView {

    private static final String USERNAME = "USERNAME";

    public static Intent getCallingIntent(Context context, String username) {
        Intent intent = new Intent(context, SearchUserActivity.class);
        if (!StringUtils.isEmpty(username)) intent.putExtra(USERNAME, username);
        return intent;
    }

    @Inject
    ScreenUtils screenUtils;

    @Inject
    SearchPresenter searchPresenter;

    @Inject
    ContactAdapter contactAdapter;

    @Inject
    User currentUser;

    @BindView(R.id.recyclerViewContacts)
    RecyclerView recyclerViewContacts;

    @BindView(R.id.editTextSearchContact)
    EditTextFont editTextSearchContact;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private boolean isSearchMode = false;
    private Unbinder unbinder;
    private ContactsLayoutManager layoutManager;
    private List<Contact> searchContactList;
    private SearchResult searchResult;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        initUI();
        initPresenter();
        initRecyclerView();
        initParams(getIntent());
    }

    @Override
    protected void onStop() {
        searchPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        recyclerViewContacts.setAdapter(null);
        subscriptions.unsubscribe();

        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    private void initUI() {
        setContentView(R.layout.activity_search);
        unbinder = ButterKnife.bind(this);

        searchContactList = new ArrayList<>();

        subscriptions.add(RxTextView.textChanges(editTextSearchContact).map(CharSequence::toString)
                .doOnNext(s -> {
                    if (StringUtils.isEmpty(s)) {
                        isSearchMode = false;
                    }
                })
                .filter(s -> !StringUtils.isEmpty(s))
                .doOnNext(s -> {
                    isSearchMode = true;
                    searchResult = new SearchResult();
                    searchResult.setUsername(s);
                    updateSearch();
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(s -> searchPresenter.findByUsername(s)));
    }

    private void initPresenter() {
        searchPresenter.onViewAttached(this);
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initRecyclerView() {
        this.layoutManager = new ContactsLayoutManager(context());
        this.recyclerViewContacts.setLayoutManager(layoutManager);
        this.recyclerViewContacts.setItemAnimator(null);
        this.recyclerViewContacts.setAdapter(contactAdapter);

        subscriptions.add(contactAdapter.onClickAdd()
                .map(view -> contactAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof SearchResult) {
                        SearchResult searchResult = (SearchResult) o;
                        if (searchResult.getUsername() != null && !searchResult.getUsername().equals(currentUser.getUsername()))
                            searchPresenter.createFriendship(searchResult.getId());
                    }
                }));
    }

    private void initParams(Intent intent) {
        if (intent != null && intent.hasExtra(USERNAME)) {
            username = intent.getStringExtra(USERNAME);
            editTextSearchContact.setText(username);
        }
    }

    private void updateSearch() {
        this.contactAdapter.updateSearch(searchResult, searchContactList);
    }

    @OnClick(R.id.imgBack)
    void clickBack() {
        onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }

    @Override
    public void onAddSuccess(Friendship friendship) {

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
            this.searchResult.setMyself(searchResult.getUsername() != null && searchResult.getUsername().equals(currentUser.getUsername()));
            updateSearch();
        }
    }

    public void search(String username) {
        editTextSearchContact.postDelayed(() -> {
            editTextSearchContact.requestFocus();
            InputMethodManager imm = (InputMethodManager) context().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextSearchContact, InputMethodManager.SHOW_IMPLICIT);
            editTextSearchContact.setText(username);
        }, 750);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }
}
