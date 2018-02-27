package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetCloudUserInfosList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.UserMVPView;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class UserPresenter implements Presenter {

  // VIEW ATTACHED
  private UserMVPView userMVPView;

  // USECASES
  private GetDiskUserInfos diskUserInfosUsecase;
  private GetCloudUserInfos cloudUserInfos;
  private SynchroContactList synchroContactList;
  private GetCloudUserInfosList cloudUserInfosList;

  // SUBSCRIBERS
  private GetUserInfoListSubscriber getUserInfoListSubscriber;

  @Inject public UserPresenter(Context context, GetDiskUserInfos diskUserInfos,
      GetCloudUserInfos cloudUserInfos, SynchroContactList synchroContactList,
      GetCloudUserInfosList cloudUserInfosList) {

    this.diskUserInfosUsecase = diskUserInfos;
    this.cloudUserInfos = cloudUserInfos;
    this.synchroContactList = synchroContactList;
    this.cloudUserInfosList = cloudUserInfosList;
  }

  @Override public void onViewDetached() {
    cloudUserInfos.unsubscribe();
    diskUserInfosUsecase.unsubscribe();
    cloudUserInfosList.unsubscribe();
    userMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    userMVPView = (UserMVPView) v;
  }

  public void getUserInfos() {
    cloudUserInfos.setUserId(null);
    cloudUserInfos.execute(new DefaultSubscriber());

    diskUserInfosUsecase.prepare(null);
    diskUserInfosUsecase.execute(new UserInfosSubscriber());
  }

  public void getUsersInfoListById(List<String> useridsList) {
    if (getUserInfoListSubscriber != null) getUserInfoListSubscriber.unsubscribe();
    getUserInfoListSubscriber = new GetUserInfoListSubscriber();

    cloudUserInfosList.setUserIdsList(useridsList);
    cloudUserInfosList.execute(getUserInfoListSubscriber);
  }

  private final class GetUserInfoListSubscriber extends DefaultSubscriber<List<User>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<User> users) {
      super.onNext(users);
      if (userMVPView != null) userMVPView.onUserInfosList(users);
    }
  }

  private final class UserInfosSubscriber extends DefaultSubscriber<User> {
    @Override public void onError(Throwable e) {
      Timber.e("Error get user infos : " + e);
    }

    @Override public void onNext(User user) {
      if (userMVPView != null) userMVPView.onUserInfos(user);
    }
  }

  public void syncContacts(Preference<Long> lastSync) {
    synchroContactList.execute(new DefaultSubscriber() {
      @Override public void onNext(Object o) {
        lastSync.set(System.currentTimeMillis());
      }
    });
  }
}
