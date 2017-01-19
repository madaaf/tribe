package com.tribe.tribelivesdk.model;

import org.webrtc.IceCandidate;

import java.util.List;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeCandidate {

    private String id;
    private IceCandidate iceCandidate;
    private List<IceCandidate> iceCandidateList;

    public TribeCandidate(String id, IceCandidate iceCandidate) {
        this.id = id;
        this.iceCandidate = iceCandidate;
    }

    public TribeCandidate(String id, List<IceCandidate> iceCandidateList) {
        this.id = id;
        this.iceCandidateList = iceCandidateList;
    }

    public String getId() {
        return id;
    }

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public List<IceCandidate> getIceCandidateList() {
        return iceCandidateList;
    }

    public void setIceCandidateList(List<IceCandidate> iceCandidateList) {
        this.iceCandidateList = iceCandidateList;
    }
}
