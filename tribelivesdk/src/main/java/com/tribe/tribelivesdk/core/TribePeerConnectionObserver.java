package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.util.LogUtil;
import java.util.LinkedList;
import java.util.List;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import rx.Observable;
import rx.subjects.PublishSubject;

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
    LogUtil.d(getClass(), "onAddStream");

    if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
      LogUtil.e(getClass(), "Weird-looking stream: " + mediaStream);
      return;
    }

    if (mediaStream.videoTracks.size() == 1) {
      onReceivedMediaStream.onNext(mediaStream);
    }
  }

  @Override public void onRemoveStream(MediaStream mediaStream) {
    LogUtil.d(getClass(), "onRemoveStream");
    onRemovedMediaStream.onNext(mediaStream);
  }

  @Override public void onDataChannel(DataChannel dataChannel) {
    LogUtil.d(getClass(), "(unhandled) onDataChannel");
  }

  @Override public void onIceCandidate(IceCandidate iceCandidate) {
    LogUtil.d(getClass(), "onIceCandidate : " + iceCandidate);

    if (iceCandidate != null) {
      String type = iceCandidate.toString().split(" ")[7];
      //if (!type.equals("relay")) {
      //    LogUtil.d(getClass(), "iceCandidate " + type + " ignored: TURN only mode.");
      //    return;
      //}
      LogUtil.d(getClass(), "iceCandidate " + type + " candidate accepted");
      onReceivedIceCandidate.onNext(iceCandidate);
      //queuedLocalCandidates.add(iceCandidate);
    }
  }

  @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
    LogUtil.d(getClass(), "onIceCandidateRemoved. " + iceCandidates);
  }

  @Override
  public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
    LogUtil.d(getClass(), "onIceConnectionChange. " + iceConnectionState);
    onIceConnectionChanged.onNext(iceConnectionState);
  }

  @Override public void onIceConnectionReceivingChange(boolean change) {
    LogUtil.d(getClass(), "OnIceConnectionReceivingChange : " + change);
  }

  @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    LogUtil.d(getClass(), "onIceGatheringChange : " + iceGatheringState.name());

    if (iceGatheringState != PeerConnection.IceGatheringState.COMPLETE) {
      return;
    }

    //LinkedList<IceCandidate> iceCandidateLinkedList = new LinkedList(queuedLocalCandidates);
    //queuedLocalCandidates.clear();
    //onReceivedIceCandidates.onNext(iceCandidateLinkedList);
  }

  @Override public void onRenegotiationNeeded() {
    LogUtil.d(getClass(), "onRenegotiationNeeded");
    if (isOffer) {
      onShouldCreateOffer.onNext(null);
    }
  }

  @Override public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

  }

  @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    LogUtil.d(getClass(), "onSignalingChange : " + signalingState.name());
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
