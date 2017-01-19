package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.util.LogUtil;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.subjects.PublishSubject;

public class TribeSdpObserver implements SdpObserver {

    protected final MediaConstraints constraints;
    private boolean creatingOffer = false;
    private WeakReference<PeerConnection> peerConnectionWeakReference;

    private PublishSubject<SessionDescription> onReadyToSendSdpOffer = PublishSubject.create();
    private PublishSubject<SessionDescription> onReadyToSendSdpAnswer = PublishSubject.create();

    public TribeSdpObserver() {
        constraints = new MediaConstraints();
        setConstraints();
    }

    private void setConstraints() {
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    public void createOffer() {
        if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
            LogUtil.e(getClass(), "Can't create offer, peer connection is null");
            return;
        }

        if (creatingOffer) {
            LogUtil.d(getClass(), "Already creating offer");
            return;
        }

        LogUtil.d(getClass(), "Creating offer");
        peerConnectionWeakReference.get().createOffer(this, constraints);
    }

    @Override
    public void onCreateFailure(String msg) {
        LogUtil.e(getClass(), "Failed to create : " + msg);
        creatingOffer = false;
    }

    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        creatingOffer = false;

        PeerConnection peerConnection;
        if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
            LogUtil.e(getClass(), "Can't create offer, peer connection is null");
            return;
        }

        peerConnection = peerConnectionWeakReference.get();

        LogUtil.d(getClass(), "On Create Success : " + sdp);
        SessionDescription localSessionDescription = new SessionDescription(sdp.type, sdp.description);
        LogUtil.d(getClass(), "onCreateSuccess: Peer connection setting local sdp " + sdp.type);
        peerConnection.setLocalDescription(this, localSessionDescription);
    }

    @Override
    public void onSetFailure(String msg) {
        LogUtil.d(getClass(), "Failed to set " + msg);
    }

    @Override
    public void onSetSuccess() {
        if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
            LogUtil.e(getClass(), "onSetSuccess: but peer connection is null");
            return;
        }

        LogUtil.d(getClass(), "onSetSuccess");

        PeerConnection peerConnection = peerConnectionWeakReference.get();

        if (peerConnection.getLocalDescription() != null && peerConnection.getRemoteDescription() == null) {
            LogUtil.d(getClass(), "onSetSuccess: created offer, setting local desc");
            onReadyToSendSdpOffer.onNext(peerConnection.getLocalDescription());
        } else if (peerConnection.getLocalDescription() == null && peerConnection.getRemoteDescription().type == SessionDescription.Type.OFFER) {
            LogUtil.d(getClass(), "onSetSuccess: setting remote desc");
            peerConnection.createAnswer(this, constraints);
        } else if (peerConnection.getLocalDescription() != null
                && peerConnection.getRemoteDescription() != null && peerConnection.getRemoteDescription().type == SessionDescription.Type.OFFER) {
            LogUtil.d(getClass(), "onSetSuccess: created answer, setting local desc");
            onReadyToSendSdpAnswer.onNext(peerConnection.getLocalDescription());
        }

//        return;
//
//            if (localPeerConnection.getLocalDescription() == null) {
//                LogUtil.print(SDPObserver.class, "onSetSuccess: creating answer");
//                localPeerConnection.createAnswer(SDPObserver.this, constraints);
//                return;
//            }
//            LogUtil.print(SDPObserver.class, "onSetSuccess: created answer, setting local desc");
//        } while (callbacks == null);
//        callbacks.readyToSendSdpAnswer(localPeerConnection.getLocalDescription());
    }

    protected void setPeerConnection(PeerConnection peerConnection) {
        if (peerConnection == null) {
            LogUtil.d(getClass(), "Attempt to setPeerConnection with null argument");
            return;
        }

        peerConnectionWeakReference = new WeakReference(peerConnection);
    }

    public void dropPeerConnection() {
        peerConnectionWeakReference.clear();
    }

    /////////////////
    // OBSERVABLES //
    /////////////////

    public Observable<SessionDescription> onReadyToSendSdpOffer() {
        return onReadyToSendSdpOffer;
    }

    public Observable<SessionDescription> onReadyToSendSdpAnswer() {
        return onReadyToSendSdpAnswer;
    }
}
