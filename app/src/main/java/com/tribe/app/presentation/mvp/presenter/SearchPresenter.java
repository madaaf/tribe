package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.SearchLocally;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

public class SearchPresenter extends UpdateUserPresenter {

  // COMPOSITE PRESENTER
  private ShortcutPresenter shortcutPresenter;

  // VIEW ATTACHED
  private SearchMVPView searchView;

  private Set<String> addedUserIds = new HashSet<>();

  // USECASES
  private JobManager jobManager;
  private FindByUsername findByUsername;
  private DiskSearchResults searchResults;
  private SearchLocally searchLocally;
  private UseCase synchroContactList;

  // SUBSCRIBERS
  private DefaultSubscriber findByUsernameSubscriber;
  private ContactListSubscriber contactListSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;

  @Inject public SearchPresenter(ShortcutPresenter shortcutPresenter, JobManager jobManager,
      @Named("cloudFindByUsername") FindByUsername findByUsername,
      @Named("diskSearchResults") DiskSearchResults diskSearchResults, SearchLocally searchLocally,
      @Named("synchroContactList") UseCase synchroContactList, RxFacebook rxFacebook,
      UpdateUser updateUser, UpdateUserPhoneNumber updateUserPhoneNumber,
      UpdateUserFacebook updateUserFacebook, LookupUsername lookupUsername) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber);
    this.shortcutPresenter = shortcutPresenter;
    this.jobManager = jobManager;
    this.findByUsername = findByUsername;
    this.searchResults = diskSearchResults;
    this.searchLocally = searchLocally;
    this.synchroContactList = synchroContactList;
  }

  @Override protected UpdateUserMVPView getUpdateUserView() {
    return searchView;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    findByUsername.unsubscribe();
    searchResults.unsubscribe();
    searchLocally.unsubscribe();
    synchroContactList.unsubscribe();
    searchView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    searchView = (SearchMVPView) v;
    shortcutPresenter.onViewAttached(v);
    initSearchResult();
    loadContacts("");
  }

  public void findByUsername(String username) {
    if (findByUsernameSubscriber != null) findByUsernameSubscriber.unsubscribe();

    findByUsernameSubscriber = new DefaultSubscriber();
    findByUsername.setUsername(username);
    findByUsername.execute(findByUsernameSubscriber);
  }

  public void initSearchResult() {
    searchResults.execute(new SearchResultSubscriber());
  }

  private final class SearchResultSubscriber extends DefaultSubscriber<SearchResult> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(SearchResult searchResult) {
      searchView.renderSearchResult(searchResult);
    }
  }

  public void loadContacts(String s) {
    if (contactListSubscriber != null) {
      contactListSubscriber.unsubscribe();
    }

    contactListSubscriber = new ContactListSubscriber();
    searchLocally.setup(s, addedUserIds);
    searchLocally.execute(contactListSubscriber);
  }

  private final class ContactListSubscriber extends DefaultSubscriber<List<Object>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Object> contactList) {
      if (contactList != null && contactList.size() > 0) {
        searchView.renderContactList(contactList);
      }
    }
  }

  public void lookupContacts() {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.execute(lookupContactsSubscriber);
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      searchView.syncDone();
    }

    @Override public void onNext(List<Contact> contactList) {
      searchView.syncDone();
    }
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }
}
