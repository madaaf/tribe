package com.tribe.tribelivesdk.core;

import android.support.annotation.StringDef;

import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.LogUtil;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.PeerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 13/01/2017.
 */

public class Room {

    @StringDef({STATE_NEW, STATE_CONNECTING, STATE_CONNECTED, STATE_READY, STATE_DISCONNECTED, STATE_ERROR})
    public @interface RoomState {}

    public static final String STATE_NEW = "new";
    public static final String STATE_CONNECTING = "connecting";
    public static final String STATE_CONNECTED = "connected";
    public static final String STATE_READY = "ready";
    public static final String STATE_DISCONNECTED = "disconnected";
    public static final String STATE_ERROR = "error";

    @StringDef({MESSAGE_ERROR, MESSAGE_AUTH, MESSAGE_JOIN, MESSAGE_OFFER, MESSAGE_CANDIDATE, MESSAGE_LEAVE})
    public @interface WebSocketMessageType {}

    public static final String MESSAGE_ERROR = "error";
    public static final String MESSAGE_AUTH = "authR";
    public static final String MESSAGE_JOIN = "joinR";
    public static final String MESSAGE_OFFER = "exchangeSdp";
    public static final String MESSAGE_CANDIDATE = "exchangeCandidate";
    public static final String MESSAGE_LEAVE = "leave";

    private String roomId;
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
    private PublishSubject<WebSocketError> onError = PublishSubject.create();

    public Room(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient, TribeLiveOptions options) {
        this.webSocketConnection = webSocketConnection;
        this.webRTCClient = webRTCClient;
        this.options = options;
        this.state = STATE_NEW;

        initJsonToModel();
    }

    private void initJsonToModel() {
        jsonToModel = new JsonToModel();

        subscriptions.add(
                jsonToModel.onJoinRoom()
                        .subscribe(joinedRoom -> {
                            for (TribeSession session : joinedRoom.getSessionList()) {
                                webRTCClient.addPeerConnection(session, true);
                            }
                        })
        );

        subscriptions.add(
                jsonToModel.onReceivedOffer()
                        .subscribe(tribeOffer -> {
                            webRTCClient.setRemoteDescription(tribeOffer.getId(), tribeOffer.getSessionDescription());
                        })
        );

        subscriptions.add(
                jsonToModel.onCandidate()
                        .subscribe(tribeCandidate -> {
                            webRTCClient.addIceCandidate(tribeCandidate.getId(), tribeCandidate.getIceCandidate());
                        })
        );
    }

    public void initLocalStream(PeerView peerView) {
        webRTCClient.setLocalStreamView(peerView, webRTCClient.getPeerConnectionFactory());
    }

    public void connect() {
        webSocketConnection.connect(options.getWsUrl());

        subscriptions.add(webSocketConnection.onStateChanged()
                .map(state -> {
                    LogUtil.d(getClass(), "On room state changed : " + state);

                    if (state.equals(WebSocketConnection.STATE_CONNECTED)) {
                        return STATE_CONNECTED;
                    } else if (state.equals(WebSocketConnection.STATE_READY)) {
                        return STATE_READY;
                    } else if (state.equals(WebSocketConnection.STATE_CONNECTING)) {
                        return STATE_CONNECTING;
                    } else if (state.equals(WebSocketConnection.STATE_DISCONNECTED)) {
                        return STATE_DISCONNECTED;
                    } else if (state.equals(WebSocketConnection.STATE_ERROR)) {
                        return STATE_ERROR;
                    }

                    return STATE_CONNECTED;
                })
                .doOnNext(newState -> {
                    if (newState == STATE_CONNECTED) webSocketConnection.send(getAuthPayload(options.getTokenId()).toString());
                })
                .subscribe(onRoomStateChanged)
        );

        subscriptions.add(
                webSocketConnection.onMessage()
                        .subscribe(message -> {
                            if (!webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
                                LogUtil.e(getClass(), "Got WebSocket message in non registered state.");
                            }

                            LogUtil.d(getClass(), "On webSocketConnection message : " + message);

                            jsonToModel.convert(message);
                        })
        );
    }

    public void joinRoom(String roomId) {
        this.roomId = roomId;

        webSocketConnection.send(getJoinPayload(roomId).toString());

        subscriptions.add(
                webRTCClient.onReadyToSendSdpOffer()
                        .doOnError(Throwable::printStackTrace)
                        .subscribe(tribeOffer -> {
                            webSocketConnection.send(
                                    getSendSdpPayload(
                                            tribeOffer.getId(), tribeOffer.getSessionDescription()
                                    ).toString()
                            );
                        })
        );

        subscriptions.add(
                webRTCClient.onReadyToSendSdpAnswer()
                        .doOnError(Throwable::printStackTrace)
                        .subscribe(tribeAnswer -> {
                            webSocketConnection.send(
                                    getSendSdpPayload(
                                            tribeAnswer.getId(), tribeAnswer.getSessionDescription()
                                    ).toString()
                            );
                        })
        );

        subscriptions.add(
                webRTCClient.onReceivedTribeCandidate()
                        .subscribe(tribeCandidate -> {
                            List<IceCandidate> candidateList = tribeCandidate.getIceCandidateList();

                            for (IceCandidate iceCandidate : candidateList) {
                                JSONObject payload = getCandidatePayload(tribeCandidate.getId(), iceCandidate);
                                webSocketConnection.send(payload.toString());
                            }
                        })
        );

        subscriptions.add(
                webRTCClient.onRemotePeersChanged()
                        .doOnNext(rxRemotePeer -> {
                            if (rxRemotePeer.changeType == ObservableRxHashMap.ADD) {
                                onRemotePeerAdded.onNext(rxRemotePeer.item);
                            } else if (rxRemotePeer.changeType == ObservableRxHashMap.REMOVE) {
                                onRemotePeerRemoved.onNext(rxRemotePeer.item);
                            } else if (rxRemotePeer.changeType == ObservableRxHashMap.UPDATE) {
                                onRemotePeerUpdated.onNext(rxRemotePeer.item);
                            }
                        })
                        .subscribe()
        );
    }

    public void leaveRoom() {
        roomId = null;
        webRTCClient.leaveRoom();
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public @RoomState String getState() {
        return state;
    }

    public static @WebSocketMessageType String getWebSocketMessageType(String a) {
        if (a.equals(MESSAGE_AUTH)) {
            return MESSAGE_AUTH;
        } else if (a.equals(MESSAGE_JOIN)) {
            return MESSAGE_JOIN;
        } else if (a.equals(MESSAGE_OFFER)) {
            return MESSAGE_OFFER;
        } else if (a.equals(MESSAGE_CANDIDATE)) {
            return MESSAGE_CANDIDATE;
        } else {
            return MESSAGE_LEAVE;
        }
    }

    private JSONObject getAuthPayload(String tokenId) {
        JSONObject a = new JSONObject();
        jsonPut(a, "a", "auth");
        JSONObject d = new JSONObject();
        jsonPut(d, "bearer", tokenId);
        jsonPut(a, "d", d);
        return a;
    }

    private JSONObject getJoinPayload(String roomId) {
        JSONObject a = new JSONObject();
        jsonPut(a, "a", "join");
        JSONObject d = new JSONObject();
        jsonPut(d, "roomId", roomId);
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

    public Observable<WebSocketError> onError() {
        return onError;
    }
}