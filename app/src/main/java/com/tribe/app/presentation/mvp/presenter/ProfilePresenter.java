package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.BookRoomLink;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.GetBlockedFriendshipList;
import com.tribe.app.domain.interactor.user.GetDiskUnblockedFriendshipList;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateFriendship;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 28/01/2017.
 */

public class ProfilePresenter extends UpdateUserPresenter {

  private ProfileMVPView profileView;

  private final RemoveInstall removeInstall;
  private GetBlockedFriendshipList getBlockedFriendshipList;
  private GetDiskUnblockedFriendshipList getDiskUnblockedFriendshipList;
  private UpdateFriendship updateFriendship;
  private DeclineInvite declineInvite;
  private BookRoomLink bookRoomLink;

  private GetBlockedFriendshipListSubscriber getBlockedFriendshipListSubscriber;
  private GetUnblockedFriendshipListSubscriber getUnblockedFriendshipListSubscriber;

  @Inject ProfilePresenter(UpdateUser updateUser, LookupUsername lookupUsername,
      RxFacebook rxFacebook, RemoveInstall removeInstall,
      GetBlockedFriendshipList getBlockedFriendshipList, UpdateFriendship updateFriendship,
      DeclineInvite declineInvite, GetDiskUnblockedFriendshipList getDiskUnblockedFriendshipList,
      BookRoomLink bookRoomLink, UpdateUserFacebook updateUserFacebook, UpdateUserPhoneNumber updateUserPhoneNumber) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber);
    this.removeInstall = removeInstall;
    this.getBlockedFriendshipList = getBlockedFriendshipList;
    this.updateFriendship = updateFriendship;
    this.declineInvite = declineInvite;
    this.getDiskUnblockedFriendshipList = getDiskUnblockedFriendshipList;
    this.bookRoomLink = bookRoomLink;
  }

  @Override public void onViewDetached() {
    removeInstall.unsubscribe();
    getBlockedFriendshipList.unsubscribe();
    updateFriendship.unsubscribe();
    declineInvite.unsubscribe();
    getDiskUnblockedFriendshipList.unsubscribe();
    bookRoomLink.unsubscribe();
    profileView = null;
    super.onViewDetached();
  }

  @Override public void onViewAttached(MVPView v) {
    profileView = (ProfileMVPView) v;
  }

  public void logout() {
    removeInstall.execute(new RemoveInstallSubscriber());
  }

  @Override protected UpdateUserMVPView getUpdateUserView() {
    return profileView;
  }

  private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      goToLauncher();
    }

    @Override public void onNext(User user) {
      goToLauncher();
    }
  }

  public void goToLauncher() {
    this.profileView.goToLauncher();
  }

  public void loadBlockedFriendshipList() {
    if (getBlockedFriendshipListSubscriber != null) {
      getBlockedFriendshipListSubscriber.unsubscribe();
    }

    getBlockedFriendshipListSubscriber = new GetBlockedFriendshipListSubscriber();
    getBlockedFriendshipList.execute(getBlockedFriendshipListSubscriber);
  }

  private class GetBlockedFriendshipListSubscriber extends DefaultSubscriber<List<Friendship>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<Friendship> friendshipList) {
      profileView.renderBlockedFriendshipList(friendshipList);
      unsubscribe();
    }
  }

  public void updateFriendship(String friendshipId, boolean mute,
      @FriendshipRealm.FriendshipStatus String status) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(FriendshipRealm.MUTE, String.valueOf(mute)));
    values.add(new Pair<>(FriendshipRealm.STATUS, status));

    if (values.size() > 0) {
      updateFriendship.prepare(friendshipId, values);
      updateFriendship.execute(new DefaultSubscriber());
    }
  }

  public void declineInvite(String roomId) {
    declineInvite.prepare(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void loadUnblockedFriendshipList() {
    if (getUnblockedFriendshipListSubscriber != null) {
      getUnblockedFriendshipListSubscriber.unsubscribe();
    }

    getUnblockedFriendshipListSubscriber = new GetUnblockedFriendshipListSubscriber();
    getDiskUnblockedFriendshipList.execute(getUnblockedFriendshipListSubscriber);
  }

  private class GetUnblockedFriendshipListSubscriber extends DefaultSubscriber<List<Friendship>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<Friendship> friendshipList) {
      profileView.renderUnblockedFriendshipList(friendshipList);
      unsubscribe();
    }
  }

  public void bookRoomLink(String linkId) {
    bookRoomLink.setLinkId(linkId);
    bookRoomLink.execute(new DefaultSubscriber());
  }
}
