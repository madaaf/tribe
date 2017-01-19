package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeChallenge;
import com.tribe.tribelivesdk.model.TribeNewPeers;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class JsonToModel {

    public JsonToModel() {}

    // OBSERVABLES
    private PublishSubject<TribeOffer> onReceivedOffer = PublishSubject.create();
    private PublishSubject<TribeChallenge> onReceivedChallenge = PublishSubject.create();
    private PublishSubject<TribeNewPeers> onNewPeers = PublishSubject.create();
    private PublishSubject<TribeCandidate> onCandidate = PublishSubject.create();

    private void convertToModel(String json) throws IOException {
        @WebSocketConnection.WebSocketMessageType String localWebSocketType = getWebSocketMessageFromJson(json);

        if (localWebSocketType == null) {
            LogUtil.e(getClass(), "WebSocket frame unhandled");
            return;
        }

        JSONObject object = null;
        try {
            object = new JSONObject(json);

            if (localWebSocketType.equals(WebSocketConnection.OFFER)) {
                JSONObject data = object.getJSONObject("d");
                LogUtil.d(getClass(), "Received challenge : " + data);
                JSONObject sdpJSON = data.getJSONObject("sdp");
                final SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(sdpJSON.getString("type")),
                        sdpJSON.getString("sdp"));
                onReceivedOffer.onNext(new TribeOffer(data.getString("from"), sdp));
            } else if (localWebSocketType.equals(WebSocketConnection.CANDIDATE)) {
                JSONObject data = object.getJSONObject("d");
                LogUtil.d(getClass(), "Exchange candidate : " + data.toString());
                JSONObject candidate = data.getJSONObject("candidate");
                TribeCandidate tribeCandidate = new TribeCandidate(
                        data.getString("from"),
                        new IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"), candidate.getString("candidate"))
                );
                onCandidate.onNext(tribeCandidate);
            } else if (localWebSocketType.equals(WebSocketConnection.LEAVE)) {

            } else if (localWebSocketType.equals(WebSocketConnection.JOIN)) {
                JSONObject r = object.getJSONObject("r");
                LogUtil.d(getClass(), "Join response received : " + r.toString());
                JSONArray jsonArray = r.getJSONArray("socketIds");
                List<String> peerIds = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    String peerId = jsonArray.getString(i);
                    peerIds.add(peerId);
                }

                onNewPeers.onNext(new TribeNewPeers(peerIds));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private @WebSocketConnection.WebSocketMessageType String getWebSocketMessageFromJson(String json) {
        JSONObject object = null;

        try {
            object = new JSONObject(json);

            if (object.has("a")) {
                String a = object.getString("a");
                return WebSocketConnection.getWebSocketMessageType(a);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void convert(String json) {
        try {
            convertToModel(json);
            return;
        } catch (IOException ex) {
            LogUtil.e(getClass(), ex.toString());
        }
    }

    /////////////////
    // OBSERVABLES //
    /////////////////

    public Observable<TribeNewPeers> onNewPeers() {
        return onNewPeers;
    }

    public Observable<TribeOffer> onReceivedOffer() {
        return onReceivedOffer;
    }

    public Observable<TribeCandidate> onCandidate() {
        return onCandidate;
    }
}
