package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.LogUtil;
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

public class JsonToModel {

  public JsonToModel() {
  }

  // OBSERVABLES
  private PublishSubject<TribeOffer> onReceivedOffer = PublishSubject.create();
  private PublishSubject<TribeJoinRoom> onJoinRoom = PublishSubject.create();
  private PublishSubject<TribeCandidate> onCandidate = PublishSubject.create();
  private PublishSubject<WebSocketError> onError = PublishSubject.create();
  private PublishSubject<TribeSession> onLeaveRoom = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onInvitedTribeGuestList = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onRemovedTribeGuestList = PublishSubject.create();
  private PublishSubject<TribePeerMediaConfiguration> onTribeMediaPeerConfiguration = PublishSubject.create();

  private void convertToModel(String json) throws IOException {
    @Room.WebSocketMessageType String localWebSocketType = getWebSocketMessageFromJson(json);

    if (localWebSocketType == null) {
      LogUtil.e(getClass(), "WebSocket message unhandled");
      return;
    }

    JSONObject object = null;
    try {
      object = new JSONObject(json);

      if (localWebSocketType.equals(Room.MESSAGE_OFFER)) {
        JSONObject data = object.getJSONObject("d");
        LogUtil.d(getClass(), "Received challenge : " + data);
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
        LogUtil.d(getClass(), "Exchange candidate : " + data.toString());

        JSONObject session = data.getJSONObject("from");
        TribeSession tribeSession =
            new TribeSession(session.getString("socketId"), session.getString("userId"));

        JSONObject candidate = data.getJSONObject("candidate");

        TribeCandidate tribeCandidate = new TribeCandidate(tribeSession,
            new IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"),
                candidate.getString("candidate")));

        onCandidate.onNext(tribeCandidate);
      } else if (localWebSocketType.equals(Room.MESSAGE_LEAVE)) {
        JSONObject d = object.getJSONObject("d");
        LogUtil.d(getClass(), Room.MESSAGE_LEAVE + " received : " + d.toString());
        String peerId = d.getString("socketId");
        String userId = d.getString("userId");
        onLeaveRoom.onNext(new TribeSession(peerId, userId));
      } else if (localWebSocketType.equals(Room.MESSAGE_JOIN)) {
        // TODO handle userMediaConfiguration
        JSONObject r = object.getJSONObject("d");
        LogUtil.d(getClass(), "Join response received : " + r.toString());
        JSONArray jsonArray = r.getJSONArray("sessions");

        int roomSize = r.getInt("roomSize");
        List<TribeSession> sessionList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject session = jsonArray.getJSONObject(i);
          sessionList.add(
              new TribeSession(session.getString("socketId"), session.getString("userId")));
        }

        onJoinRoom.onNext(new TribeJoinRoom(sessionList, roomSize));
      } else if (localWebSocketType.equals(Room.MESSAGE_ERROR)) {
        boolean success = object.getBoolean("success");

        if (!success) {
          String error = object.getString("error");
          onError.onNext(new WebSocketError(error, "Can't connect"));
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
      LogUtil.e(getClass(), ex.toString());
    }
  }

  private @Room.WebSocketMessageType String getWebSocketMessageFromJson(String json) {
    JSONObject object = null;

    try {
      object = new JSONObject(json);

      if (object.has("a")) {
        String a = object.getString("a");
        return Room.getWebSocketMessageType(a);
      } else if (object.has("error")) {
        return Room.MESSAGE_ERROR;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void convertDataChannelToModel(String json, TribeSession session) throws IOException {
    JSONObject object = null;
    try {
      object = new JSONObject(json);

      if (object.has(Room.MESSAGE_APP)) {
        JSONObject app = object.getJSONObject(Room.MESSAGE_APP);
        if (app.has(Room.MESSAGE_INVITE_ADDED)) {
          LogUtil.d(getClass(), "Receiving invite added");
          List<TribeGuest> guestList = new ArrayList<>();
          JSONArray arrayInvited = app.getJSONArray(Room.MESSAGE_INVITE_ADDED);
          for (int i = 0; i < arrayInvited.length(); i++) {
            JSONObject guest = arrayInvited.getJSONObject(i);
            guestList.add(new TribeGuest(guest.getString("id"), guest.getString("display_name"), guest.getString("picture")));
          }
          onInvitedTribeGuestList.onNext(guestList);

        } else if (app.has(Room.MESSAGE_INVITE_REMOVED)) {
          LogUtil.d(getClass(), "Receiving invite removed");
          List<TribeGuest> guestRemovedList = new ArrayList<>();
          JSONArray arrayRemoved = app.getJSONArray(Room.MESSAGE_INVITE_REMOVED);
          for (int i = 0; i < arrayRemoved.length(); i++) {
            JSONObject guest = arrayRemoved.getJSONObject(i);
            guestRemovedList.add(new TribeGuest(guest.getString("id")));
          }
          onRemovedTribeGuestList.onNext(guestRemovedList);

        }
      } else if (object.has(Room.MESSAGE_MEDIA_CONFIGURATION)) {
        LogUtil.d(getClass(), "Receiving media configuration");
        TribePeerMediaConfiguration peerMediaConfiguration = new TribePeerMediaConfiguration(session);
        peerMediaConfiguration.setAudioEnabled(object.getBoolean("isAudioEnabled"));
        peerMediaConfiguration.setVideoEnabled(object.getBoolean("isVideoEnabled"));
        onTribeMediaPeerConfiguration.onNext(peerMediaConfiguration);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void convertDataChannel(String json, TribeSession session) {
    try {
      convertDataChannelToModel(json, session);
      return;
    } catch (IOException ex) {
      LogUtil.e(getClass(), ex.toString());
    }
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

  public Observable<List<TribeGuest>> onInvitedTribeGuestList() {
    return onInvitedTribeGuestList;
  }

  public Observable<List<TribeGuest>> onRemovedTribeGuestList() {
    return onRemovedTribeGuestList;
  }

  public Observable<TribePeerMediaConfiguration> onTribePeerMediaConfiguration() {
    return onTribeMediaPeerConfiguration;
  }
}
