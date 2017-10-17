package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.DeleteRoomJob;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetDataChallengesGame;
import com.tribe.app.domain.interactor.game.GetNamesDrawGame;
import com.tribe.app.domain.interactor.game.GetNamesPostItGame;
import com.tribe.app.domain.interactor.user.FbIdUpdated;
import com.tribe.app.domain.interactor.user.GetCloudUserInfosList;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.domain.interactor.user.IncrUserTimeInCall;
import com.tribe.app.domain.interactor.user.ReportUser;
import com.tribe.app.presentation.mvp.presenter.common.RoomPresenter;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;

public class LivePresenter implements Presenter {

  // COMPOSITE PRESENTERS
  private ShortcutPresenter shortcutPresenter;
  private RoomPresenter roomPresenter;

  // VIEW ATTACHED
  private LiveMVPView liveMVPView;

  // USECASES
  private JobManager jobManager;
  private GetRecipientInfos getRecipientInfos;
  private GetCloudUserInfosList cloudUserInfosList;
  private GetNamesPostItGame getNamesPostItGame;
  private GetNamesDrawGame getNamesDrawGame;
  private GetDataChallengesGame getDataChallengesGame;
  private FbIdUpdated fbIdUpdated;
  private ReportUser reportUser;
  private IncrUserTimeInCall incrUserTimeInCall;

  // SUBSCRIBERS
  private GetUserInfoListSubscriber getUserInfoListSubscriber;
  private FbIdUpdatedSubscriber fbIdUpdatedSubscriber;

  @Inject public LivePresenter(JobManager jobManager, RoomPresenter roomPresenter,
      ShortcutPresenter shortcutPresenter, GetRecipientInfos getRecipientInfos,
      GetCloudUserInfosList cloudUserInfosList, GetNamesPostItGame getNamesPostItGame,
      ReportUser reportUser, FbIdUpdated fbIdUpdated, GetDataChallengesGame getDataChallengesGame,
      IncrUserTimeInCall incrUserTimeInCall, GetNamesDrawGame getNamesDrawGame) {
    this.jobManager = jobManager;
    this.shortcutPresenter = shortcutPresenter;
    this.roomPresenter = roomPresenter;
    this.getRecipientInfos = getRecipientInfos;
    this.cloudUserInfosList = cloudUserInfosList;
    this.getNamesPostItGame = getNamesPostItGame;
    this.reportUser = reportUser;
    this.incrUserTimeInCall = incrUserTimeInCall;
    this.fbIdUpdated = fbIdUpdated;
    this.getDataChallengesGame = getDataChallengesGame;
    this.getNamesDrawGame = getNamesDrawGame;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    roomPresenter.onViewDetached();
    cloudUserInfosList.unsubscribe();
    getRecipientInfos.unsubscribe();
    getNamesPostItGame.unsubscribe();
    reportUser.unsubscribe();
    incrUserTimeInCall.unsubscribe();
    fbIdUpdated.unsubscribe();
    getDataChallengesGame.unsubscribe();
    getNamesDrawGame.unsubscribe();
    liveMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    liveMVPView = (LiveMVPView) v;
    shortcutPresenter.onViewAttached(v);
    roomPresenter.onViewAttached(v);
  }

  private final class GetUserInfoListSubscriber extends DefaultSubscriber<List<User>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<User> users) {
      super.onNext(users);
      liveMVPView.onReceivedAnonymousMemberInRoom(users);
    }
  }

  public void getUsersInfoListById(List<String> useridsList) {
    if (getUserInfoListSubscriber != null) getUserInfoListSubscriber.unsubscribe();
    getUserInfoListSubscriber = new GetUserInfoListSubscriber();

    cloudUserInfosList.setUserIdsList(useridsList);
    cloudUserInfosList.execute(getUserInfoListSubscriber);
  }

  public void incrementTimeInCall(String userId, Long timeInCall) {

    if (timeInCall != null) {
      incrUserTimeInCall.prepare(userId, timeInCall);
      incrUserTimeInCall.execute(new DefaultSubscriber());
    }
  }

  public void getNamesPostItGame(String lang) {
    getNamesPostItGame.setup(lang);
    getNamesPostItGame.execute(new GetNamesPostItGameSubscriber());
  }

  public void getNamesDrawGame(String lang) {
    getNamesDrawGame.setup(lang);
    getNamesDrawGame.execute(new GetNamesDrawGameSubscriber());
  }

  public void getDataChallengesGame(String lang) {
    getDataChallengesGame.setup(lang);
    getDataChallengesGame.execute(new GetDataChallengesGameSubscriber());
  }

  private final class GetNamesPostItGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onNamesPostItGame(nameList);
    }
  }

  private final class GetNamesDrawGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onNamesDrawGame(nameList);
    }
  }

  private final class GetDataChallengesGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onDataChallengesGame(nameList);
    }
  }

  public void reportUser(String userId) {
    reportUser.setUserId(userId);
    reportUser.execute(new DefaultSubscriber());
  }

  private final class FbIdUpdatedSubscriber extends DefaultSubscriber<User> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(User userUpdated) {
      liveMVPView.fbIdUpdatedSubscriber(userUpdated);
    }
  }

  public void fbidUpdated() {
    if (fbIdUpdatedSubscriber != null) {
      fbIdUpdatedSubscriber.unsubscribe();
    }

    fbIdUpdatedSubscriber = new FbIdUpdatedSubscriber();
    fbIdUpdated.execute(fbIdUpdatedSubscriber);
  }

  public void randomRoomAssigned() {
    roomPresenter.randomRoomAssigned();
  }

  public void subscribeToRoomUpdates(String roomId) {
    roomPresenter.subscribeToRoomUpdates(roomId);
  }

  public void buzzRoom(String roomId) {
    roomPresenter.buzzRoom(roomId);
  }

  public void removeInvite(String roomId, String userId) {
    roomPresenter.removeInvite(roomId, userId);
  }

  public void createInvite(String roomId, String userId) {
    roomPresenter.createInvite(roomId, userId);
  }

  public void createRoom(Live live) {
    roomPresenter.createRoom(live);
  }

  public void deleteRoom(String roomId) {
    jobManager.addJobInBackground(new DeleteRoomJob(roomId));
  }

  public void roomAcceptRandom(String roomId) {
    roomPresenter.roomAcceptRandom(roomId);
  }

  public void getRoomInfos(Live live) {
    roomPresenter.getRoomInfos(live);
  }

  public void declineInvite(String roomId) {
    roomPresenter.declineInvite(roomId);
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    shortcutPresenter.muteShortcut(shortcutId, mute);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status);
  }

  public void updateShortcutName(String shortcutId, String name) {
    shortcutPresenter.updateShortcutName(shortcutId, name);
  }

  public void readShortcut(String shortcutId) {
    shortcutPresenter.readShortcut(shortcutId);
  }

  public void pinShortcut(String shortcutId, boolean pinned) {
    shortcutPresenter.pinShortcut(shortcutId, pinned);
  }

  public void removeShortcut(String shortcutId) {
    shortcutPresenter.removeShortcut(shortcutId);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }

  public void shortcutForUserIds(List<String> userIds) {
    shortcutPresenter.shortcutForUserIds(userIds.toArray(new String[userIds.size()]));
  }
}
