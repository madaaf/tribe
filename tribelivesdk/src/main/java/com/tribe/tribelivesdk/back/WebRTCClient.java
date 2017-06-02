package com.tribe.tribelivesdk.back;

import android.content.Context;
import com.tribe.tribelivesdk.core.TribePeerConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeAnswer;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeMediaConstraints;
import com.tribe.tribelivesdk.model.TribeMediaStream;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.stream.StreamManager;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.R.attr.id;

@Singleton public class WebRTCClient {

  // VARIABLES
  private TribeLiveOptions options;
  private Context context;
  private Map<String, TribePeerConnection> peerConnections;
  private MediaStream localMediaStream;
  private PeerConnectionFactory peerConnectionFactory;
  private List<PeerConnection.IceServer> iceServers;
  private StreamManager streamManager;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribeOffer> onReadyToSendSdpOffer = PublishSubject.create();
  private PublishSubject<TribeAnswer> onReadyToSendSdpAnswer = PublishSubject.create();
  private PublishSubject<TribeCandidate> onReceivedTribeCandidate = PublishSubject.create();
  private PublishSubject<TribeMediaStream> onReceivedPeer = PublishSubject.create();
  private PublishSubject<JSONObject> onSendToPeers = PublishSubject.create();
  private PublishSubject<PeerConnection.IceGatheringState> onIceGatheringChanged =
      PublishSubject.create();

  @Inject public WebRTCClient(Context context) {
    this.context = context;
    this.streamManager = new StreamManager(context);
    this.peerConnections = new HashMap<>();
    initPeerConnectionFactory();
  }

