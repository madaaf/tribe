package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;

/**
 * Created by remy on 28/07/2017.
 */

public class TestActivity extends BaseActivity {

  private static final String EXTRA_LIVE = "EXTRA_LIVE";

  @BindView(R.id.chatview) ChatView chatView;

  public static Intent getCallingIntent(Context context, Recipient recipient, int color,
      @LiveActivity.Source String source) {
    Intent intent = new Intent(context, TestActivity.class);
    if (recipient instanceof Shortcut) {
      User friend = ((Shortcut) recipient).getSingleFriend();
      intent.putExtra(EXTRA_LIVE, friend);
    }

    return intent;
  }

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, TestActivity.class);
  }

  private void initCallRouletteService(String usersFromatedId) {
    startService(WSService.getCallingIntent(this, WSService.CHAT_SUBSCRIBE, usersFromatedId));
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ButterKnife.bind(this);
    initDependencyInjector();

    if (getIntent().hasExtra(EXTRA_LIVE)) {
      User friend = (User) getIntent().getSerializableExtra(EXTRA_LIVE);
      chatView.setChatId(friend);
      String[] ids = new String[1];
      ids[0] = friend.getId();
      initCallRouletteService(arrayToJson(ids));
    }
  }

  public String arrayToJson(String[] array) {
    String json = "\"";
    for (int i = 0; i < array.length; i++) {
      if (i == array.length - 1) {
        json += array[i] + "\"";
      } else {
        json += array[i] + "\", \"";
      }
    }
    if (array.length == 0) json += "\"";
    return json;
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
