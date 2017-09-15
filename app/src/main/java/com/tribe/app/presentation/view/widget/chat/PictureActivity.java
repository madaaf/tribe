package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.GlideUtils;

/**
 * Created by madaaflak on 08/09/2017.
 */

public class PictureActivity extends Activity {

  public static Intent getCallingIntent(Context context, String uri) {
    Intent intent = new Intent(context, PictureActivity.class);
    intent.putExtra("SOEF", uri);
    return intent;
  }

  @BindView(R.id.picture) ImageView picture;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture);
    ButterKnife.bind(this);

    if (getIntent().hasExtra("SOEF")) {
      String uri = getIntent().getStringExtra("SOEF");
      new GlideUtils.Builder(this).url(uri)
          .rounded(false)
          .target(picture)
          .hasPlaceholder(false)
          .load();
    }
  }
}
