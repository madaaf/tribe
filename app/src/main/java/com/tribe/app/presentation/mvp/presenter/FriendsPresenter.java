package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.CreateFriendships;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.presentation.mvp.view.FriendsMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;


public class FriendsPresenter implements Presenter {

    private final RxFacebook rxFacebook;
    private final GetDiskContactOnAppList diskContactOnAppList;
    private final UseCase synchroContactList;
    private final CreateFriendships createFriendships;

    private ContactListSubscriber contactListSubscriber;
    private LookupContactsSubscriber lookupContactsSubscriber;

    private FriendsMVPView friendsMVPView;

    @Inject
    public FriendsPresenter(GetDiskContactOnAppList getDiskContactOnAppList,
                            RxFacebook rxFacebook,
                            @Named("synchroContactList") UseCase synchroContactList,
                            CreateFriendships createFriendships) {
        this.rxFacebook = rxFacebook;
        this.diskContactOnAppList = getDiskContactOnAppList;
        this.synchroContactList = synchroContactList;
        this.createFriendships = createFriendships;
    }

    @Override
    public void onViewDetached() {
        diskContactOnAppList.unsubscribe();
        if (contactListSubscriber != null) contactListSubscriber.unsubscribe();
        synchroContactList.unsubscribe();
        createFriendships.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        friendsMVPView = (FriendsMVPView) v;
    }

    public void loadContacts() {
        if (contactListSubscriber != null) {
            contactListSubscriber.unsubscribe();
        }

        contactListSubscriber = new ContactListSubscriber();
        diskContactOnAppList.execute(contactListSubscriber);
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
            if (contactList != null && contactList.size() > 0) {
                Map<String, User> userMap = new HashMap<>();

                for (Contact contact : contactList) {
                    User user = contact.getUserList().get(0);
                    userMap.put(user.getId(), user);
                }

                friendsMVPView.renderContactList(new ArrayList<>(userMap.values()));
            }
        }
    }

    public void loginFacebook() {
        if (!FacebookUtils.isLoggedIn()) {
            rxFacebook.requestLogin().subscribe(loginResult -> {
                if (FacebookUtils.isLoggedIn()) {
                    friendsMVPView.successFacebookLogin();
                } else {
                    friendsMVPView.errorFacebookLogin();
                }
            });
        } else {
            friendsMVPView.successFacebookLogin();
        }
    }

    public void lookupContacts() {
        if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
        lookupContactsSubscriber = new LookupContactsSubscriber();
        synchroContactList.execute(lookupContactsSubscriber);
    }

    private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() { }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            friendsMVPView.syncDone();
        }

        @Override
        public void onNext(List<Contact> contactList) {
            friendsMVPView.syncDone();
        }
    }

    public void createFriendships(List<User> userList) {
        if (userList != null && userList.size() > 0) {
            friendsMVPView.showLoading();

            Set<String> userIds = new HashSet<>();

            for (User user : userList) {
                if (!user.isInvisibleMode()) userIds.add(user.getId());
            }

            createFriendships.setUserIds(userIds.toArray(new String[userIds.size()]));
            createFriendships.execute(new CreateFriendshipsSubscriber());
        }
    }

    private final class CreateFriendshipsSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            friendsMVPView.hideLoading();
            e.printStackTrace();
            friendsMVPView.errorCreateFriendships();
        }

        @Override
        public void onNext(Void aVoid) {
            friendsMVPView.hideLoading();
            friendsMVPView.successCreateFriendships();
        }
    }
}
