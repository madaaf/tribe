package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.adapter.ContactAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.ContactsLayoutManager;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
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

    @Inject
    @AddressBook
    Preference<Boolean> addressBook;

    @BindView(R.id.recyclerViewContacts)
    RecyclerView recyclerViewContacts;

    @BindView(R.id.editTextSearchContact)
    EditTextFont editTextSearchContact;

    @BindView(R.id.layoutFocus)
    ViewGroup layoutFocus;

    @BindView(R.id.layoutContent)
    ViewGroup layoutContent;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    @BindView(R.id.layoutTop)
    ViewGroup layoutTop;

    LoadFriendsView viewFriendsFBLoad;
    LoadFriendsView viewFriendsAddressBookLoad;
    View viewSeparatorAddressBook;
    View viewSeparatorFBTop;
    View viewSeparatorFBBottom;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private boolean isSearchMode = false;
    private Unbinder unbinder;
    private ContactsLayoutManager layoutManager;
    private List<Object> filteredContactList;
    private List<Object> originalContactList;
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

        filteredContactList = new ArrayList<>();
        originalContactList = new ArrayList<>();

        refactorActions();

        subscriptions.add(RxTextView.textChanges(editTextSearchContact).map(CharSequence::toString)
                .doOnNext(s -> {
                    if (StringUtils.isEmpty(s)) {
                        isSearchMode = false;
                        filter();
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
        filter();
        this.contactAdapter.updateSearch(searchResult, filteredContactList);
    }

    private void refactorActions() {
        boolean permissionsFB = FacebookUtils.isLoggedIn();
        boolean permissionsContact = PermissionUtils.hasPermissionsContact(this) && addressBook.get();

        layoutBottom.removeAllViews();
        layoutTop.removeAllViews();

        if (permissionsContact && permissionsFB) {
            recyclerViewContacts.setPadding(0, 0, 0, 0);
            searchPresenter.loadContacts();
            return;
        }

        if (!permissionsContact && !permissionsFB) {
            layoutContent.setVisibility(View.GONE);
            layoutTop.setVisibility(View.VISIBLE);
            initLoadView(getLayoutInflater().inflate(R.layout.view_load_ab_fb_friends, layoutTop));
        } else if (!permissionsContact || !permissionsFB) {
            layoutContent.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            recyclerViewContacts.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.load_friends_height));
            initLoadView(getLayoutInflater().inflate(R.layout.view_load_ab_fb_friends, layoutBottom));
        }

        if (permissionsContact || permissionsFB) {
            searchPresenter.loadContacts();
        }

        if (!permissionsContact) {
            viewFriendsAddressBookLoad.setOnClickListener(v -> {
                lookupContacts();
                viewFriendsAddressBookLoad.showLoading();
            });
        } else if (!permissionsFB) {
            viewFriendsAddressBookLoad.setVisibility(View.GONE);
            viewSeparatorAddressBook.setVisibility(View.GONE);
        }

        if (!permissionsFB) {
            if (permissionsContact) viewSeparatorFBBottom.setVisibility(View.GONE);
            viewFriendsFBLoad.setOnClickListener(v -> {
                searchPresenter.loginFacebook();
                viewFriendsFBLoad.showLoading();
            });
        } else if (!permissionsContact) {
            viewFriendsFBLoad.setVisibility(View.GONE);
            viewSeparatorFBTop.setVisibility(View.GONE);
            viewSeparatorFBBottom.setVisibility(View.GONE);
        }
    }

    private void initLoadView(View v) {
        viewFriendsFBLoad = ButterKnife.findById(v, R.id.viewFriendsFBLoad);
        viewFriendsAddressBookLoad = ButterKnife.findById(v, R.id.viewFriendsAddressBookLoad);
        viewSeparatorAddressBook = ButterKnife.findById(v, R.id.viewSeparatorAddressBook);
        viewSeparatorFBTop = ButterKnife.findById(v, R.id.viewSeparatorFBTop);
        viewSeparatorFBBottom = ButterKnife.findById(v, R.id.viewSeparatorFBBottom);
    }

    private void lookupContacts() {
        RxPermissions.getInstance(this)
                .request(PermissionUtils.PERMISSIONS_CONTACTS)
                .subscribe(hasPermission -> {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED, hasPermission);
                    tagManager.setProperty(bundle);

                    if (hasPermission) {
                        addressBook.set(true);
                        sync();
                    } else {
                        viewFriendsAddressBookLoad.hideLoading();
                    }
                });
    }

    private void sync() {
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_CONTACTS_SYNC);
        searchPresenter.lookupContacts();
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
        contactAdapter.updateAdd(friendship.getFriend());
    }

    @Override
    public void onAddError() {
        updateSearch();
    }

    @Override
    public void successFacebookLogin() {
        sync();
    }

    @Override
    public void errorFacebookLogin() {
        viewFriendsFBLoad.hideLoading();
    }

    @Override
    public void syncDone() {
        refactorActions();
        viewFriendsFBLoad.hideLoading();
        viewFriendsAddressBookLoad.hideLoading();
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
        this.originalContactList.clear();
        this.originalContactList.addAll(contactList);
        refactorContacts(contactList);
        showContactList();
    }

    private void showContactList() {
        if (!isSearchMode)
            contactAdapter.setItems(this.filteredContactList);
    }

    private void filter() {
        refactorContacts(originalContactList);
    }

    private void refactorContacts(List<Object> contactList) {
        this.filteredContactList.clear();

        int count = 0;
        boolean headerOnAppDone = false;
        boolean headerInviteDone = false;


        Contact contact = null;

        for (Object obj : contactList) {
            contact = (Contact) obj;

            if (!isSearchMode || (isSearchMode && contact.getName().toLowerCase().startsWith(searchResult.getUsername().toString().toLowerCase()))) {
                if (count == 0 && (contact.getUserList() != null && contact.getUserList().size() > 0) && !headerOnAppDone) {
                    this.filteredContactList.add(R.string.search_suggest_friends);

                    User user = contact.getUserList().get(0);

                    for (Friendship friendship : currentUser.getFriendships()) {
                        if (friendship.getFriend().equals(user)) {
                            user.setAnimateAdd(true);
                            user.setFriend(true);
                        }
                    }

                    headerOnAppDone = true;
                } else if ((contact.getUserList() == null || contact.getUserList().size() == 0) && !headerInviteDone) {
                    this.filteredContactList.add(new String());
                    this.filteredContactList.add(R.string.search_invite_contacts);
                    headerInviteDone = true;
                }

                this.filteredContactList.add(contact);
            }
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
