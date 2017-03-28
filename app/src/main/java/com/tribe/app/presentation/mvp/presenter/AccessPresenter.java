package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RefreshHowManyFriendsJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.GetGroupInfos;
import com.tribe.app.presentation.mvp.view.AccessMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import rx.subscriptions.CompositeSubscription;

public class AccessPresenter implements Presenter {

  // VIEW ATTACHED
  private AccessMVPView accessView;

  // USECASES
  private JobManager jobManager;
  private UseCase synchroContactList;
  private GetGroupInfos getGroupInfos;

  // SUBSCRIBERS
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private LookupContactsSubscriber lookupContactsSubscriber;

  @Inject public AccessPresenter(JobManager jobManager,
      @Named("synchroContactList") UseCase synchroContactList, GetGroupInfos getGroupInfos) {
    this.jobManager = jobManager;
    this.synchroContactList = synchroContactList;
    this.getGroupInfos = getGroupInfos;
  }

  @Override public void onViewDetached() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    accessView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    accessView = (AccessMVPView) v;
  }

  public void cancelLookupContacts() {
    synchroContactList.unsubscribe();
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
  }

  public void lookupGroupInfos(String groupId) {
    getGroupInfos.prepare(groupId);
    getGroupInfos.execute(new GetGroupInfosSubscriber());
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
      accessView.renderFriendList(new ArrayList<>());
    }

    @Override public void onNext(List<Contact> contactList) {
      Map<String, User> friendList = new HashMap<>();

      for (Contact contact : contactList) {
        if (contact.getUserList() != null && contact.getUserList().size() > 0) {
          for (User user : contact.getUserList()) {
            if (!friendList.containsKey(user)) {
              friendList.put(user.getId(), user);
            }
          }
        }
      }

      accessView.renderFriendList(new ArrayList<>(friendList.values()));
      jobManager.addJobInBackground(new RefreshHowManyFriendsJob());
    }
  }

  private final class GetGroupInfosSubscriber extends DefaultSubscriber<Group> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      accessView.groupInfosFailed();
      e.printStackTrace();
    }

    @Override public void onNext(Group group) {
      if (group != null) accessView.groupInfosSuccess(group);
    }
  }
}
