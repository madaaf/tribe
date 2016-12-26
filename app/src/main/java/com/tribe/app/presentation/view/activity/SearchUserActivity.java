package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
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

    @BindView(R.id.layoutFocus)
    ViewGroup layoutFocus;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private boolean isSearchMode = false;
    private Unbinder unbinder;
    private ContactsLayoutManager layoutManager;
    private List<Object> contactList;
    private SearchResult searchResult;
    private String username;
    private boolean shouldOverridePendingTransactions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        initUI();
        initRecyclerView();
        initParams(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldOverridePendingTransactions) {
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
            shouldOverridePendingTransactions = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchPresenter.onViewAttached(this);
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

        contactList = new ArrayList<>();

        subscriptions.add(RxTextView.textChanges(editTextSearchContact).map(CharSequence::toString)
                .doOnNext(s -> {
                    if (StringUtils.isEmpty(s)) {
                        isSearchMode = false;
                        showContactList();
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
                .subscribe(s -> searchPresenter.findByUsername(s))
        );
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
        this.recyclerViewContacts.addItemDecoration(new DividerHeadersItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10)));
        this.recyclerViewContacts.setAdapter(contactAdapter);

        contactAdapter.setItems(new ArrayList<>());

        subscriptions.add(contactAdapter.onClickAdd()
                .map(view -> contactAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof SearchResult) {
                        SearchResult searchResult = (SearchResult) o;
                        if (searchResult.getUsername() != null && !searchResult.getUsername().equals(currentUser.getUsername()))
                            searchPresenter.createFriendship(searchResult.getId());
                    } else {
                        Contact contact = (Contact) o;
                        searchPresenter.createFriendship(contact.getUserList().get(0).getId());
                    }
                }));

        subscriptions.add(contactAdapter.onClickInvite()
                .map(view -> contactAdapter.getItemAtPosition(recyclerViewContacts.getChildLayoutPosition(view)))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(o -> {
                    if (o instanceof ContactAB) {
                        ContactAB contact = (ContactAB) o;
                        shouldOverridePendingTransactions = true;
                        navigator.invite(contact.getPhone(), contact.getHowManyFriends(), this);
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
        this.contactAdapter.updateSearch(searchResult, contactList);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (editTextSearchContact.hasFocus()) {
                Rect outRect = new Rect();
                editTextSearchContact.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    editTextSearchContact.clearFocus();
                    screenUtils.hideKeyboard(editTextSearchContact);
                    layoutFocus.requestFocus();
                }
            }
        }

        return super.dispatchTouchEvent(event);
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
        //currentUser.getFriendships().add(friendship);
        contactAdapter.updateAdd(friendship.getFriend());
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

    @Override
    public void renderContactList(List<Object> contactList) {
        refactorContacts(contactList);
        showContactList();
    }

    private void showContactList() {
        if (!isSearchMode)
            contactAdapter.setItems(this.contactList);
    }

    private void refactorContacts(List<Object> contactList) {
        this.contactList.clear();

        int count = 0;
        boolean headerOnAppDone = false;
        boolean headerInviteDone = false;

        for (Object obj : contactList) {
            Contact contact = (Contact) obj;
            if (count == 0 && (contact.getUserList() != null && contact.getUserList().size() > 0) && !headerOnAppDone) {
                this.contactList.add(R.string.search_suggest_friends);

                User user = contact.getUserList().get(0);

                for (Friendship friendship : currentUser.getFriendships()) {
                    if (friendship.getFriend().equals(user)) {
                        user.setAnimateAdd(true);
                        user.setFriend(true);
                    }
                }

                headerOnAppDone = true;
            } else if ((contact.getUserList() == null || contact.getUserList().size() == 0) && !headerInviteDone) {
                this.contactList.add(new String());
                this.contactList.add(R.string.search_invite_contacts);
                headerInviteDone = true;
            }

            this.contactList.add(contact);
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
