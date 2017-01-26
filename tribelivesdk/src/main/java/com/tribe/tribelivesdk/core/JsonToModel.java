package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
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
    private PublishSubject<TribeJoinRoom> onJoinRoom = PublishSubject.create();
    private PublishSubject<TribeCandidate> onCandidate = PublishSubject.create();
    private PublishSubject<WebSocketError> onError = PublishSubject.create();

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
                onReceivedOffer.onNext(new TribeOffer(data.getString("from"), sdp));
            } else if (localWebSocketType.equals(Room.MESSAGE_CANDIDATE)) {
                JSONObject data = object.getJSONObject("d");
                LogUtil.d(getClass(), "Exchange candidate : " + data.toString());
                JSONObject candidate = data.getJSONObject("candidate");
                TribeCandidate tribeCandidate = new TribeCandidate(
                        data.getString("from"),
                        new IceCandidate(candidate.getString("sdpMid"), candidate.getInt("sdpMLineIndex"), candidate.getString("candidate"))
                );
                onCandidate.onNext(tribeCandidate);
            } else if (localWebSocketType.equals(Room.MESSAGE_LEAVE)) {

            } else if (localWebSocketType.equals(Room.MESSAGE_JOIN)) {
                JSONObject r = object.getJSONObject("r");
                LogUtil.d(getClass(), "Join response received : " + r.toString());
                JSONArray jsonArray = r.getJSONArray("sessions");
                int roomSize = r.getInt("roomSize");
                List<TribeSession> sessionList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject session = jsonArray.getJSONObject(i);
                    sessionList.add(new TribeSession(session.getString("socketId"), session.getString("userId")));
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
}
