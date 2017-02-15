package com.tribe.tribelivesdk.core;

import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 13/01/2017.
 */

public class Room {

  @StringDef({
      STATE_NEW, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTED, STATE_ERROR
  }) public @interface RoomState {
  }

  public static final String STATE_NEW = "new";
  public static final String STATE_CONNECTING = "connecting";
  public static final String STATE_CONNECTED = "connected";
  public static final String STATE_DISCONNECTED = "disconnected";
  public static final String STATE_ERROR = "error";

  @StringDef({
      MESSAGE_ERROR, MESSAGE_JOIN, MESSAGE_OFFER, MESSAGE_CANDIDATE, MESSAGE_LEAVE, MESSAGE_NONE
  }) public @interface WebSocketMessageType {
  }

  public static final String MESSAGE_ERROR = "error";
  public static final String MESSAGE_JOIN = "joinR";
  public static final String MESSAGE_OFFER = "eventExchangeSdp";
  public static final String MESSAGE_CANDIDATE = "eventExchangeCandidate";
  public static final String MESSAGE_LEAVE = "eventLeave";
  public static final String MESSAGE_NONE = "none";

  @StringDef({
      MESSAGE_APP, MESSAGE_MEDIA_CONFIGURATION, MESSAGE_INVITE_ADDED, MESSAGE_INVITE_REMOVED
  }) public @interface DataChannelMessageType {
  }

  public static final String MESSAGE_APP = "app";
  public static final String MESSAGE_MEDIA_CONFIGURATION = "isVideoEnabled";
  public static final String MESSAGE_INVITE_ADDED = "invited_guests";
  public static final String MESSAGE_INVITE_REMOVED = "remove_invited_guests";

  private WebSocketConnection webSocketConnection;
  private WebRTCClient webRTCClient;
  private TribeLiveOptions options;
  private @Room.RoomState String state;
  private JsonToModel jsonToModel;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onRoomStateChanged = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerAdded = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerUpdated = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onInvitedTribeGuestList = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onRemovedTribeGuestList = PublishSubject.create();
  private PublishSubject<WebSocketError> onError = PublishSubject.create();

  public Room(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient) {
    this.webSocketConnection = webSocketConnection;
    this.webRTCClient = webRTCClient;
    this.state = STATE_NEW;

    initJsonToModel();
  }

  private void initJsonToModel() {
    jsonToModel = new JsonToModel();

    subscriptions.add(jsonToModel.onJoinRoom().subscribe(joinedRoom -> {
      for (TribeSession session : joinedRoom.getSessionList()) {
        webRTCClient.addPeerConnection(session, true);
      }
    }));

    subscriptions.add(jsonToModel.onReceivedOffer().subscribe(tribeOffer -> {
      webRTCClient.setRemoteDescription(tribeOffer.getSession(),
          tribeOffer.getSessionDescription());
    }));

    subscriptions.add(jsonToModel.onCandidate().subscribe(tribeCandidate -> {
      webRTCClient.addIceCandidate(tribeCandidate.getSession().getPeerId(),
          tribeCandidate.getIceCandidate());
    }));

    subscriptions.add(jsonToModel.onLeaveRoom()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeSession -> {
          webRTCClient.removePeerConnection(tribeSession);
        }));

    subscriptions.add(jsonToModel.onInvitedTribeGuestList().subscribe(onInvitedTribeGuestList));

    subscriptions.add(jsonToModel.onRemovedTribeGuestList().subscribe(onRemovedTribeGuestList));

