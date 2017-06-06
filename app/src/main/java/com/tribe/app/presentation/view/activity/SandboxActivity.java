package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.tribe.app.R;

/**
 * Created by madaaflak on 07/06/2017.
 */

public class SandboxActivity extends Activity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }
}
