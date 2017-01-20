package com.tribe.tribelivesdk.core;

import android.support.annotation.StringDef;

import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
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

    @StringDef({NEW, CONNECTING, CONNECTED, DISCONNECTED, ERROR})
    public @interface RoomState {}

    public static final String NEW = "new";
    public static final String CONNECTING = "connecting";
    public static final String CONNECTED = "connected";
    public static final String DISCONNECTED = "disconnected";
    public static final String ERROR = "error";

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

    public Room(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient, TribeLiveOptions options) {
        this.webSocketConnection = webSocketConnection;
        this.webRTCClient = webRTCClient;
        this.options = options;
        this.state = NEW;

        initJsonToModel();
    }

    private void initJsonToModel() {
        jsonToModel = new JsonToModel();

        subscriptions.add(
                jsonToModel.onNewPeers()
                        .subscribe(tribeNewPeers -> {
                            for (String peerId : tribeNewPeers.getPeerIds()) {
                                webRTCClient.addPeerConnection(peerId, true);
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

                    if (state.equals(WebSocketConnection.CONNECTED)) {
                        return CONNECTED;
                    } else if (state.equals(WebSocketConnection.CONNECTING)) {
                        return CONNECTING;
                    } else if (state.equals(WebSocketConnection.DISCONNECTED)) {
                        return DISCONNECTED;
                    } else if (state.equals(WebSocketConnection.ERROR)) {
                        return ERROR;
                    }

                    return CONNECTED;
                })
                .subscribe(onRoomStateChanged)
        );

        subscriptions.add(
                webSocketConnection.onMessage()
                        .subscribe(message -> {
                            if (!webSocketConnection.getState().equals(WebSocketConnection.CONNECTED)) {
                                LogUtil.e(getClass(), "Got WebSocket message in non registered state.");
                            }

                            LogUtil.d(getClass(), "On webSocketConnection message : " + message);

                            jsonToModel.convert(message);
                        })
        );
    }

    public void joinRoom(String roomId) {
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
        webRTCClient.leaveRoom();
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public @RoomState String getState() {
        return state;
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
        jsonPut(a, "a", "exchangeSdpFrom");
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
}
