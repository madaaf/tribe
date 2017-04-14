package com.tribe.tribelivesdk.model;

import android.view.ViewGroup;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import timber.log.Timber;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeer extends Peer {

  private TribeSession session;
  private RemotePeerView peerView;
  private TribePeerMediaConfiguration mediaConfiguration;

  public RemotePeer(TribeSession session) {
    this.session = session;
    mediaConfiguration = new TribePeerMediaConfiguration(session);
  }

  public TribeSession getSession() {
    return session;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }

  @Override public PeerView getPeerView() {
    return peerView;
  }

  public void setPeerView(RemotePeerView peerView) {
    this.peerView = peerView;
  }

  public void setMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    this.mediaConfiguration = mediaConfiguration;
    if (peerView != null) peerView.setMediaConfiguration(mediaConfiguration);
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    return mediaConfiguration;
  }

  public boolean isRenderingWell() {
    if (peerView != null) return peerView.isRenderingWell();
    return true;
  }

  public void dispose() {
    if (peerView != null) {
      Timber.d("Disposing remote peer for peer : " + session.getPeerId());
      if (peerView.getParent() != null) ((ViewGroup) peerView.getParent()).removeView(peerView);
      peerView.dispose();
      Timber.d("End disposing remote peer for peer : " + session.getPeerId());
    }
  }
}
