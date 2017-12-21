package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaView extends GameViewWithRanking {

  // VARIABLES

  public GameTriviaView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_trivia, this, true);
    unbinder = ButterKnife.bind(this);
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
              if (message.has(ACTION_KEY)) {

              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  @Override protected void takeOverGame() {

  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, mapObservable, liveViewsObservable, userId);
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
