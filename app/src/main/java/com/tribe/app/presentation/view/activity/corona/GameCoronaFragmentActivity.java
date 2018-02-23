package com.tribe.app.presentation.view.activity.corona;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import com.tribe.app.R;
import com.tribe.app.presentation.navigation.Navigator;

/**
 * Created by nicolasbradier on 22/02/2018.
 */

public class GameCoronaFragmentActivity extends AppCompatActivity {

  public static final String GAME_ID = "game_id";

  public static Intent getCallingIntent(Activity activity, String gameId) {
    Intent intent = new Intent(activity, GameCoronaFragmentActivity.class);
    intent.putExtra(GAME_ID, gameId);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_corona);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, new GameCoronaFragment())
          .commit();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Navigator.FROM_GAME_DETAILS && resultCode == RESULT_OK && data != null) {
      setResult(RESULT_OK, data);
      finish();
    }
  }
}
