package com.tribe.tribelivesdk.core;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringDef;
import android.support.v4.util.Pair;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.CpuUtils;
import com.tribe.tribelivesdk.util.DeviceUtils;
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
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 13/01/2017.
 */

public class WebRTCRoom {

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
      MESSAGE_INVITE_REMOVED, MESSAGE_ROLL_THE_DICE
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
  public static final String MESSAGE_UNLOCK_ROLL_DICE = "unlockRollTheDice";
  public static final String MESSAGE_UNLOCKED_ROLL_DICE = "unlockedRollTheDice";
  public static final String MESSAGE_MEDIA_CONFIGURATION = "isVideoEnabled";
  public static final String MESSAGE_INVITE_ADDED = "invited_guests";
  public static final String MESSAGE_INVITE_REMOVED = "removed_invited_guest";
  public static final String MESSAGE_ROLL_THE_DICE = "rollTheDice";
  public static final String MESSAGE_GAME = "game";

  public static final int DURATION = 60; // SECS

  private Context context;
  private WebSocketConnection webSocketConnection;
  private WebRTCClient webRTCClient;
  private TribeLiveOptions options;
  private @WebRTCRoom.RoomState String state;
  private JsonToModel jsonToModel;
  private boolean hasJoined = false;

  // OBSERVABLES
  // They stay when switching from Room to room
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  // They are removed when switching room
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private PublishSubject<TribeJoinRoom> onJoined = PublishSubject.create();
  private PublishSubject<String> onRoomStateChanged = PublishSubject.create();
  private PublishSubject<Void> onRollTheDiceReceived = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerAdded = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<RemotePeer> onRemotePeerUpdated = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onInvitedTribeGuestList = PublishSubject.create();
  private PublishSubject<List<TribeGuest>> onRemovedTribeGuestList = PublishSubject.create();
  private PublishSubject<WebSocketError> onError = PublishSubject.create();
  private PublishSubject<Void> onShouldLeaveRoom = PublishSubject.create();
  private PublishSubject<Void> onClearDrawReceived = PublishSubject.create();
  private PublishSubject<String> unlockRollTheDice = PublishSubject.create();
  private PublishSubject<String> onPointsDrawReceived = PublishSubject.create();
  private PublishSubject<String> test = PublishSubject.create();
  private PublishSubject<List<String>> onNewChallengeReceived = PublishSubject.create();
  private PublishSubject<List<String>> onNewDrawReceived = PublishSubject.create();
  private PublishSubject<String> unlockedRollTheDice = PublishSubject.create();
  private PublishSubject<WebSocketError> onRoomError = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, String>> onNewGame = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, String>> onStopGame = PublishSubject.create();
  private PublishSubject<RemotePeer> onReceivedStream = PublishSubject.create();

  public WebRTCRoom(Context context, WebSocketConnection webSocketConnection,
      WebRTCClient webRTCClient) {
    this.context = context;
    this.webSocketConnection = webSocketConnection;
    this.webRTCClient = webRTCClient;
    this.state = STATE_NEW;

    initJsonToModel();
  }

  private void initJsonToModel() {
    jsonToModel = new JsonToModel();

    persistentSubscriptions.add(jsonToModel.onJoinRoom().doOnNext(joinedRoom -> {
      onJoined.onNext(joinedRoom);

      initCPUInfo();

      if (!options.isShadowCall()) {
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
      }

      hasJoined = true;
    }).delay(1000, TimeUnit.MILLISECONDS).doOnNext(tribeJoinRoom -> {
      if (options != null && !options.isShadowCall()) {
        sendToPeers(webRTCClient.getJSONForNewPeer(webRTCClient.getMediaConfiguration()), false);
      }
    }).subscribe());

    persistentSubscriptions.add(
        jsonToModel.onRollTheDiceReceived().onBackpressureDrop().doOnNext(s -> {
          onRollTheDiceReceived.onNext(null);
        }).subscribe());

    persistentSubscriptions.add(
        jsonToModel.unlockRollTheDice().onBackpressureDrop().subscribe(unlockRollTheDice));

    persistentSubscriptions.add(jsonToModel.onNewChallengeReceived()
        .onBackpressureDrop()
        .subscribe(onNewChallengeReceived));

    persistentSubscriptions.add(
        jsonToModel.onNewDrawReceived().onBackpressureDrop().subscribe(onNewDrawReceived));

    persistentSubscriptions.add(
        jsonToModel.unlockedRollTheDice().onBackpressureDrop().subscribe(unlockedRollTheDice));

    persistentSubscriptions.add(
        jsonToModel.onClearDrawReceived().onBackpressureDrop().subscribe(onClearDrawReceived));

    persistentSubscriptions.add(
        jsonToModel.onPointsDrawReceived().onBackpressureDrop().subscribe(onPointsDrawReceived));

    persistentSubscriptions.add(jsonToModel.onReceivedOffer()
        .onBackpressureDrop()
        .subscribe(tribeOffer -> webRTCClient.setRemoteDescription(tribeOffer.getSession(),
            tribeOffer.getSessionDescription())));

    persistentSubscriptions.add(jsonToModel.onCandidate()
        .onBackpressureDrop()
        .subscribe(
            tribeCandidate -> webRTCClient.addIceCandidate(tribeCandidate.getSession().getPeerId(),
                tribeCandidate.getIceCandidate())));

    persistentSubscriptions.add(jsonToModel.onLeaveRoom()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeSession -> webRTCClient.removePeerConnection(tribeSession)));

