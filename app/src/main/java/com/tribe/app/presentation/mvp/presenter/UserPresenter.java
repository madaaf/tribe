package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.UserMVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import com.tribe.tribelivesdk.game.Game;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class UserPresenter implements Presenter {

  // VIEW ATTACHED
  private UserMVPView userMVPView;

  // USECASES
  private GetDiskUserInfos diskUserInfosUsecase;
  private GetCloudUserInfos cloudUserInfos;

  // SUBSCRIBERS

  @Inject public UserPresenter(Context context, GetDiskUserInfos diskUserInfos,
      GetCloudUserInfos cloudUserInfos) {

    this.diskUserInfosUsecase = diskUserInfos;
    this.cloudUserInfos = cloudUserInfos;
  }

  @Override public void onViewDetached() {
    cloudUserInfos.unsubscribe();
    diskUserInfosUsecase.unsubscribe();
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

  private final class UserInfosSubscriber extends DefaultSubscriber<User> {
    @Override public void onError(Throwable e) {
      Timber.e("Error get user infos : " + e);
    }

    @Override public void onNext(User user) {
      if (userMVPView != null) userMVPView.onUserInfos(user);
    }
  }
}
