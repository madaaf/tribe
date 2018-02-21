package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.support.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ansca.corona.CoronaView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.game.common.GameView;

/**
 * Created by nicolasbradier on 20/02/2018.
 */

public class GameCoronaView extends GameView {

  @BindView(R.id.coronaView) CoronaView coronaView;

  public GameCoronaView(@NonNull Context context) {
    super(context);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_corona, this, true);
    unbinder = ButterKnife.bind(this);
  }

  @Override protected void initWebRTCRoomSubscriptions() {

  }

  @Override protected void takeOverGame() {

  }

  @Override public void setNextGame() {

  }
}
