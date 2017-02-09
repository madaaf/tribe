package com.tribe.tribelivesdk.model;

import android.view.ViewGroup;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeer extends Peer {

  private TribeSession session;
  private RemotePeerView peerView;

  public RemotePeer(TribeSession session) {
    this.session = session;
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
    if (peerView != null) peerView.setMediaConfiguration(mediaConfiguration);
  }

  public void dispose() {
    if (peerView != null) {
      ((ViewGroup) peerView.getParent()).removeView(peerView);
      peerView.dispose();
    }
  }
}
