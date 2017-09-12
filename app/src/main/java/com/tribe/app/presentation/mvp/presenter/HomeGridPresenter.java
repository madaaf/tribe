package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetHeadDeepLink;
import com.tribe.app.domain.interactor.user.SendInvitations;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter implements Presenter {

  // VIEW ATTACHED
  private HomeGridMVPView homeGridView;

  // TEMP VARIABLES
  private String feature, phone;
  private boolean shouldOpenSMS;

  // USECASES
  private JobManager jobManager;
  private GetDiskUserInfos diskUserInfosUsecase;
  private SendToken sendTokenUseCase;
  private GetHeadDeepLink getHeadDeepLink;
  private UseCase cloudUserInfos;
  private UpdateUserFacebook updateUserFacebook;
  private RxFacebook rxFacebook;
  private UseCase synchroContactList;
  private GetDiskContactOnAppList getDiskContactOnAppList;
  private DeclineInvite declineInvite;
  private SendInvitations sendInvitations;
  private CreateRoom createRoom;

  // SUBSCRIBERS
  private RecipientListSubscriber diskRecipientListSubscriber;
  private RecipientListSubscriber cloudRecipientListSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;
  private ContactsOnAppSubscriber contactsOnAppSubscriber;

  @Inject public HomeGridPresenter(JobManager jobManager,
      @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
      @Named("sendToken") SendToken sendToken, GetHeadDeepLink getHeadDeepLink,
      @Named("cloudUserInfos") UseCase cloudUserInfos, UpdateUserFacebook updateUserFacebook,
      RxFacebook rxFacebook, @Named("synchroContactList") UseCase synchroContactList,
      GetDiskContactOnAppList getDiskContactOnAppList, DeclineInvite declineInvite,
      SendInvitations sendInvitations, CreateRoom createRoom) {
    this.jobManager = jobManager;
    this.diskUserInfosUsecase = diskUserInfos;
    this.sendTokenUseCase = sendToken;
    this.getHeadDeepLink = getHeadDeepLink;
    this.cloudUserInfos = cloudUserInfos;
    this.updateUserFacebook = updateUserFacebook;
    this.rxFacebook = rxFacebook;
    this.synchroContactList = synchroContactList;
    this.getDiskContactOnAppList = getDiskContactOnAppList;
    this.declineInvite = declineInvite;
    this.sendInvitations = sendInvitations;
    this.createRoom = createRoom;
  }

  @Override public void onViewDetached() {
    getHeadDeepLink.unsubscribe();
    cloudUserInfos.unsubscribe();
    updateUserFacebook.unsubscribe();
    diskUserInfosUsecase.unsubscribe();
    synchroContactList.unsubscribe();
    getDiskContactOnAppList.unsubscribe();
    declineInvite.unsubscribe();
    sendInvitations.unsubscribe();
    createRoom.unsubscribe();
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
    if (diskRecipientListSubscriber != null) {
      diskRecipientListSubscriber.unsubscribe();
    }

    diskRecipientListSubscriber = new RecipientListSubscriber(false);
    diskUserInfosUsecase.execute(diskRecipientListSubscriber);
  }

  public void syncFriendList() {
    if (cloudRecipientListSubscriber != null) {
      cloudRecipientListSubscriber.unsubscribe();
    }

    cloudRecipientListSubscriber = new RecipientListSubscriber(true);
    cloudUserInfos.execute(cloudRecipientListSubscriber);
  }

  private void showRecipients(List<Recipient> recipientList) {
    this.homeGridView.renderRecipientList(recipientList);
  }

  public void getHeadDeepLink(String url) {
    getHeadDeepLink.prepare(url);
    getHeadDeepLink.execute(new GetHeadDeepLinkSubscriber());
  }

  public void sendToken(String token) {
    sendTokenUseCase.setToken(token);
    sendTokenUseCase.execute(new SendTokenSubscriber());
  }

  public void createRoom(String feature, String phone, boolean shouldOpenSMS) {
    this.feature = feature;
    this.phone = phone;
    this.shouldOpenSMS = shouldOpenSMS;
    createRoom.execute(new CreateRoomSubscriber());
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

  private final class RecipientListSubscriber extends DefaultSubscriber<User> {

    private boolean cloud = false;

    public RecipientListSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (cloud) showErrorMessage(new DefaultErrorBundle((Exception) e));
    }

    @Override public void onNext(User user) {
      if (!cloud) {
        List<Recipient> recipientList = user.getRecipientList();
        showRecipients(recipientList);
      }
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
    declineInvite.setup(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void sendInvitations() {
    sendInvitations.execute(new DefaultSubscriber());
  }

  private class CreateRoomSubscriber extends DefaultSubscriber<Room> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Room room) {
      homeGridView.onCreateRoom(room, feature, phone, shouldOpenSMS);
    }
  }
}
