package com.tribe.tribelivesdk.core;

import java.lang.ref.WeakReference;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

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
    constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
  }

  public void createOffer() {
    if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
      Timber.d("Can't create offer, peer connection is null");
      return;
    }

    if (creatingOffer) {
      Timber.d("Already creating offer");
      return;
    }

    Timber.d("Creating offer");
    peerConnectionWeakReference.get().createOffer(this, constraints);
  }

  @Override public void onCreateFailure(String msg) {
    Timber.d("Failed to create : " + msg);
    creatingOffer = false;
  }

  @Override public void onCreateSuccess(SessionDescription sdp) {
    creatingOffer = false;

    PeerConnection peerConnection;
    if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
      Timber.d("Can't create offer, peer connection is null");
      return;
    }

    peerConnection = peerConnectionWeakReference.get();

    Timber.d("On Create Success : " + sdp);

    String description = sdp.description;
    //com.tribe.tribelivesdk.core.MediaConstraints.removeVideoCodec(sdp.description,
    //    com.tribe.tribelivesdk.core.MediaConstraints.VIDEO_CODEC_VP9);

    SessionDescription localSessionDescription = new SessionDescription(sdp.type, description);

    Timber.d("onCreateSuccess: Peer connection setting local sdp " + sdp.type);
    peerConnection.setLocalDescription(this, localSessionDescription);
  }

  @Override public void onSetFailure(String msg) {
    Timber.d("Failed to set " + msg);
  }

  @Override public void onSetSuccess() {
    if (peerConnectionWeakReference == null || peerConnectionWeakReference.get() == null) {
      Timber.d("onSetSuccess: but peer connection is null");
      return;
    }

    Timber.d("onSetSuccess");

    PeerConnection peerConnection = peerConnectionWeakReference.get();

    if (peerConnection.getLocalDescription() != null
        && peerConnection.getRemoteDescription() == null) {
      Timber.d("onSetSuccess: created offer, setting local desc");

      onReadyToSendSdpOffer.onNext(peerConnection.getLocalDescription());
    } else if (peerConnection.getLocalDescription() == null
        && peerConnection.getRemoteDescription().type == SessionDescription.Type.OFFER) {
      Timber.d("onSetSuccess: setting remote desc");
      peerConnection.createAnswer(this, constraints);
    } else if (peerConnection.getLocalDescription() != null
        && peerConnection.getRemoteDescription() != null
        && peerConnection.getRemoteDescription().type == SessionDescription.Type.OFFER) {
      Timber.d("onSetSuccess: created answer, setting local desc");
      onReadyToSendSdpAnswer.onNext(peerConnection.getLocalDescription());
    }
  }

  protected void setPeerConnection(PeerConnection peerConnection) {
    if (peerConnection == null) {
      Timber.d("Attempt to setPeerConnection with null argument");
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
