package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.model.TribeAnswer;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeMediaStream;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribeSession;
import java.util.ArrayList;
import java.util.List;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 13/01/2017.
 */

public class TribePeerConnection {

  private TribeSession session;
  private PeerConnection peerConnection;
  private TribeSdpObserver sdpObserver;
  private TribePeerConnectionObserver peerConnectionObserver;
  private List<PeerConnection.IceServer> iceServerList;
  private List<IceCandidate> pendingIceCandidateList;

  // OBSERVABLE
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribeCandidate> onReceivedTribeCandidate = PublishSubject.create();
  private PublishSubject<TribeMediaStream> onReceivedMediaStream = PublishSubject.create();

  public TribePeerConnection(TribeSession session, PeerConnectionFactory peerConnectionFactory,
      List<PeerConnection.IceServer> iceServerList, boolean isOffer) {
    this.session = session;
    this.iceServerList = iceServerList;
    this.pendingIceCandidateList = new ArrayList<>();

    Timber.d("Initiating Peer Connection");
    init(peerConnectionFactory, isOffer);
    Timber.d("End initiating Peer Connection");
  }

  private void init(PeerConnectionFactory peerConnectionFactory, boolean isOffer) {
    sdpObserver = new TribeSdpObserver();
    peerConnectionObserver = new TribePeerConnectionObserver(isOffer);
    peerConnection =
        peerConnectionFactory.createPeerConnection(iceServerList, sdpObserver.constraints,
            peerConnectionObserver);
    sdpObserver.setPeerConnection(peerConnection);

    subscriptions.add(
        peerConnectionObserver.onShouldCreateOffer().subscribe(aVoid -> sdpObserver.createOffer()));

    subscriptions.add(peerConnectionObserver.onReceivedIceCandidate()
        .map(iceCandidate -> new TribeCandidate(session, iceCandidate))
        .subscribe(onReceivedTribeCandidate));

    subscriptions.add(
        peerConnectionObserver.onIceConnectionChanged().subscribe(iceConnectionState -> {

        }));

    subscriptions.add(peerConnectionObserver.onReceivedMediaStream()
        .map(mediaStream -> new TribeMediaStream(session, mediaStream))
        .subscribe(onReceivedMediaStream));
  }

  public void setRemoteDescription(SessionDescription sessionDescription) {
    if (sessionDescription == null) {
      Timber.e("Attempting to setRemoteDescription to null");
      return;
    }

    peerConnection.setRemoteDescription(sdpObserver,
        new SessionDescription(sessionDescription.type, sessionDescription.description));
  }

  public void addIceCandidate(final IceCandidate iceCandidate) {
    if (iceCandidate == null) {
      Timber.e("Attempting to add null ice candidate");
      return;
    }

    if (peerConnection == null) {
      Timber.e("Peer connection is null can't add iceCandidate");
      return;
    }

    if (peerConnection.getRemoteDescription() == null) {
      Timber.d("Adding iceCandidate to pending");
      pendingIceCandidateList.add(iceCandidate);
      return;
    }

    Timber.d("Adding iceCandidate : " + iceCandidate);
    peerConnection.addIceCandidate(iceCandidate);
  }

  public PeerConnection getPeerConnection() {
    return peerConnection;
  }

  public TribeSession getSession() {
    return session;
  }

  public void dispose() {
    subscriptions.clear();

    Timber.d("Disposing peer connection for peer : " + session.getPeerId());

    if (peerConnection != null) {
      Timber.d("Closing peer connection for peer : " + session.getPeerId());
      peerConnection.close();
      peerConnection = null;
      peerConnectionObserver = null;
    }

    Timber.d("End disposing for peer : " + session.getPeerId());
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribeOffer> onReadyToSendSdpOffer() {
    return sdpObserver.onReadyToSendSdpOffer().map(sessionDescription -> {
      TribeOffer offer = new TribeOffer(session, sessionDescription);
      return offer;
    });
  }

  public Observable<TribeAnswer> onReadyToSendSdpAnswer() {
    return sdpObserver.onReadyToSendSdpAnswer().map(sessionDescription -> {
      TribeAnswer answer = new TribeAnswer(session, sessionDescription);
      return answer;
    });
  }

  public Observable<TribeCandidate> onReceiveTribeCandidate() {
    return onReceivedTribeCandidate;
  }

  public Observable<TribeMediaStream> onReceivedMediaStream() {
    return onReceivedMediaStream;
  }
}