    subscriptions.add(
        jsonToModel.onTribePeerMediaConfiguration().subscribe(tribePeerMediaConfiguration -> {
          webRTCClient.setMediaConfiguration(tribePeerMediaConfiguration);
        }));
  }

  public void initLocalStream(LocalPeerView localPeerView) {
    webRTCClient.setLocalStreamView(localPeerView);
  }

  public void connect(TribeLiveOptions options) {
    this.options = options;

    webRTCClient.setIceServers(this.options.getIceServers());

    webSocketConnection.connect(options.getWsUrl());

    subscriptions.add(webSocketConnection.onStateChanged().map(state -> {
      Timber.d( "On room state changed : " + state);

      if (state.equals(WebSocketConnection.STATE_CONNECTED)) {
        return STATE_CONNECTED;
      } else if (state.equals(WebSocketConnection.STATE_CONNECTING)) {
        return STATE_CONNECTING;
      } else if (state.equals(WebSocketConnection.STATE_DISCONNECTED)) {
        return STATE_DISCONNECTED;
      } else if (state.equals(WebSocketConnection.STATE_ERROR)) {
        return STATE_ERROR;
      }

      return STATE_CONNECTED;
    }).doOnNext(s -> {
      if (s.equals(STATE_CONNECTED)) {
        joinRoom();
      }
    }).subscribe(onRoomStateChanged));

    subscriptions.add(webSocketConnection.onMessage().subscribe(message -> {
      if (!webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
        Timber.e( "Got WebSocket message in non registered state.");
      }

      Timber.d( "On webSocketConnection message : " + message);

      jsonToModel.convert(message);
    }));
  }

  public void joinRoom() {
    Timber.d( "Joining room");

    webSocketConnection.send(getJoinPayload(options.getRoomId(), options.getTokenId()).toString());

    subscriptions.add(webRTCClient.onReadyToSendSdpOffer()
        .doOnError(Throwable::printStackTrace)
        .subscribe(tribeOffer -> {
          webSocketConnection.send(getSendSdpPayload(tribeOffer.getSession().getPeerId(),
              tribeOffer.getSessionDescription()).toString());
        }));

    subscriptions.add(webRTCClient.onReadyToSendSdpAnswer()
        .doOnError(Throwable::printStackTrace)
        .subscribe(tribeAnswer -> {
          webSocketConnection.send(getSendSdpPayload(tribeAnswer.getSession().getPeerId(),
              tribeAnswer.getSessionDescription()).toString());
        }));

    subscriptions.add(webRTCClient.onReceivedTribeCandidate().subscribe(tribeCandidate -> {
      JSONObject payload = getCandidatePayload(tribeCandidate.getSession().getPeerId(),
          tribeCandidate.getIceCandidate());
      webSocketConnection.send(payload.toString());
    }));

    subscriptions.add(webRTCClient.onRemotePeersChanged().doOnNext(rxRemotePeer -> {
      if (rxRemotePeer.changeType == ObservableRxHashMap.ADD) {
        onRemotePeerAdded.onNext(rxRemotePeer.item);
      } else if (rxRemotePeer.changeType == ObservableRxHashMap.REMOVE) {
        onRemotePeerRemoved.onNext(rxRemotePeer.item);
      } else if (rxRemotePeer.changeType == ObservableRxHashMap.UPDATE) {
        onRemotePeerUpdated.onNext(rxRemotePeer.item);
      }
    }).subscribe());

    subscriptions.add(webRTCClient.onReceivedDataChannelMessage()
        .doOnNext(
            message -> jsonToModel.convertDataChannel(message.getMessage(), message.getSession()))
        .subscribe());
  }

  public void leaveRoom() {
    if (subscriptions.hasSubscriptions()) subscriptions.clear();

    options = null;
    webRTCClient.dispose();
    webSocketConnection.disconnect(false);
  }

  public void sendToPeers(JSONObject obj) {
    webRTCClient.sendToPeers(obj);
  }

  public @RoomState String getState() {
    return state;
  }

  public TribeLiveOptions getOptions() {
    return options;
  }

  public static @WebSocketMessageType String getWebSocketMessageType(String a) {
    if (a.equals(MESSAGE_JOIN)) {
      return MESSAGE_JOIN;
    } else if (a.equals(MESSAGE_OFFER)) {
      return MESSAGE_OFFER;
    } else if (a.equals(MESSAGE_CANDIDATE)) {
      return MESSAGE_CANDIDATE;
    } else if (a.equals(MESSAGE_LEAVE)) {
      return MESSAGE_LEAVE;
    } else {
      return MESSAGE_NONE;
    }
  }

  private JSONObject getJoinPayload(String roomId, String tokenId) {
    JSONObject a = new JSONObject();
    jsonPut(a, "a", "join");
    JSONObject d = new JSONObject();
    jsonPut(d, "roomId", roomId);
    jsonPut(d, "bearer", tokenId);
    jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendSdpPayload(String id, SessionDescription sessionDescription) {
    JSONObject a = new JSONObject();
    jsonPut(a, "a", "exchangeSdp");
    JSONObject d = new JSONObject();
    jsonPut(d, "to", id);
    JSONObject sdp = new JSONObject();
    jsonPut(sdp, "type", sessionDescription.type.toString().toLowerCase());
    jsonPut(sdp, "sdp", sessionDescription.description);
    jsonPut(d, "sdp", sdp);
    jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getCandidatePayload(String peerId, IceCandidate iceCandidate) {
    JSONObject a = new JSONObject();
    jsonPut(a, "a", "exchangeCandidate");
    JSONObject d = new JSONObject();
    jsonPut(d, "to", peerId);
    JSONObject candidateJSON = new JSONObject();
    jsonPut(candidateJSON, "sdpMid", iceCandidate.sdpMid);
    jsonPut(candidateJSON, "sdpMLineIndex", iceCandidate.sdpMLineIndex);
    jsonPut(candidateJSON, "candidate", iceCandidate.sdp);
    jsonPut(d, "candidate", candidateJSON);
    jsonPut(a, "d", d);
    return a;
  }

  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<String> onRoomStateChanged() {
    return onRoomStateChanged;
  }

  public Observable<RemotePeer> onRemotePeerAdded() {
    return onRemotePeerAdded;
  }

  public Observable<RemotePeer> onRemotePeerRemoved() {
    return onRemotePeerRemoved;
  }

  public Observable<RemotePeer> onRemotePeerUpdated() {
    return onRemotePeerUpdated;
  }

  public Observable<List<TribeGuest>> onInvitedTribeGuestList() {
    return onInvitedTribeGuestList;
  }

  public Observable<List<TribeGuest>> onRemovedTribeGuestList() {
    return onRemovedTribeGuestList;
  }

  public Observable<WebSocketError> onError() {
    return onError;
  }
}
