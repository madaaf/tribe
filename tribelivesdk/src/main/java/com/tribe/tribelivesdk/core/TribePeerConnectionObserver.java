package com.tribe.tribelivesdk.core;

import java.util.LinkedList;
import java.util.List;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class TribePeerConnectionObserver implements PeerConnection.Observer {

  private boolean isOffer;
  private final List<IceCandidate> queuedLocalCandidates = new LinkedList();

  private PublishSubject<PeerConnection.IceConnectionState> onIceConnectionChanged =
      PublishSubject.create();
  //private PublishSubject<List<IceCandidate>> onReceivedIceCandidates = PublishSubject.create();
  private PublishSubject<IceCandidate> onReceivedIceCandidate = PublishSubject.create();
  private PublishSubject<MediaStream> onReceivedMediaStream = PublishSubject.create();
  private PublishSubject<MediaStream> onRemovedMediaStream = PublishSubject.create();
  private PublishSubject<Void> onShouldCreateOffer = PublishSubject.create();

  public TribePeerConnectionObserver(boolean isOffer) {
    this.isOffer = isOffer;
  }

  @Override public void onAddStream(MediaStream mediaStream) {
    Timber.d("onAddStream");

    if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
      Timber.e("Weird-looking stream: " + mediaStream);
      return;
    }

    if (mediaStream.videoTracks.size() == 1) {
      onReceivedMediaStream.onNext(mediaStream);
    }
  }

  @Override public void onRemoveStream(MediaStream mediaStream) {
    Timber.d("onRemoveStream");
    onRemovedMediaStream.onNext(mediaStream);
  }

  @Override public void onDataChannel(DataChannel dataChannel) {
    Timber.d("onDataChannel : " + dataChannel);
  }

  @Override public void onIceCandidate(IceCandidate iceCandidate) {
    Timber.d("onIceCandidate : " + iceCandidate);

    if (iceCandidate != null) {
      String type = iceCandidate.toString().split(" ")[7];
      //if (!type.equals("relay")) {
      //    Timber.d( "iceCandidate " + type + " ignored: TURN only mode.");
      //    return;
      //}
      Timber.d("iceCandidate " + type + " candidate accepted");
      onReceivedIceCandidate.onNext(iceCandidate);
      //queuedLocalCandidates.add(iceCandidate);
    }
  }

  @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
    Timber.d("onIceCandidateRemoved. " + iceCandidates);
  }

  @Override
  public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
    Timber.d("onIceConnectionChange. " + iceConnectionState);
    onIceConnectionChanged.onNext(iceConnectionState);
  }

  @Override public void onIceConnectionReceivingChange(boolean change) {
    Timber.d("OnIceConnectionReceivingChange : " + change);
  }

  @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    Timber.d("onIceGatheringChange : " + iceGatheringState.name());

    if (iceGatheringState != PeerConnection.IceGatheringState.COMPLETE) {
      return;
    }

    //LinkedList<IceCandidate> iceCandidateLinkedList = new LinkedList(queuedLocalCandidates);
    //queuedLocalCandidates.clear();
    //onReceivedIceCandidates.onNext(iceCandidateLinkedList);
  }

  @Override public void onRenegotiationNeeded() {
    Timber.d("onRenegotiationNeeded");
    if (isOffer) {
      onShouldCreateOffer.onNext(null);
    }
  }

  @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    Timber.d("onSignalingChange : " + signalingState.name());
  }

  @Override public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

  }

  public Observable<PeerConnection.IceConnectionState> onIceConnectionChanged() {
    return onIceConnectionChanged;
  }

  //public Observable<List<IceCandidate>> onReceivedIceCandidates() {
  //  return onReceivedIceCandidates;
  //}

  public Observable<IceCandidate> onReceivedIceCandidate() {
    return onReceivedIceCandidate;
  }

  public Observable<MediaStream> onReceivedMediaStream() {
    return onReceivedMediaStream;
  }

  public Observable<MediaStream> onRemovedMediaStream() {
    return onRemovedMediaStream;
  }

  public Observable<Void> onShouldCreateOffer() {
    return onShouldCreateOffer;
  }
}
