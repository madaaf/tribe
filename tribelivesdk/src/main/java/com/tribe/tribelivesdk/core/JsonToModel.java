package com.tribe.tribelivesdk.core;

import android.support.v4.util.Pair;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribeMediaConstraints;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class JsonToModel {

  public JsonToModel() {
  }

  // VARIABLES
  private TribeLiveOptions options;

  // OBSERVABLES
  private PublishSubject<TribeOffer> onReceivedOffer = PublishSubject.create();
  private PublishSubject<TribeJoinRoom> onJoinRoom = PublishSubject.create();
  private PublishSubject<TribeCandidate> onCandidate = PublishSubject.create();
  private PublishSubject<WebSocketError> onError = PublishSubject.create();
  private PublishSubject<TribeSession> onLeaveRoom = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onInvitedTribeGuestList = PublishSubject.create();
  private PublishSubject<Void> onRollTheDiceReceived = PublishSubject.create();
  private PublishSubject<String> unlockRollTheDice = PublishSubject.create();
  private PublishSubject<String> unlockedRollTheDice = PublishSubject.create();
  private PublishSubject<String> onFbIdUpdated = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onRemovedTribeGuestList = PublishSubject.create();
  private PublishSubject<TribePeerMediaConfiguration> onTribeMediaPeerConfiguration =
      PublishSubject.create();
  private PublishSubject<TribeMediaConstraints> onTribeMediaConstraints = PublishSubject.create();
  private PublishSubject<TribePeerMediaConfiguration> onShouldSwitchRemoteMediaMode =
      PublishSubject.create();
  private PublishSubject<Pair<TribeSession, String>> onNewGame = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, String>> onStopGame = PublishSubject.create();

  public void setOptions(TribeLiveOptions options) {
    this.options = options;
  }

  private void convertToModel(String json) throws IOException {
    if (json == null && !json.equals("")) return;

    @Room.WebSocketMessageType String localWebSocketType = getWebSocketMessageFromJson(json);

    if (localWebSocketType == null) {
      Timber.e("WebSocket message unhandled : " + json);
      return;
    }

    JSONObject object = null;
    try {
      object = new JSONObject(json);

      if (localWebSocketType.equals(Room.MESSAGE_OFFER)) {

        JSONObject data = object.getJSONObject("d");
        Timber.d("Received challenge : " + data);
        JSONObject sdpJSON = data.getJSONObject("sdp");

        final SessionDescription sdp = new SessionDescription(
            SessionDescription.Type.fromCanonicalForm(sdpJSON.getString("type")),
            sdpJSON.getString("sdp"));

        JSONObject session = data.getJSONObject("from");
        TribeSession tribeSession =
            new TribeSession(session.getString("socketId"), session.getString("userId"));

        onReceivedOffer.onNext(new TribeOffer(tribeSession, sdp));
      } else if (localWebSocketType.equals(Room.MESSAGE_CANDIDATE)) {

        JSONObject data = object.getJSONObject("d");
        Timber.d("Exchange candidate : " + data.toString());

        JSONObject session = data.getJSONObject("from");
        TribeSession tribeSession =
            new TribeSession(session.getString("socketId"), session.getString("userId"));

        JSONObject candidate = data.getJSONObject("candidate");

        TribeCandidate tribeCandidate = new TribeCandidate(tribeSession,
            new IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"),
                candidate.getString("candidate")));

        onCandidate.onNext(tribeCandidate);
      } else if (localWebSocketType.equals(Room.MESSAGE_LEAVE)) {

        Timber.d("Leave message received");
        JSONObject d = object.getJSONObject("d");
        Timber.d(Room.MESSAGE_LEAVE + " received : " + d.toString());
        String peerId = d.getString("socketId");
        String userId = d.getString("userId");
        onLeaveRoom.onNext(new TribeSession(peerId, userId));
      } else if (localWebSocketType.equals(Room.MESSAGE_JOIN)) {

        TribeJoinRoom tribeJoinRoom;

        JSONObject r = object.getJSONObject("d");
        Timber.d("Join response received : " + r.toString());
        JSONArray jsonArray = r.getJSONArray("sessions");

        int roomSize = r.getInt("roomSize");
        List<TribeSession> sessionList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject session = jsonArray.getJSONObject(i);
          sessionList.add(
              new TribeSession(session.getString("socketId"), session.getString("userId")));
        }

        tribeJoinRoom = new TribeJoinRoom(sessionList, roomSize);

        onJoinRoom.onNext(tribeJoinRoom);

        if (r.has("userMediaConfiguration")) {
          Timber.d("Media constraints in join received");
          JSONObject jo = r.getJSONObject("userMediaConfiguration");
          computeMediaConstraints(jo, false);
        }
      } else if (localWebSocketType.equals(Room.MESSAGE_LEAVE)) {

        Timber.d("Leave message received");
        JSONObject d = object.getJSONObject("d");
        Timber.d(Room.MESSAGE_LEAVE + " received : " + d.toString());
        String peerId = d.getString("socketId");
        String userId = d.getString("userId");
        onLeaveRoom.onNext(new TribeSession(peerId, userId));
      } else if (localWebSocketType.equals(Room.MESSAGE_MEDIA_CONSTRAINTS)) {

        Timber.d("User configuration message received");
        JSONObject d = object.getJSONObject("d");
        computeMediaConstraints(d, true);
      } else if (localWebSocketType.equals(Room.MESSAGE_REMOTE_SWITCH_MODE)) {

        Timber.d("Receiving remote switch mode (low connectivity)");
        JSONObject d = object.getJSONObject("d");
        JSONObject session = d.getJSONObject("from");
        TribeSession tribeSession =
            new TribeSession(session.getString("socketId"), session.getString("userId"));

        TribePeerMediaConfiguration peerMediaConfiguration =
            new TribePeerMediaConfiguration(tribeSession);
        peerMediaConfiguration.setAudioEnabled(d.getBoolean("audio"));
        peerMediaConfiguration.setVideoEnabled(d.getBoolean("video"));
        peerMediaConfiguration.setMediaConfigurationType(TribePeerMediaConfiguration.LOW_BANDWIDTH);
        onShouldSwitchRemoteMediaMode.onNext(peerMediaConfiguration);
      } else if (localWebSocketType.equals(Room.MESSAGE_MESSAGE)) {

        JSONObject d = object.getJSONObject("d");
        Timber.d("Received message app");

        JSONObject session = d.getJSONObject("from");
        TribeSession tribeSession =
            new TribeSession(session.getString("socketId"), session.getString("userId"));

        JSONObject message = d.getJSONObject("message");
        if (message.has(Room.UNLOCK_ROLL_DICE)) {
          JSONObject app = message.getJSONObject(Room.UNLOCK_ROLL_DICE);
          String userId = app.get("by").toString();
          unlockRollTheDice.onNext(userId);
        }

        if (message.has(Room.UNLOCKED_ROLL_DICE)) {
          unlockedRollTheDice.onNext(tribeSession.getUserId());
        }
        if (message.has(Room.MESSAGE_APP)) {
          JSONObject app = message.getJSONObject(Room.MESSAGE_APP);

          if (app.has(Room.MESSAGE_ROLL_THE_DICE)) {
            Timber.d("Receiving roll the dice");
            onRollTheDiceReceived.onNext(null);
          } else if (app.has(Room.MESSAGE_INVITE_ADDED)) {
            Timber.d("Receiving invite added");
            List<TribeGuest> guestList = new ArrayList<>();
            JSONArray arrayInvited = app.getJSONArray(Room.MESSAGE_INVITE_ADDED);
            for (int i = 0; i < arrayInvited.length(); i++) {
              JSONObject guest = arrayInvited.getJSONObject(i);
              String userName = guest.has("username") ? guest.getString("username") : null;
              guestList.add(new TribeGuest(guest.getString("id"), guest.getString("display_name"),
                  guest.getString("picture"), false, false, null, true, userName));
            }
            onInvitedTribeGuestList.onNext(guestList);
          } else if (app.has(Room.MESSAGE_INVITE_REMOVED)) {
            Timber.d("Receiving invite removed");
            List<TribeGuest> guestRemovedList = new ArrayList<>();
            JSONArray arrayRemoved = app.getJSONArray(Room.MESSAGE_INVITE_REMOVED);
            for (int i = 0; i < arrayRemoved.length(); i++) {
              guestRemovedList.add(new TribeGuest(arrayRemoved.getString(i)));
            }
            onRemovedTribeGuestList.onNext(guestRemovedList);
          }
        } else if (message.has(Room.MESSAGE_MEDIA_CONFIGURATION)) {

          Timber.d("Receiving media configuration");
          TribePeerMediaConfiguration peerMediaConfiguration =
              new TribePeerMediaConfiguration(tribeSession);
          peerMediaConfiguration.setAudioEnabled(message.getBoolean("isAudioEnabled"));
          peerMediaConfiguration.setVideoEnabled(message.getBoolean("isVideoEnabled"));

          if (message.has("videoChangeReason")) {
            peerMediaConfiguration.setMediaConfigurationType(
                TribePeerMediaConfiguration.computeType(message.getString("videoChangeReason")));
          }

          onTribeMediaPeerConfiguration.onNext(peerMediaConfiguration);

          if (message.has(Game.CURRENT_GAME)) {
            onNewGame.onNext(new Pair<>(tribeSession,
                message.getJSONObject(Game.CURRENT_GAME).getString(Game.ID)));
          }
        } else if (message.has(Room.MESSAGE_GAME)) {
          JSONObject gameMessage = message.getJSONObject(Room.MESSAGE_GAME);
          String action = gameMessage.getString(Game.ACTION);
          if (action.equals(Game.START)) {
            onNewGame.onNext(new Pair<>(tribeSession, gameMessage.getString(Game.ID)));
          } else if (action.equals(Game.STOP)) {
            onStopGame.onNext(new Pair<>(tribeSession, gameMessage.getString(Game.ID)));
          }
        }
      } else if (object != null && object.has(Room.MESSAGE_ERROR)) {
        boolean success = object.getBoolean("s");

        if (!success) {
          String error = object.getString("e");
          onError.onNext(new WebSocketError(Integer.parseInt(error), "Can't connect"));
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void convert(String json) {
    try {
      convertToModel(json);
      return;
    } catch (IOException ex) {
      Timber.e(ex.toString());
    }
  }

  private @Room.WebSocketMessageType String getWebSocketMessageFromJson(String json) {
    JSONObject object = null;

    try {
      object = new JSONObject(json);

      if (object.has("a") && !object.has(Room.MESSAGE_ERROR)) {
        String a = object.getString("a");
        return Room.getWebSocketMessageType(a);
      } else if (object.has(Room.MESSAGE_ERROR)) {
        return Room.MESSAGE_ERROR;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void computeMediaConstraints(JSONObject jo, boolean shouldCreateOffer)
      throws JSONException {
    Timber.d(jo.toString());
    TribeMediaConstraints tribeUserConfiguration = new TribeMediaConstraints();
    JSONObject video = jo.getJSONObject("video");
    JSONObject fps = video.getJSONObject("frameRate");
    int maxWidth = Math.min(MediaConstraints.MAX_WIDTH, video.getJSONObject("width").getInt("max"));
    int maxHeight =
        Math.min(MediaConstraints.MAX_HEIGHT, video.getJSONObject("height").getInt("max"));

    int minFps = 0;
    if (fps.has("min")) {
      fps.getInt("min");
    }

    int maxFps = 30;
    if (fps.has("max")) {
      fps.getInt("max");
    }
    tribeUserConfiguration.setMaxWidth(maxWidth);
    tribeUserConfiguration.setMaxHeight(maxHeight);
    tribeUserConfiguration.setMaxFps(maxFps);
    tribeUserConfiguration.setMinFps(minFps);
    tribeUserConfiguration.setShouldCreateOffer(shouldCreateOffer);
    onTribeMediaConstraints.onNext(tribeUserConfiguration);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribeJoinRoom> onJoinRoom() {
    return onJoinRoom;
  }

  public Observable<TribeOffer> onReceivedOffer() {
    return onReceivedOffer;
  }

  public Observable<TribeCandidate> onCandidate() {
    return onCandidate;
  }

  public Observable<WebSocketError> onError() {
    return onError;
  }

  public Observable<TribeSession> onLeaveRoom() {
    return onLeaveRoom;
  }

  public Observable<Void> onRollTheDiceReceived() {
    return onRollTheDiceReceived;
  }

  public Observable<String> unlockRollTheDice() {
    return unlockRollTheDice;
  }

  public Observable<String> unlockedRollTheDice() {
    return unlockedRollTheDice;
  }

  public Observable<List<TribeGuest>> onInvitedTribeGuestList() {
    return onInvitedTribeGuestList;
  }

  public Observable<List<TribeGuest>> onRemovedTribeGuestList() {
    return onRemovedTribeGuestList;
  }

  public Observable<TribePeerMediaConfiguration> onTribePeerMediaConfiguration() {
    return onTribeMediaPeerConfiguration;
  }

  public Observable<TribeMediaConstraints> onTribeMediaConstraints() {
    return onTribeMediaConstraints;
  }

  public Observable<TribePeerMediaConfiguration> onShouldSwitchRemoteMediaMode() {
    return onShouldSwitchRemoteMediaMode;
  }

  public Observable<Pair<TribeSession, String>> onNewGame() {
    return onNewGame;
  }

  public Observable<Pair<TribeSession, String>> onStopGame() {
    return onStopGame;
  }
}