    persistentSubscriptions.add(jsonToModel.onError()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(error -> {
          onRoomError.onNext(error);
        }));

    persistentSubscriptions.add(
        jsonToModel.onInvitedTribeGuestList().subscribe(onInvitedTribeGuestList));

    persistentSubscriptions.add(
        jsonToModel.onRemovedTribeGuestList().subscribe(onRemovedTribeGuestList));

    persistentSubscriptions.add(jsonToModel.onTribePeerMediaConfiguration()
        .onBackpressureDrop()
        .subscribe(tribePeerMediaConfiguration -> webRTCClient.setRemoteMediaConfiguration(
            tribePeerMediaConfiguration)));

    persistentSubscriptions.add(jsonToModel.onTribeMediaConstraints()
        .onBackpressureDrop()
        .filter(tribeMediaConstraints -> hasJoined)
        .subscribe(
            tribeMediaConstraints -> webRTCClient.updateMediaConstraints(tribeMediaConstraints)));

    persistentSubscriptions.add(jsonToModel.onShouldSwitchRemoteMediaMode()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeMediaConfiguration -> webRTCClient.setRemoteMediaConfiguration(
            tribeMediaConfiguration)));

    persistentSubscriptions.add(jsonToModel.onNewGame()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNewGame));

    persistentSubscriptions.add(jsonToModel.onStopGame()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onStopGame));
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
        .onBackpressureBuffer()
        .doOnError(Throwable::printStackTrace)
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

    webSocketConnection.send(
        getJoinPayload(options.getRoomId(), options.getTokenId(), options.getOrientation(),
            options.isFrontCamera()).toString());

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
        sendToPeer(rxRemotePeer.item,
            webRTCClient.getJSONForNewPeer(webRTCClient.getMediaConfiguration()), false);
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

    tempSubscriptions.add(webRTCClient.onIceGatheringChanged().subscribe(iceGatheringState -> {
      JSONObject payload = getSendMessageIceGatheringComplete();
      webSocketConnection.send(payload.toString());
    }));

    tempSubscriptions.add(webRTCClient.onReceivedStream().subscribe(onReceivedStream));

    tempSubscriptions.add(webRTCClient.isLocalFreeze().subscribe(freeze -> sendFreeze()));
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
    if (webSocketConnection != null) webSocketConnection.disconnect(false);
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

  public void sendOrientation(int orientation, boolean frontFacing) {
    if (webSocketConnection == null) return;

    webSocketConnection.send(getSendOrientation(orientation, frontFacing).toString());
  }

  private void sendCpu(int cpu) {
    if (webSocketConnection == null) return;

    webSocketConnection.send(getSendCPU(cpu).toString());
  }

  private void sendFreeze() {
    if (webSocketConnection == null) return;

    webSocketConnection.send(getSendFreeze().toString());
  }

  public void sendToUser(String userId, JSONObject obj, boolean isAppMessage) {

    for (TribePeerConnection tpc : webRTCClient.getPeers()) {
      if (tpc != null &&
          !tpc.getSession().getPeerId().equals(TribeSession.PUBLISHER_ID) &&
          tpc.getSession().getUserId().equals(userId)) {

        webSocketConnection.send(
            getSendMessagePayload(tpc.getSession().getPeerId(), obj, isAppMessage).toString());
      }
    }
  }

  public void sendToPeer(RemotePeer remotePeer, JSONObject obj, boolean isAppMessage) {
    if (webSocketConnection == null) return;

    if (remotePeer != null &&
        !remotePeer.getSession().getPeerId().equals(TribeSession.PUBLISHER_ID)) {
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

  private JSONObject getJoinPayload(String roomId, String tokenId, int orientation,
      boolean frontFacing) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "join");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "roomId", roomId);
    JsonUtils.jsonPut(d, "bearer", tokenId);
    JsonUtils.jsonPut(d, "platform", "Android");
    JsonUtils.jsonPut(d, "orientation", orientation);
    JsonUtils.jsonPut(d, "camera", frontFacing ? "front" : "back");

    String deviceName = DeviceUtils.getDeviceName();
    String appVersion = Integer.toString(DeviceUtils.getVersionCode(context));
    String releaseOS = Build.VERSION.RELEASE;
    String networkType = DeviceUtils.getNetworkType(context);

    JsonUtils.jsonPut(d, "deviceName", deviceName);
    JsonUtils.jsonPut(d, "networkType", networkType);
    JsonUtils.jsonPut(d, "version", releaseOS);
    JsonUtils.jsonPut(d, "appVersion", appVersion);

    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendOrientation(int orientation, boolean frontFacing) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "orientationChange");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "orientation", orientation);
    JsonUtils.jsonPut(d, "camera", frontFacing ? "front" : "back");
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

  private JSONObject getSendMessageIceGatheringComplete() {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "exchangeCandidate");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "to", TribeSession.PUBLISHER_ID);
    JSONObject candidate = new JSONObject();
    JsonUtils.jsonPut(candidate, "completed", true);
    JsonUtils.jsonPut(d, "candidate", candidate);
    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendCPU(int cpu) {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "eventCpu");
    JSONObject d = new JSONObject();
    JsonUtils.jsonPut(d, "cpuUsed", cpu);
    JsonUtils.jsonPut(a, "d", d);
    return a;
  }

  private JSONObject getSendFreeze() {
    JSONObject a = new JSONObject();
    JsonUtils.jsonPut(a, "a", "eventFreeze");
    return a;
  }

  private void initCPUInfo() {
    tempSubscriptions.add(Observable.interval(0, DURATION, TimeUnit.SECONDS)
        .onBackpressureDrop()
        .observeOn(Schedulers.io())
        .map(aLong -> {
          int[] cpuUsage = CpuUtils.getCpuUsageStatistic();
          return cpuUsage[0];
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aInt -> sendCpu(aInt)));
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribeJoinRoom> onJoined() {
    return onJoined;
  }

  public Observable<Void> onRollTheDiceReceived() {
    return onRollTheDiceReceived;
  }

  public Observable<String> onRoomStateChanged() {
    return onRoomStateChanged;
  }

  public Observable<String> test() {
    return test;
  }

  public Observable<String> unlockRollTheDice() {
    return unlockRollTheDice;
  }

  public Observable<String> unlockedRollTheDice() {
    return unlockedRollTheDice;
  }

  public Observable<RemotePeer> onRemotePeerAdded() {
    return onRemotePeerAdded;
  }

  public Observable<RemotePeer> onRemotePeerRemoved() {
    return onRemotePeerRemoved;
  }

  public Observable<WebSocketError> onError() {
    return onError;
  }

  public Observable<Void> onShouldLeaveRoom() {
    return onShouldLeaveRoom;
  }

  public Observable<Void> onClearDrawReceived() {
    return onClearDrawReceived;
  }

  public Observable<WebSocketError> onRoomError() {
    return onRoomError;
  }

  public Observable<RemotePeer> onReceivedStream() {
    return onReceivedStream;
  }

  public Observable<Pair<TribeSession, String>> onNewGame() {
    return onNewGame;
  }

  public Observable<Pair<TribeSession, String>> onStopGame() {
    return onStopGame;
  }

  public Observable<String> onPointsDrawReceived() {
    return onPointsDrawReceived;
  }

  public Observable<List<String>> onNewChallengeReceived() {
    return onNewChallengeReceived;
  }

  public Observable<List<String>> onNewDrawReceived() {
    return onNewDrawReceived;
  }

  public Observable<JSONObject> onGameMessage() {
    return jsonToModel.onGameMessage();
  }
}
