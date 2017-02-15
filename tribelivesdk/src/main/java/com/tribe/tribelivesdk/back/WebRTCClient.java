package com.tribe.tribelivesdk.back;

import android.content.Context;
import com.tribe.tribelivesdk.core.TribePeerConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeAnswer;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeMediaStream;
import com.tribe.tribelivesdk.model.TribeMessageDataChannel;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.stream.StreamManager;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
  private PublishSubject<TribeMessageDataChannel> onReceivedDataChannelMessage =
      PublishSubject.create();

  @Inject public WebRTCClient(Context context) {
    this.context = context;
    this.streamManager = new StreamManager(context);
    this.peerConnections = new HashMap<>();
    initPeerConnectionFactory();
    initSubscriptions();
  }

  private void initPeerConnectionFactory() {
    if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, false)) {
      Timber.e("Failed to initializeAndroidGlobals");
    }

    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    peerConnectionFactory = new PeerConnectionFactory(options);
  }

  private void initSubscriptions() {
    subscriptions.add(streamManager.onMediaChanged().subscribe(aVoid -> {
      JSONObject jsonMedia =
          getJSONMedia(streamManager.isLocalAudioEnabled(), streamManager.isLocalCameraEnabled());
      for (TribePeerConnection tribePeerConnection : peerConnections.values()) {
        tribePeerConnection.send(jsonMedia.toString());
      }
    }));
  }

  public void setIceServers(List<PeerConnection.IceServer> iceServers) {
    this.iceServers = iceServers;
  }

  public void addPeerConnection(TribeSession session, boolean isOffer) {
    if (session == null) {
      Timber.e("Attempt to addPeerConnection with null peerId");
      return;
    }

    if (peerConnections.get(session.getPeerId()) != null) {
      Timber.i("Client already exists - not adding client again. " + id);
      return;
    }

    Timber.d("Attemp to addPeerConnection : " + localMediaStream);
    TribePeerConnection remotePeer = createPeerConnection(session, isOffer);
    peerConnections.put(session.getPeerId(), remotePeer);
    remotePeer.getPeerConnection().addStream(localMediaStream);

    subscriptions.add(remotePeer.onReadyToSendSdpOffer().subscribe(onReadyToSendSdpOffer));
    subscriptions.add(remotePeer.onReadyToSendSdpAnswer().subscribe(onReadyToSendSdpAnswer));
    subscriptions.add(remotePeer.onReceiveTribeCandidate().subscribe(onReceivedTribeCandidate));

    subscriptions.add(remotePeer.onReceivedMediaStream()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(tribeMediaStream -> streamManager.generateNewRemotePeer(session))
        .doOnNext(tribeMediaStream -> streamManager.setMediaStreamForClient(session.getPeerId(),
            tribeMediaStream.getMediaStream()))
        .subscribe(onReceivedPeer));

    subscriptions.add(remotePeer.onDataChannelOpened()
        .delay(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> remotePeer.send(getJSONMedia(streamManager.isLocalAudioEnabled(),
            streamManager.isLocalCameraEnabled()).toString())));

    subscriptions.add(remotePeer.onDataChannelMessage().subscribe(onReceivedDataChannelMessage));
  }

  private TribePeerConnection createPeerConnection(TribeSession session, boolean isOffer) {
    return new TribePeerConnection(session, peerConnectionFactory, iceServers, isOffer);
  }

  public boolean removePeerConnection(TribeSession tribeSession) {
    Timber.d("removePeerConnection for peerId : " + tribeSession.getPeerId());
    TribePeerConnection tribePeerConnection = peerConnections.get(tribeSession.getPeerId());

    if (tribePeerConnection == null) {
      Timber.e("Attempt to removePeerConnection on invalid peerId");
      return false;
    }

    streamManager.removePeer(tribeSession);

    tribePeerConnection.dispose(localMediaStream);

    try {
      peerConnections.remove(tribeSession.getPeerId());
      return true;
    } catch (Throwable throwable) {
      throw throwable;
    }
  }

  public void setMediaConfiguration(TribePeerMediaConfiguration tribePeerMediaConfiguration) {
    Timber.d("setMediaConfiguration for peerId : " + tribePeerMediaConfiguration.getSession()
        .getPeerId());

    streamManager.setPeerMediaConfiguration(tribePeerMediaConfiguration);
  }

  private void initLocalStream() {
    if (localMediaStream == null) {
      localMediaStream = streamManager.generateLocalStream(context, peerConnectionFactory);
    }

    for (TribePeerConnection tpc : peerConnections.values()) {
      //tpc.addStream(localMediaStream);
    }
  }

  private JSONObject getJSONMedia(boolean audioEnabled, boolean videoEnabled) {
    JSONObject obj = new JSONObject();
    jsonPut(obj, "isAudioEnabled", audioEnabled);
    jsonPut(obj, "isVideoEnabled", videoEnabled);
    return obj;
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
      Timber.e("Attempt to addIceCandidate on invalid clientId");
      return;
    }

    tribePeerConnection.addIceCandidate(iceCandidate);
  }

  public void setRemoteDescription(TribeSession session, SessionDescription sdp) {
    TribePeerConnection tribePeerConnection = peerConnections.get(session.getPeerId());

    if (tribePeerConnection == null) {
      Timber.d("Peer is null, creating it");
      addPeerConnection(session, false);
      tribePeerConnection = peerConnections.get(session.getPeerId());
    }

    tribePeerConnection.setRemoteDescription(sdp);
  }

  public void sendToPeers(JSONObject obj) {
    if (peerConnections != null && peerConnections.size() > 0) {
      JSONObject appMsg = new JSONObject();
      jsonPut(appMsg, "app", obj);

      for (TribePeerConnection tribePeerConnection : peerConnections.values()) {
        tribePeerConnection.send(appMsg.toString());
      }
    }
  }

  public void dispose() {
    Timber.d("Disposing subscriptions");
    if (subscriptions.hasSubscriptions()) subscriptions.clear();

    if (peerConnections != null && peerConnections.size() > 0) {
      Timber.d("Iterating peer subscriptions");
      for (TribePeerConnection tribePeerConnection : peerConnections.values()) {
        tribePeerConnection.dispose(localMediaStream);
      }

      Timber.d("Clearing all peer connections");
      peerConnections.clear();
    }

    Timber.d("Disposing stream manager");
    streamManager.dispose();

    localMediaStream = null;
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

  public Observable<TribeMessageDataChannel> onReceivedDataChannelMessage() {
    return onReceivedDataChannelMessage;
  }
}