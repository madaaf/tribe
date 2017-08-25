package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.BookRoomLink;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.CreateMembership;
import com.tribe.app.domain.interactor.user.DeclineInvite;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetHeadDeepLink;
import com.tribe.app.domain.interactor.user.LeaveGroup;
import com.tribe.app.domain.interactor.user.RemoveGroup;
import com.tribe.app.domain.interactor.user.SendInvitations;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.domain.interactor.user.UpdateFriendship;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends FriendshipPresenter implements Presenter {

  // VIEW ATTACHED
  private HomeGridMVPView homeGridView;

  // USECASES
  private JobManager jobManager;
  private GetDiskUserInfos diskUserInfosUsecase;
  private LeaveGroup leaveGroup;
  private RemoveGroup removeGroup;
  private SendToken sendTokenUseCase;
  private GetHeadDeepLink getHeadDeepLink;
  private CreateMembership createMembership;
  private UseCase cloudUserInfos;
  private UpdateUserFacebook updateUserFacebook;
  private RxFacebook rxFacebook;
  private UseCase synchroContactList;
  private GetDiskContactOnAppList getDiskContactOnAppList;
  private DeclineInvite declineInvite;
  private SendInvitations sendInvitations;
  private CreateFriendship createFriendship;
  private BookRoomLink bookRoomLink;

  // SUBSCRIBERS
  private FriendListSubscriber diskFriendListSubscriber;
  private FriendListSubscriber cloudFriendListSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;
  private ContactsOnAppSubscriber contactsOnAppSubscriber;

  @Inject public HomeGridPresenter(JobManager jobManager,
      @Named("diskUserInfos") GetDiskUserInfos diskUserInfos, LeaveGroup leaveGroup,
      RemoveGroup removeGroup, @Named("sendToken") SendToken sendToken,
      GetHeadDeepLink getHeadDeepLink, CreateMembership createMembership,
      @Named("cloudUserInfos") UseCase cloudUserInfos, UpdateUserFacebook updateUserFacebook, RxFacebook rxFacebook,
      @Named("synchroContactList") UseCase synchroContactList,
      GetDiskContactOnAppList getDiskContactOnAppList, DeclineInvite declineInvite,
      SendInvitations sendInvitations, CreateFriendship createFriendship, BookRoomLink bookRoomLink,
      UpdateFriendship updateFriendship) {
    this.updateFriendship = updateFriendship;
    this.jobManager = jobManager;
    this.diskUserInfosUsecase = diskUserInfos;
    this.leaveGroup = leaveGroup;
    this.removeGroup = removeGroup;
    this.sendTokenUseCase = sendToken;
    this.getHeadDeepLink = getHeadDeepLink;
    this.createMembership = createMembership;
    this.cloudUserInfos = cloudUserInfos;
    this.updateUserFacebook = updateUserFacebook;
    this.rxFacebook = rxFacebook;
    this.synchroContactList = synchroContactList;
    this.getDiskContactOnAppList = getDiskContactOnAppList;
    this.declineInvite = declineInvite;
    this.sendInvitations = sendInvitations;
    this.createFriendship = createFriendship;
    this.bookRoomLink = bookRoomLink;
  }

  @Override public void onViewDetached() {
    super.onViewDetached();
    leaveGroup.unsubscribe();
    removeGroup.unsubscribe();
    getHeadDeepLink.unsubscribe();
    createMembership.unsubscribe();
    cloudUserInfos.unsubscribe();
    updateUserFacebook.unsubscribe();
    diskUserInfosUsecase.unsubscribe();
    synchroContactList.unsubscribe();
    getDiskContactOnAppList.unsubscribe();
    declineInvite.unsubscribe();
    sendInvitations.unsubscribe();
    createFriendship.unsubscribe();
    bookRoomLink.unsubscribe();
    homeGridView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    homeGridView = (HomeGridMVPView) v;
  }

  public void reload(boolean sync) {
    showViewLoading();
    loadFriendList();
    loadContactsOnApp();
    if (!sync) syncFriendList();
  }

  public void loadFriendList() {
    if (diskFriendListSubscriber != null) {
      diskFriendListSubscriber.unsubscribe();
    }

    diskFriendListSubscriber = new FriendListSubscriber(false);
    diskUserInfosUsecase.prepare(null);
    diskUserInfosUsecase.execute(diskFriendListSubscriber);
  }

  public void syncFriendList() {
    if (cloudFriendListSubscriber != null) {
      cloudFriendListSubscriber.unsubscribe();
    }

    cloudFriendListSubscriber = new FriendListSubscriber(true);
    cloudUserInfos.execute(cloudFriendListSubscriber);
  }

  private void showFriendCollectionInView(List<Recipient> recipientList) {
    this.homeGridView.renderRecipientList(recipientList);
  }

  public void leaveGroup(String membershipId) {
    leaveGroup.prepare(membershipId);
    leaveGroup.execute(new LeaveGroupSubscriber());
  }

  public void removeGroup(String groupId) {
    removeGroup.prepare(groupId);
    removeGroup.execute(new RemoveGroupSubscriber());
  }

  public void getHeadDeepLink(String url) {
    getHeadDeepLink.prepare(url);
    getHeadDeepLink.execute(new GetHeadDeepLinkSubscriber());
  }

  public void createMembership(String groupId) {
    createMembership.setGroupId(groupId);
    createMembership.execute(new CreateMembershipSubscriber());
  }

  public void sendToken(String token) {
    sendTokenUseCase.setToken(token);
    sendTokenUseCase.execute(new SendTokenSubscriber());
  }

  public void bookRoomLink(String linkId) {
    bookRoomLink.setLinkId(linkId);
    bookRoomLink.execute(new BookRoomLinkSubscriber());
  }

  protected void showViewLoading() {
    homeGridView.showLoading();
  }

  protected void hideViewLoading() {
    homeGridView.hideLoading();
  }

  protected void showErrorMessage(ErrorBundle errorBundle) {
    if (homeGridView != null && homeGridView.context() != null) {
      String errorMessage =
          ErrorMessageFactory.create(homeGridView.context(), errorBundle.getException());
      homeGridView.showError(errorMessage);
    }
  }

  private final class FriendListSubscriber extends DefaultSubscriber<User> {

    private boolean cloud = false;

    public FriendListSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (cloud) showErrorMessage(new DefaultErrorBundle((Exception) e));
    }

    @Override public void onNext(User user) {
      if (!cloud) {
        List<Recipient> recipients = user.getFriendshipList();
        showFriendCollectionInView(recipients);
      }
    }
  }

  private final class LeaveGroupSubscriber extends DefaultSubscriber<Void> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Void aVoid) {
      homeGridView.refreshGrid();
    }
  }

  private final class RemoveGroupSubscriber extends DefaultSubscriber<Void> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Void aVoid) {
      homeGridView.refreshGrid();
    }
  }

  private final class UpdateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(Friendship friendship) {
      homeGridView.onFriendshipUpdated(friendship);
      loadFriendList();
    }
  }

  private final class GetHeadDeepLinkSubscriber extends DefaultSubscriber<String> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(String url) {
      if (!StringUtils.isEmpty(url)) homeGridView.onDeepLink(url);
    }
  }

  private final class CreateMembershipSubscriber extends DefaultSubscriber<Membership> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Membership membership) {
      homeGridView.onMembershipCreated(membership);
    }
  }

  private final class SendTokenSubscriber extends DefaultSubscriber<Installation> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(Installation installation) {
    }
  }

  public void updateUserFacebook(String userId, String accessToken) {

    updateUserFacebook.prepare(userId, accessToken);
    updateUserFacebook.execute(new DefaultSubscriber() {
      @Override public void onError(Throwable e) {
        super.onError(e);
        System.out.println("ON ERROR" + e.getMessage());
      }

      @Override public void onNext(Object o) {
        super.onNext(o);
        System.out.println("ON NEXT");
      }
    });
  }

  public void loginFacebook() {
    if (!FacebookUtils.isLoggedIn()) {
      rxFacebook.requestLogin().subscribe(loginResult -> {
        if (FacebookUtils.isLoggedIn()) {
          homeGridView.successFacebookLogin();
        } else {
          homeGridView.errorFacebookLogin();
        }
      });
    } else {
      homeGridView.successFacebookLogin();
    }
  }

  public void lookupContacts() {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.execute(lookupContactsSubscriber);
    if (homeGridView != null) homeGridView.onSyncStart();
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      homeGridView.onSyncError();
    }

    @Override public void onNext(List<Contact> contactList) {
      if (homeGridView != null) homeGridView.onSyncDone();
    }
  }

  public void removeNewStatusContact() {
    jobManager.addJobInBackground(new RemoveNewStatusContactJob());
  }

  public void loadContactsOnApp() {
    if (contactsOnAppSubscriber != null) {
      contactsOnAppSubscriber.unsubscribe();
      getDiskContactOnAppList.unsubscribe();
    }

    contactsOnAppSubscriber = new ContactsOnAppSubscriber();
    getDiskContactOnAppList.execute(contactsOnAppSubscriber);
  }

  private final class ContactsOnAppSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Contact> contactList) {
      homeGridView.renderContactsOnApp(contactList);
    }
  }

  public void declineInvite(String roomId) {
    declineInvite.prepare(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void sendInvitations() {
    sendInvitations.execute(new DefaultSubscriber());
  }

  public void createFriendship(String userId) {
    createFriendship.setUserId(userId);
    createFriendship.execute(new DefaultSubscriber());
  }

  private class BookRoomLinkSubscriber extends DefaultSubscriber<Boolean> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Boolean isBookLink) {
      homeGridView.onBookLink(isBookLink);
    }
  }
}
