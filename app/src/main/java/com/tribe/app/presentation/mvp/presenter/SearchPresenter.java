package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.GetDiskContactInviteList;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.SearchMVPView;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class SearchPresenter implements Presenter {

    // VIEW ATTACHED
    private SearchMVPView searchView;

    // USECASES
    private JobManager jobManager;
    private FindByUsername findByUsername;
    private DiskSearchResults searchResults;
    private CreateFriendship createFriendship;
    private GetDiskContactInviteList diskContactInviteList;

    // SUBSCRIBERS
    private CreateFriendshipSubscriber createFriendshipSubscriber;
    private DefaultSubscriber findByUsernameSubscriber;
    private ContactListSubscriber contactListSubscriber;

    @Inject
    public SearchPresenter(JobManager jobManager,
                           @Named("cloudFindByUsername") FindByUsername findByUsername,
                           @Named("diskSearchResults") DiskSearchResults diskSearchResults,
                           CreateFriendship createFriendship,
                           GetDiskContactInviteList diskContactInviteList) {
        super();
        this.jobManager = jobManager;
        this.findByUsername = findByUsername;
        this.searchResults = diskSearchResults;
        this.createFriendship = createFriendship;
        this.diskContactInviteList = diskContactInviteList;
    }

    @Override
    public void onViewDetached() {
        findByUsername.unsubscribe();
        searchResults.unsubscribe();
        createFriendship.unsubscribe();
        diskContactInviteList.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        searchView = (SearchMVPView) v;
        initSearchResult();
        loadContacts();
    }

    public void findByUsername(String username) {
        if (findByUsernameSubscriber != null)
            findByUsernameSubscriber.unsubscribe();

        findByUsernameSubscriber = new DefaultSubscriber();
        findByUsername.setUsername(username);
        findByUsername.execute(findByUsernameSubscriber);
    }

    public void initSearchResult() {
        searchResults.execute(new SearchResultSubscriber());
    }

    public void createFriendship(String userId) {
        if (createFriendshipSubscriber != null) createFriendshipSubscriber.unsubscribe();

        createFriendshipSubscriber = new CreateFriendshipSubscriber();
        createFriendship.setUserId(userId);
        createFriendship.execute(createFriendshipSubscriber);
    }

    private final class SearchResultSubscriber extends DefaultSubscriber<SearchResult> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(SearchResult searchResult) {
            searchView.renderSearchResult(searchResult);
        }
    }

    private final class CreateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Friendship friendship) {
            if (friendship == null) {
                searchView.onAddError();
            } else {
                jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.NEW_FRIENDSHIP, 1));
                searchView.onAddSuccess(friendship);
            }
        }
    }

    public void loadContacts() {
        if (contactListSubscriber != null) {
            contactListSubscriber.unsubscribe();
        }

        contactListSubscriber = new ContactListSubscriber();
        diskContactInviteList.execute(contactListSubscriber);
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
                searchView.renderContactList(new ArrayList<>(contactList));
            }

            diskContactInviteList.unsubscribe();
            contactListSubscriber.unsubscribe();
        }
    }
}
