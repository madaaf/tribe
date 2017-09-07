package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by remy on 28/07/2017.
 */

public class TestActivity extends BaseActivity{

  private static final String EXTRA_LIVE = "EXTRA_LIVE";


  public static Intent getCallingIntent(Context context, Recipient recipient, int color,
      @LiveActivity.Source String source) {
    Intent intent = new Intent(context, TestActivity.class);

    Live.Builder builder = new Live.Builder(recipient.getId(), recipient.getSubId()).color(color)
        .displayName(recipient.getDisplayName())
        .userName(recipient.getUsername())
        .isGroup(recipient.isGroup())
        .countdown(!recipient.isLive())
        .picture(recipient.getProfilePicture())
        .source(source);
    if (recipient instanceof Friendship) {
      String fbId = ((Friendship) recipient).getFriend().getFbid();
      builder.fbId(fbId);
    }
    if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      builder.memberList(invite.getMembers());
      builder.sessionId(invite.getRoomId());
      builder.isInvite(true);
    } else if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      builder.memberList(membership.getGroup().getMembers());
    }

    intent.putExtra(EXTRA_LIVE, builder.build());

    return intent;
  }

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, TestActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ButterKnife.bind(this);
    initDependencyInjector();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }


}
