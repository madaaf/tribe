package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.DiskFindContactByValue;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.RemoveFriendship;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class ContactsGridPresenter implements Presenter {

    // VIEW ATTACHED
    private ContactsView contactsView;

    // USECASES
    private JobManager jobManager;
    private RxFacebook rxFacebook;
    private UseCaseDisk getDiskContactList;
    private FindByUsername findByUsername;
    private DiskSearchResults searchResults;
    private DiskFindContactByValue findContactByValue;
    private RemoveFriendship removeFriendship;
    private CreateFriendship createFriendship;
    private UseCase notifyFBFriends;

    // SUBSCRIBERS
    private FindByValueSubscriber findByValueSubscriber;
    private CreateFriendshipSubscriber createFriendshipSubscriber;
    private RemoveFriendshipSubscriber removeFriendshipSubscriber;
    private DefaultSubscriber findByUsernameSubscriber;
    private NotifyFBFriendsSubscriber notifyFBFriendsSubscriber;

    @Inject
    public ContactsGridPresenter(JobManager jobManager,
                                 RxFacebook rxFacebook,
                                 @Named("diskContactList") UseCaseDisk getDiskContactList,
                                 @Named("cloudFindByUsername") FindByUsername findByUsername,
                                 @Named("diskSearchResults") DiskSearchResults diskSearchResults,
                                 @Named("diskFindContactByValue") DiskFindContactByValue diskFindContactByValue,
                                 @Named("createFriendship") CreateFriendship createFriendship,
                                 @Named("removeFriendship") RemoveFriendship removeFriendship,
                                 @Named("notifyFBFriends") UseCase notifyFBFriends) {
        super();
        this.jobManager = jobManager;
        this.rxFacebook = rxFacebook;
        this.getDiskContactList = getDiskContactList;
        this.findByUsername = findByUsername;
        this.searchResults = diskSearchResults;
        this.findContactByValue = diskFindContactByValue;
        this.createFriendship = createFriendship;
        this.removeFriendship = removeFriendship;
        this.notifyFBFriends = notifyFBFriends;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        findByUsername.unsubscribe();
        findContactByValue.unsubscribe();
        searchResults.unsubscribe();
        getDiskContactList.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        contactsView = (ContactsView) v;
    }

    public void findByValue(String value) {
        if (findByValueSubscriber != null)
            findByValueSubscriber.unsubscribe();

        findByValueSubscriber = new FindByValueSubscriber();
        findContactByValue.setValue(value);
        findContactByValue.execute(findByValueSubscriber);
    }

    public void findByUsername(String username) {
        if (findByUsernameSubscriber != null)
            findByUsernameSubscriber.unsubscribe();

        findByUsernameSubscriber = new DefaultSubscriber();
        findByUsername.setUsername(username);
        findByUsername.execute(findByUsernameSubscriber);
    }

    public void loadContactList() {
        getDiskContactList.execute(new ContactListSubscriber());
        searchResults.execute(new SearchResultSubscriber());
    }

    public void createFriendship(String userId) {
        if (createFriendshipSubscriber != null) createFriendshipSubscriber.unsubscribe();

        createFriendshipSubscriber = new CreateFriendshipSubscriber();
        createFriendship.setUserId(userId);
        createFriendship.execute(createFriendshipSubscriber);
    }

    public void removeFriendship(String friendshipId) {
        if (removeFriendshipSubscriber != null) removeFriendshipSubscriber.unsubscribe();

        removeFriendshipSubscriber = new RemoveFriendshipSubscriber();
        removeFriendship.setFriendshipId(friendshipId);
        removeFriendship.execute(removeFriendshipSubscriber);
    }

    public void loginFacebook() {
        if (!FacebookUtils.isLoggedIn()) {
            rxFacebook.requestLogin().subscribe(loginResult -> {
                if (FacebookUtils.isLoggedIn()) {
                    contactsView.successFacebookLogin();
                } else {
                    contactsView.errorFacebookLogin();
                }
            });
        } else {
            contactsView.successFacebookLogin();
        }
    }

    public void notifyFBFriends() {
        if (notifyFBFriendsSubscriber != null) notifyFBFriendsSubscriber.unsubscribe();

        notifyFBFriendsSubscriber = new NotifyFBFriendsSubscriber();
        notifyFBFriends.execute(notifyFBFriendsSubscriber);
    }

    private final class ContactListSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<Contact> contactList) {
            contactsView.renderContactList(contactList);
        }
    }

    private final class FindByValueSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<Contact> contactList) {
            contactsView.renderSearchContacts(contactList);
        }
    }

    private final class SearchResultSubscriber extends DefaultSubscriber<SearchResult> {

        @Override
        public void onCompleted() {
            System.out.println("COMPLETED");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(SearchResult searchResult) {
            contactsView.renderSearchResult(searchResult);
        }
    }

    private final class CreateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {
            System.out.println("COMPLETED");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Friendship friendship) {
            if (friendship == null) contactsView.onAddError();
            else contactsView.onAddSuccess(friendship);
        }
    }

    private final class RemoveFriendshipSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
            System.out.println("COMPLETED");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            System.out.println("Friendship deleted");
        }
    }

    private final class NotifyFBFriendsSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            contactsView.notifySuccess();
        }
    }
}
