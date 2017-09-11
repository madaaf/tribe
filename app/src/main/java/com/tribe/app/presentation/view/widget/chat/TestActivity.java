package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
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
    if (recipient instanceof Friendship) {
      String id = ((Friendship) recipient).getFriend().getId();
      intent.putExtra(EXTRA_LIVE, id);
    }

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
    if (getIntent().hasExtra(EXTRA_LIVE)) {
      String id = getIntent().getStringExtra(EXTRA_LIVE);
      chatView.setChatId(id);
    }
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
