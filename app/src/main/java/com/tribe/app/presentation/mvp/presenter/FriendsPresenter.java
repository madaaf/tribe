package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.presentation.mvp.view.FriendsMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


public class FriendsPresenter implements Presenter {

    private final RxFacebook rxFacebook;
    private final GetDiskContactOnAppList diskContactOnAppList;

    private ContactListSubscriber contactListSubscriber;

    private FriendsMVPView friendsMVPView;

    @Inject
    public FriendsPresenter(GetDiskContactOnAppList getDiskContactOnAppList,
                            RxFacebook rxFacebook) {
        this.rxFacebook = rxFacebook;
        this.diskContactOnAppList = getDiskContactOnAppList;
    }

    @Override
    public void onViewDetached() {
        diskContactOnAppList.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        friendsMVPView = (FriendsMVPView) v;
        loadContacts();
    }

    private void loadContacts() {
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
}
