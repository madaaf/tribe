package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.RemoveInvite;
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
  private RemoveInvite removeInvite;
  private CreateRoom createRoom;

  private GetBlockedFriendshipListSubscriber getBlockedFriendshipListSubscriber;
  private GetUnblockedFriendshipListSubscriber getUnblockedFriendshipListSubscriber;

  @Inject ProfilePresenter(UpdateUser updateUser, LookupUsername lookupUsername,
      RxFacebook rxFacebook, RemoveInstall removeInstall,
      GetBlockedFriendshipList getBlockedFriendshipList, UpdateFriendship updateFriendship,
      RemoveInvite removeInvite, GetDiskUnblockedFriendshipList getDiskUnblockedFriendshipList,
      CreateRoom createRoom, UpdateUserFacebook updateUserFacebook,
      UpdateUserPhoneNumber updateUserPhoneNumber) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber);
    this.removeInstall = removeInstall;
    this.getBlockedFriendshipList = getBlockedFriendshipList;
    this.updateFriendship = updateFriendship;
    this.removeInvite = removeInvite;
    this.getDiskUnblockedFriendshipList = getDiskUnblockedFriendshipList;
    this.createRoom = createRoom;
  }

  @Override public void onViewDetached() {
    removeInstall.unsubscribe();
    getBlockedFriendshipList.unsubscribe();
    updateFriendship.unsubscribe();
    removeInvite.unsubscribe();
    getDiskUnblockedFriendshipList.unsubscribe();
    createRoom.unsubscribe();
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

  public void removeInvite(String roomId, String userId) {
    removeInvite.setup(roomId, userId);
    removeInvite.execute(new DefaultSubscriber());
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

  public void createRoom() {
    createRoom.execute(new CreateRoomSubscriber());
  }

  private class CreateRoomSubscriber extends DefaultSubscriber<Room> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(Room room) {
      if (profileView != null) profileView.onCreateRoom(room);
    }
  }
}
