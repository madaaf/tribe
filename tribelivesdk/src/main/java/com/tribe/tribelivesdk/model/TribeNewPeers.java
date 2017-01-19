package com.tribe.tribelivesdk.model;

import java.util.List;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeNewPeers {

    private List<String> peerIds;

    public TribeNewPeers(List<String> peerIds) {
        this.peerIds = peerIds;
    }

    public List<String> getPeerIds() {
        return peerIds;
    }
}
