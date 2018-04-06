package com.tribe.app.presentation.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.GetDiskContactInviteList;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskFBContactInviteList;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.SearchLocally;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserAge;
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
  private SynchroContactList synchroContactList;
  private GetDiskContactOnAppList getDiskContactOnAppList;
  private GetDiskContactInviteList getDiskContactInviteList;
  private GetDiskFBContactInviteList getDiskFBContactInviteList;

  // SUBSCRIBERS
  private DefaultSubscriber findByUsernameSubscriber;
  private ContactListSubscriber contactListSubscriber;
  private ContactListOnAppSubscriber contactListOnAppSubscriber;
  private ContactListInviteSubscriber contactListInviteSubscriber;
  private FBContactListInviteSubscriber fbContactListInviteSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;

  @Inject public SearchPresenter(ShortcutPresenter shortcutPresenter, JobManager jobManager,
      @Named("cloudFindByUsername") FindByUsername findByUsername,
      @Named("diskSearchResults") DiskSearchResults diskSearchResults, SearchLocally searchLocally,
       SynchroContactList synchroContactList, RxFacebook rxFacebook,
      UpdateUser updateUser, UpdateUserPhoneNumber updateUserPhoneNumber,
      UpdateUserFacebook updateUserFacebook, LookupUsername lookupUsername,
      GetDiskContactOnAppList getDiskContactOnAppList,
      GetDiskContactInviteList getDiskContactInviteList,
      GetDiskFBContactInviteList getDiskFBContactInviteList, UpdateUserAge updateUserAge) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber,
        updateUserAge);
    this.shortcutPresenter = shortcutPresenter;
    this.jobManager = jobManager;
    this.findByUsername = findByUsername;
    this.searchResults = diskSearchResults;
    this.searchLocally = searchLocally;
    this.synchroContactList = synchroContactList;
    this.getDiskContactOnAppList = getDiskContactOnAppList;
    this.getDiskContactInviteList = getDiskContactInviteList;
    this.getDiskFBContactInviteList = getDiskFBContactInviteList;
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
    getDiskContactOnAppList.unsubscribe();
    getDiskContactInviteList.unsubscribe();
    getDiskFBContactInviteList.unsubscribe();
    searchView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    searchView = (SearchMVPView) v;
    shortcutPresenter.onViewAttached(v);
    initSearchResult();
    searchLocally("");
    contactsInApp();
    contactsInvite();
    contactsFBInvite();
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

  public void searchLocally(String s) {
    if (contactListSubscriber != null) {
      contactListSubscriber.unsubscribe();
    }

    contactListSubscriber = new ContactListSubscriber();
    searchLocally.setup(s, addedUserIds);
    searchLocally.execute(contactListSubscriber);
  }

  private final class ContactListSubscriber extends DefaultSubscriber<List<Shortcut>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Shortcut> contactList) {
      searchView.renderContactList(contactList);
    }
  }

  public void lookupContacts(Activity c) {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.setParams(c);
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

  public void contactsInApp() {
    if (contactListOnAppSubscriber != null) {
      contactListOnAppSubscriber.unsubscribe();
    }

    contactListOnAppSubscriber = new ContactListOnAppSubscriber();
    getDiskContactOnAppList.execute(contactListOnAppSubscriber);
  }

  private final class ContactListOnAppSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      searchView.renderContactListOnApp(contactList);
    }
  }

  public void contactsInvite() {
    if (contactListInviteSubscriber != null) {
      contactListInviteSubscriber.unsubscribe();
    }

    contactListInviteSubscriber = new ContactListInviteSubscriber();
    getDiskContactInviteList.execute(contactListInviteSubscriber);
  }

  private final class ContactListInviteSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      searchView.renderContactListInvite(contactList);
    }
  }

  public void contactsFBInvite() {
    if (fbContactListInviteSubscriber != null) {
      fbContactListInviteSubscriber.unsubscribe();
    }

    fbContactListInviteSubscriber = new FBContactListInviteSubscriber();
    getDiskFBContactInviteList.execute(fbContactListInviteSubscriber);
  }

  private final class FBContactListInviteSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      searchView.renderContactListInviteFB(contactList);
    }
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status, null);
  }

  public void removeShortcut(String shortcutId) {
    shortcutPresenter.removeShortcut(shortcutId);
  }
}
