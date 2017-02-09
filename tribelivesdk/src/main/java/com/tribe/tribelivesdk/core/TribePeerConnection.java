package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.model.TribeAnswer;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeMediaStream;
import com.tribe.tribelivesdk.model.TribeMessageDataChannel;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.LogUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 13/01/2017.
 */

public class TribePeerConnection {

  private static final String DATA_CHANNEL_PROTOCOL = "tribe-v3";
  private static final String DATA_CHANNEL_META = "meta";

  private TribeSession session;
  private PeerConnection peerConnection;
  private TribeSdpObserver sdpObserver;
  private TribePeerConnectionObserver peerConnectionObserver;
  private TribeDataChannelObserver dataChannelObserver;
  private List<PeerConnection.IceServer> iceServerList;
  private List<IceCandidate> pendingIceCandidateList;
  private DataChannel dataChannel;

  // OBSERVABLE
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribeCandidate> onReceivedTribeCandidate = PublishSubject.create();
  private PublishSubject<TribeMediaStream> onReceivedMediaStream = PublishSubject.create();
  private PublishSubject<Void> onDataChannelOpened = PublishSubject.create();
  private PublishSubject<TribeMessageDataChannel> onDataChannelMessage = PublishSubject.create();

  public TribePeerConnection(TribeSession session, PeerConnectionFactory peerConnectionFactory,
      List<PeerConnection.IceServer> iceServerList, boolean isOffer) {
    this.session = session;
    this.iceServerList = iceServerList;
    this.pendingIceCandidateList = new ArrayList<>();

    LogUtil.d(getClass(), "Initiating Peer Connection");
    init(peerConnectionFactory, isOffer);
    LogUtil.d(getClass(), "End initiating Peer Connection");
  }

  private void init(PeerConnectionFactory peerConnectionFactory, boolean isOffer) {
    dataChannelObserver = new TribeDataChannelObserver();
    sdpObserver = new TribeSdpObserver();
    peerConnectionObserver = new TribePeerConnectionObserver(isOffer);
    peerConnection =
        peerConnectionFactory.createPeerConnection(iceServerList, sdpObserver.constraints,
            peerConnectionObserver);
    sdpObserver.setPeerConnection(peerConnection);

    DataChannel.Init init = new DataChannel.Init();
    init.protocol = DATA_CHANNEL_PROTOCOL;
    dataChannel = peerConnection.createDataChannel(DATA_CHANNEL_META, init);
    dataChannel.registerObserver(dataChannelObserver);

    subscriptions.add(dataChannelObserver.onMessage().subscribe(message -> {
      onDataChannelMessage.onNext(new TribeMessageDataChannel(session, message));
    }));

    subscriptions.add(peerConnectionObserver.onReceivedDataChannel().subscribe(newDataChannel -> {
      dataChannel = newDataChannel;
      dataChannel.registerObserver(dataChannelObserver);
      onDataChannelOpened.onNext(null);
    }));

    subscriptions.add(
        peerConnectionObserver.onShouldCreateOffer().subscribe(aVoid -> sdpObserver.createOffer()));

    //subscriptions.add(peerConnectionObserver.onReceivedIceCandidates()
    //    .map(iceCandidates -> new TribeCandidate(new TribeSession(id, userId), iceCandidates))
    //    .subscribe(onReceivedTribeCandidate));

    subscriptions.add(peerConnectionObserver.onReceivedIceCandidate()
        .map(iceCandidate -> new TribeCandidate(session, iceCandidate))
        .subscribe(onReceivedTribeCandidate));

    subscriptions.add(peerConnectionObserver.onReceivedMediaStream()
        .map(mediaStream -> new TribeMediaStream(session, mediaStream))
        .subscribe(onReceivedMediaStream));
  }

  public void setRemoteDescription(SessionDescription sessionDescription) {
    if (sessionDescription == null) {
      LogUtil.e(getClass(), "Attempting to setRemoteDescription to null");
      return;
    }

    peerConnection.setRemoteDescription(sdpObserver,
        new SessionDescription(sessionDescription.type, sessionDescription.description));
  }

  public void addIceCandidate(final IceCandidate iceCandidate) {
    if (iceCandidate == null) {
      LogUtil.e(getClass(), "Attempting to add null ice candidate");
      return;
    }

    if (peerConnection == null) {
      LogUtil.e(getClass(), "Peer connection is null can't add iceCandidate");
      return;
    }

    if (peerConnection.getRemoteDescription() == null) {
      LogUtil.d(getClass(), "Adding iceCandidate to pending");
      pendingIceCandidateList.add(iceCandidate);
      return;
    }

    LogUtil.d(getClass(), "Adding iceCandidate : " + iceCandidate);
    peerConnection.addIceCandidate(iceCandidate);
  }

  public void send(String str) {
    LogUtil.d(getClass(), "Sending through dataChannel : " + str);
    ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
    dataChannel.send(new DataChannel.Buffer(buffer, false));
  }

  public PeerConnection getPeerConnection() {
    return peerConnection;
  }

  public void dispose(MediaStream mediaStream) {
    subscriptions.clear();

    if (dataChannel != null) {
      dataChannel.close();
      dataChannel = null;
    }

    if (peerConnection != null) {
      if (mediaStream != null) peerConnection.removeStream(mediaStream);
      peerConnection.close();
      sdpObserver.dropPeerConnection();
      peerConnection = null;
      peerConnectionObserver = null;
    }

    if (dataChannelObserver != null) {
      dataChannelObserver = null;
    }
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

  public Observable<Void> onDataChannelOpened() {
    return onDataChannelOpened;
  }

  public Observable<TribeMessageDataChannel> onDataChannelMessage() {
    return onDataChannelMessage;
  }
}
