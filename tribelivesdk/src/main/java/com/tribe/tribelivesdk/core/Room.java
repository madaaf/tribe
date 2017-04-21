package com.tribe.tribelivesdk.core;

import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
      MESSAGE_ERROR, MESSAGE_JOIN, MESSAGE_OFFER, MESSAGE_CANDIDATE, MESSAGE_LEAVE,
      MESSAGE_MEDIA_CONSTRAINTS, MESSAGE_MESSAGE, MESSAGE_NONE, MESSAGE_LOCAL_SWITCH_MODE,
      MESSAGE_REMOTE_SWITCH_MODE, MESSAGE_APP, MESSAGE_MEDIA_CONFIGURATION, MESSAGE_INVITE_ADDED,
      MESSAGE_INVITE_REMOVED
  }) public @interface WebSocketMessageType {
  }

  public static final String MESSAGE_ERROR = "e";
  public static final String MESSAGE_JOIN = "joinR";
  public static final String MESSAGE_OFFER = "eventExchangeSdp";
  public static final String MESSAGE_CANDIDATE = "eventExchangeCandidate";
  public static final String MESSAGE_LEAVE = "eventLeave";
  public static final String MESSAGE_MESSAGE = "eventMessage";
  public static final String MESSAGE_MEDIA_CONSTRAINTS = "eventUserMediaConfiguration";
  public static final String MESSAGE_LOCAL_SWITCH_MODE = "eventSetLocalAudioVideoMode";
  public static final String MESSAGE_REMOTE_SWITCH_MODE = "eventSetRemoteAudioVideoMode";
  public static final String MESSAGE_NONE = "none";
  public static final String MESSAGE_APP = "app";
  public static final String MESSAGE_MEDIA_CONFIGURATION = "isVideoEnabled";
  public static final String MESSAGE_INVITE_ADDED = "invited_guests";
  public static final String MESSAGE_INVITE_REMOVED = "removed_invited_guest";

  private WebSocketConnection webSocketConnection;
  private WebRTCClient webRTCClient;
  private TribeLiveOptions options;
  private @Room.RoomState String state;
  private JsonToModel jsonToModel;
  private boolean hasJoined = false;

  // OBSERVABLES
  // They stay when switching from Room to room
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  // They are removed when switching room
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private PublishSubject<TribeJoinRoom> onJoined = PublishSubject.create();
  private PublishSubject<String> onRoomStateChanged = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerAdded = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerUpdated = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onInvitedTribeGuestList = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onRemovedTribeGuestList = PublishSubject.create();
  private PublishSubject<WebSocketError> onError = PublishSubject.create();
  private PublishSubject<Void> onShouldLeaveRoom = PublishSubject.create();

  public Room(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient) {
    this.webSocketConnection = webSocketConnection;
    this.webRTCClient = webRTCClient;
    this.state = STATE_NEW;

    initJsonToModel();
  }

  private void initJsonToModel() {
    jsonToModel = new JsonToModel();

    persistentSubscriptions.add(jsonToModel.onJoinRoom()
        .doOnNext(joinedRoom -> {
          onJoined.onNext(joinedRoom);

          if (options.getRoutingMode().equals(TribeLiveOptions.P2P)) {
            for (TribeSession session : joinedRoom.getSessionList()) {
              webRTCClient.addPeerConnection(session, true);
            }
          } else {
            for (TribeSession session : joinedRoom.getSessionList()) {
              webRTCClient.addPeerConnection(session, false);
            }

            webRTCClient.addPeerConnection(
                new TribeSession(TribeSession.PUBLISHER_ID, TribeSession.PUBLISHER_ID), true);
          }

          hasJoined = true;
        })
        .delay(1000, TimeUnit.MILLISECONDS)
        .doOnNext(tribeJoinRoom -> sendToPeers(
            webRTCClient.getJSONMedia(webRTCClient.getMediaConfiguration()), false))
        .subscribe());

    persistentSubscriptions.add(jsonToModel.onReceivedOffer()
        .subscribe(tribeOffer -> webRTCClient.setRemoteDescription(tribeOffer.getSession(),
            tribeOffer.getSessionDescription())));

    persistentSubscriptions.add(jsonToModel.onCandidate()
        .subscribe(
            tribeCandidate -> webRTCClient.addIceCandidate(tribeCandidate.getSession().getPeerId(),
                tribeCandidate.getIceCandidate())));

    persistentSubscriptions.add(jsonToModel.onLeaveRoom()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeSession -> webRTCClient.removePeerConnection(tribeSession)));

    persistentSubscriptions.add(
        jsonToModel.onInvitedTribeGuestList().subscribe(onInvitedTribeGuestList));

    persistentSubscriptions.add(
        jsonToModel.onRemovedTribeGuestList().subscribe(onRemovedTribeGuestList));

    persistentSubscriptions.add(jsonToModel.onTribePeerMediaConfiguration()
        .subscribe(tribePeerMediaConfiguration -> webRTCClient.setRemoteMediaConfiguration(
            tribePeerMediaConfiguration)));

    persistentSubscriptions.add(jsonToModel.onTribeMediaConstraints()
        .filter(tribeMediaConstraints -> hasJoined)
        .subscribe(tribeMediaConstraints -> {
          webRTCClient.updateMediaConstraints(tribeMediaConstraints);
        }));

    persistentSubscriptions.add(jsonToModel.onShouldSwitchRemoteMediaMode()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeMediaConfiguration -> webRTCClient.setRemoteMediaConfiguration(
            tribeMediaConfiguration)));
  }

  public void initLocalStream(LocalPeerView localPeerView) {
    webRTCClient.setLocalStreamView(localPeerView);
  }

  public void connect(TribeLiveOptions options) {
    if (webSocketConnection == null) return;

    this.options = options;

    jsonToModel.setOptions(options);

    webRTCClient.setOptions(this.options);
    webRTCClient.setIceServers(this.options.getIceServers());

    webSocketConnection.setHeaders(options.getHeaders());
    webSocketConnection.connect(options.getWsUrl());

    tempSubscriptions.add(webSocketConnection.onStateChanged().map(state -> {
      Timber.d("On room state changed : " + state);

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
      } else if (s.equals(STATE_DISCONNECTED)) {
        onShouldLeaveRoom.onNext(null);
      }
    }).subscribe(onRoomStateChanged));

    tempSubscriptions.add(webSocketConnection.onMessage()
        .onErrorResumeNext(throwable -> Observable.just(""))
        .subscribe(message -> {
          if (!webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
            Timber.d("Got WebSocket message in non registered state.");
          }

          Timber.d("On webSocketConnection message : " + message);

          jsonToModel.convert(message);
        }));
  }

  public void joinRoom() {
    if (webSocketConnection == null) return;
    Timber.d("Joining room");

    webRTCClient.initSubscriptions();

    webSocketConnection.send(getJoinPayload(options.getRoomId(), options.getTokenId()).toString());

    tempSubscriptions.add(webRTCClient.onReadyToSendSdpOffer()
        .doOnError(Throwable::printStackTrace)
        .subscribe(tribeOffer -> {
          webSocketConnection.send(getSendSdpPayload(tribeOffer.getSession().getPeerId(),
              tribeOffer.getSessionDescription()).toString());
        }));

    tempSubscriptions.add(webRTCClient.onReadyToSendSdpAnswer()
        .doOnError(Throwable::printStackTrace)
        .subscribe(tribeAnswer -> {
          webSocketConnection.send(getSendSdpPayload(tribeAnswer.getSession().getPeerId(),
              tribeAnswer.getSessionDescription()).toString());
        }));

    tempSubscriptions.add(webRTCClient.onReceivedTribeCandidate().subscribe(tribeCandidate -> {
      JSONObject payload = getCandidatePayload(tribeCandidate.getSession().getPeerId(),
          tribeCandidate.getIceCandidate());
      webSocketConnection.send(payload.toString());
    }));

    tempSubscriptions.add(webRTCClient.onRemotePeersChanged().doOnNext(rxRemotePeer -> {
      if (rxRemotePeer.changeType == ObservableRxHashMap.ADD) {
        onRemotePeerAdded.onNext(rxRemotePeer.item);
      } else if (rxRemotePeer.changeType == ObservableRxHashMap.REMOVE) {
        onRemotePeerRemoved.onNext(rxRemotePeer.item);
      } else if (rxRemotePeer.changeType == ObservableRxHashMap.UPDATE) {
        onRemotePeerUpdated.onNext(rxRemotePeer.item);
      }
    }).subscribe());

    tempSubscriptions.add(webRTCClient.onSendToPeers().subscribe(jsonObject -> {
      sendToPeers(jsonObject, false);
    }));
  }

  public void leaveRoom() {
    dispose(true);
    if (persistentSubscriptions.hasSubscriptions()) persistentSubscriptions.clear();
  }

  public void jump() {
    dispose(false);
  }

  private void dispose(boolean shouldDisposeLocal) {
    hasJoined = false;

    if (tempSubscriptions.hasSubscriptions()) tempSubscriptions.clear();

    options = null;
    webSocketConnection.disconnect(false);
    webSocketConnection = null;
    webRTCClient.dispose(shouldDisposeLocal);
  }

  public void sendToPeers(JSONObject obj, boolean isAppMessage) {
    if (webSocketConnection == null) return;

    for (TribePeerConnection tpc : webRTCClient.getPeers()) {
      if (tpc != null && !tpc.getSession().getPeerId().equals(TribeSession.PUBLISHER_ID)) {
        webSocketConnection.send(
            getSendMessagePayload(tpc.getSession().getPeerId(), obj, isAppMessage).toString());
      }
    }
  }

  public void sendToPeer(RemotePeer remotePeer, JSONObject obj, boolean isAppMessage) {
    if (webSocketConnection == null) return;

    if (remotePeer != null && !remotePeer.getSession()
        .getPeerId()
        .equals(TribeSession.PUBLISHER_ID)) {
      webSocketConnection.send(
          getSendMessagePayload(remotePeer.getSession().getPeerId(), obj, isAppMessage).toString());
    }
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
    } else if (a.equals(MESSAGE_MEDIA_CONSTRAINTS)) {
      return MESSAGE_MEDIA_CONSTRAINTS;
    } else if (a.equals(MESSAGE_LOCAL_SWITCH_MODE)) {
      return MESSAGE_LOCAL_SWITCH_MODE;
    } else if (a.equals(MESSAGE_REMOTE_SWITCH_MODE)) {
      return MESSAGE_REMOTE_SWITCH_MODE;
    } else if (a.equals(MESSAGE_MESSAGE)) {
      return MESSAGE_MESSAGE;
    } else {
      return MESSAGE_NONE;
    }
  }

  private JSONObject getJoinPayload(String roomId, String tokenId) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "join");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "roomId", roomId);
    JsonUtils.jsonPut(d, "bearer", tokenId);
    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendSdpPayload(String id, SessionDescription sessionDescription) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "exchangeSdp");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "to", id);
    JSONObject sdp = new JSONObject();
    JsonUtils.jsonPut(sdp, "type", sessionDescription.type.toString().toLowerCase());
    JsonUtils.jsonPut(sdp, "sdp", sessionDescription.description);
    JsonUtils.jsonPut(d, "sdp", sdp);
    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getCandidatePayload(String peerId, IceCandidate iceCandidate) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "exchangeCandidate");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "to", peerId);
    JSONObject candidateJSON = new JSONObject();
    JsonUtils.jsonPut(candidateJSON, "sdpMid", iceCandidate.sdpMid);
    JsonUtils.jsonPut(candidateJSON, "sdpMLineIndex", iceCandidate.sdpMLineIndex);
    JsonUtils.jsonPut(candidateJSON, "candidate", iceCandidate.sdp);
    JsonUtils.jsonPut(d, "candidate", candidateJSON);
    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendMessagePayload(String peerId, JSONObject message,
      boolean isAppMessage) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "sendMessage");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "to", peerId);

    if (isAppMessage) {
      JSONObject appJson = new JSONObject();
      JsonUtils.jsonPut(appJson, "app", message);
      JsonUtils.jsonPut(d, "message", appJson);
    } else {
      JsonUtils.jsonPut(d, "message", message);
    }

    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribeJoinRoom> onJoined() {
    return onJoined;
  }

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

  public Observable<Void> onShouldLeaveRoom() {
    return onShouldLeaveRoom;
  }
}
