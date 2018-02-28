package com.tribe.app.presentation.view.activity.corona;

/**
 * Created by nicolasbradier on 22/02/2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.game.corona.GameCoronaViewOld;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameCoronaFragment extends Fragment {

  public GameCoronaViewOld coronaView;

  public GameCoronaFragment() {
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_game_corona, container, false);

    coronaView = (GameCoronaViewOld) rootView.findViewById(R.id.coronaView);
    coronaView.coronaView.init("coronatest/aliens-attack/");
    coronaView.coronaView.setZOrderMediaOverlay(false);

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    coronaView.coronaView.resume();
  }

  @Override public void onPause() {
    super.onPause();
    coronaView.coronaView.pause();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    coronaView.coronaView.destroy();
  }
}
