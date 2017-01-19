package com.tribe.tribelivesdk.model;

import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeer extends Peer {

    private String id;
    private RemotePeerView peerView;

    public RemotePeer(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public PeerView getPeerView() {
        return peerView;
    }

    public void setPeerView(RemotePeerView peerView) {
        this.peerView = peerView;
    }
}
