package com.tribe.app.presentation.view.activity.corona;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import com.ansca.corona.CoronaView;
import com.tribe.app.R;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.live.game.corona.GameCoronaViewOld;
import java.util.Hashtable;
import timber.log.Timber;

/**
 * Created by nicolasbradier on 22/02/2018.
 */

public class GameCoronaActivity extends AppCompatActivity {

  public static final String GAME_ID = "game_id";
  public GameCoronaViewOld coronaView;

  public static Intent getCallingIntent(Activity activity, String gameId) {
    Intent intent = new Intent(activity, GameCoronaActivity.class);
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

    //CoronaView coronaView = this.findViewById(R.id.coronaView);
    coronaView = (GameCoronaViewOld) findViewById(R.id.coronaView);
    coronaView.coronaView.setCoronaEventListener(new CoronaView.CoronaEventListener() {
      @Override public Object onReceivedCoronaEvent(CoronaView coronaView,
          Hashtable<Object, Object> hashtable) {

        Timber.d("CoronaEventListener -> " + hashtable);

        return null;
      }
    });
  }

  @Override protected void onStart() {
    super.onStart();
    coronaView.coronaView.init("coronatest/aliens-attack/");
    coronaView.coronaView.setZOrderMediaOverlay(false);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Navigator.FROM_GAME_DETAILS && resultCode == RESULT_OK && data != null) {
      setResult(RESULT_OK, data);
      finish();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    coronaView.coronaView.resume();
  }

  @Override protected void onPause() {
    super.onPause();
    coronaView.coronaView.pause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    coronaView.coronaView.destroy();
  }
}