  private void initPeerConnectionFactory() {
    if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, false)) {
      Timber.d("Failed to initializeAndroidGlobals");
    }

    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    peerConnectionFactory = new PeerConnectionFactory(options);
  }

  public void initSubscriptions() {
    subscriptions.add(streamManager.onMediaChanged().subscribe(mediaConfiguration -> {
      JSONObject jsonMedia = getJSONMedia(mediaConfiguration);
      onSendToPeers.onNext(jsonMedia);
    }));
  }

  public void setOptions(TribeLiveOptions options) {
    this.options = options;
  }

  public void setIceServers(List<PeerConnection.IceServer> iceServers) {
    this.iceServers = iceServers;
  }

  public void addPeerConnection(TribeSession session, boolean isOffer) {
    if (session == null) {
      Timber.d("Attempt to addPeerConnection with null peerId");
      return;
    }

    if (peerConnections.get(session.getPeerId()) != null) {
      Timber.d("Client already exists - not adding client again. " + id);
      return;
    }

    Timber.d("Attemp to addPeerConnection : " + localMediaStream);
    TribePeerConnection remotePeer = createPeerConnection(session, isOffer);
    peerConnections.put(session.getPeerId(), remotePeer);

    if ((options.getRoutingMode().equals(TribeLiveOptions.P2P)
        || session.getPeerId().equals(TribeSession.PUBLISHER_ID)
        && remotePeer.getPeerConnection() != null)) {
      remotePeer.getPeerConnection().addStream(localMediaStream);
    }

    subscriptions.add(remotePeer.onReadyToSendSdpOffer().subscribe(onReadyToSendSdpOffer));
    subscriptions.add(remotePeer.onReadyToSendSdpAnswer().subscribe(onReadyToSendSdpAnswer));
    subscriptions.add(remotePeer.onReceiveTribeCandidate().subscribe(onReceivedTribeCandidate));

    if (session.getPeerId().equals(TribeSession.PUBLISHER_ID)) {
      subscriptions.add(remotePeer.onIceGatheringChanged().subscribe(onIceGatheringChanged));
    }

    subscriptions.add(remotePeer.onReceivedMediaStream()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(tribeMediaStream -> streamManager.generateNewRemotePeer(session))
        .doOnNext(tribeMediaStream -> streamManager.setMediaStreamForClient(session.getPeerId(),
            tribeMediaStream.getMediaStream()))
        .subscribe(onReceivedPeer));
  }

  private TribePeerConnection createPeerConnection(TribeSession session, boolean isOffer) {
    return new TribePeerConnection(session, peerConnectionFactory, iceServers, isOffer);
  }

  public boolean removePeerConnection(TribeSession tribeSession) {
    Timber.d("removePeerConnection for peerId : " + tribeSession.getPeerId());
    TribePeerConnection tribePeerConnection = peerConnections.get(tribeSession.getPeerId());

    if (tribePeerConnection == null) {
      Timber.d("Attempt to removePeerConnection on invalid peerId");
      return false;
    }

    streamManager.removePeer(tribeSession);

    tribePeerConnection.dispose();

    try {
      peerConnections.remove(tribeSession.getPeerId());
      return true;
    } catch (Throwable throwable) {
      throw throwable;
    }
  }

  public void setRemoteMediaConfiguration(TribePeerMediaConfiguration tribePeerMediaConfiguration) {
    Timber.d("setMediaConfiguration for peerId : " + tribePeerMediaConfiguration.getSession()
        .getPeerId());

    streamManager.setPeerMediaConfiguration(tribePeerMediaConfiguration);
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    return streamManager.getMediaConfiguration();
  }

  public void updateMediaConstraints(TribeMediaConstraints tribeMediaConstraints) {
    streamManager.updateMediaConstraints(tribeMediaConstraints);

    if (tribeMediaConstraints.isShouldCreateOffer()) {
      for (TribePeerConnection peerConnection : peerConnections.values()) {
        if (peerConnection.getSession().getPeerId().equals(TribeSession.PUBLISHER_ID)) {
          peerConnection.createOffer();
        }
      }
    }
  }

  private void initLocalStream() {
    if (localMediaStream == null) {
      localMediaStream = streamManager.generateLocalStream(context, peerConnectionFactory);
    }
  }

  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public void setLocalStreamView(LocalPeerView peerView) {
    streamManager.initLocalStreamView(peerView, peerConnectionFactory);
    initLocalStream();
  }

  public void addIceCandidate(String peerId, IceCandidate iceCandidate) {
    TribePeerConnection tribePeerConnection = peerConnections.get(peerId);

    if (tribePeerConnection == null) {
      Timber.d("Attempt to addIceCandidate on invalid clientId");
      return;
    }

    tribePeerConnection.addIceCandidate(iceCandidate);
  }

  public void setRemoteDescription(TribeSession session, SessionDescription sdp) {
    TribePeerConnection tribePeerConnection = peerConnections.get(session.getPeerId());

    if (tribePeerConnection == null) {
      Timber.d("Peer is null, creating it");
      if (!session.equals(TribeSession.PUBLISHER_ID)) {
        addPeerConnection(session, false);
        tribePeerConnection = peerConnections.get(session.getPeerId());
      }
    }

    tribePeerConnection.setRemoteDescription(sdp);
  }

  public Collection<TribePeerConnection> getPeers() {
    return peerConnections.values();
  }

  public JSONObject getJSONMedia(TribePeerMediaConfiguration mediaConfiguration) {
    JSONObject obj = new JSONObject();
    jsonPut(obj, "isAudioEnabled", mediaConfiguration.isAudioEnabled());
    jsonPut(obj, "isVideoEnabled", mediaConfiguration.isVideoEnabled());
    jsonPut(obj, "videoChangeReason", mediaConfiguration.getType());
    return obj;
  }

  public void dispose(boolean shouldDisposeLocal) {
    Timber.d("Disposing subscriptions");
    if (subscriptions.hasSubscriptions()) subscriptions.clear();

    if (peerConnections != null && peerConnections.size() > 0) {
      Timber.d("Iterating peer subscriptions");
      for (TribePeerConnection tribePeerConnection : peerConnections.values()) {
        tribePeerConnection.dispose();
      }

      Timber.d("Clearing all peer connections");
      peerConnections.clear();
    }

    streamManager.dispose(shouldDisposeLocal);

    if (shouldDisposeLocal) localMediaStream = null;
    Timber.d("End dispose success");
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribeOffer> onReadyToSendSdpOffer() {
    return onReadyToSendSdpOffer;
  }

  public Observable<TribeAnswer> onReadyToSendSdpAnswer() {
    return onReadyToSendSdpAnswer;
  }

  public Observable<TribeCandidate> onReceivedTribeCandidate() {
    return onReceivedTribeCandidate;
  }

  public Observable<ObservableRxHashMap.RxHashMap<String, RemotePeer>> onRemotePeersChanged() {
    return streamManager.onRemotePeersChanged();
  }

  public Observable<JSONObject> onSendToPeers() {
    return onSendToPeers;
  }

  public Observable<PeerConnection.IceGatheringState> onIceGatheringChanged() {
    return onIceGatheringChanged;
  }
}