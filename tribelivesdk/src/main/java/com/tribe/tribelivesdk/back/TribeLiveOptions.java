package com.tribe.tribelivesdk.back;

import android.content.Context;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 13/01/2017.
 */

public class TribeLiveOptions {

    private String wsUrl;
    private List<PeerConnection.IceServer> iceServers;
    private String tokenId;

    private TribeLiveOptions(TribeLiveOptionsBuilder builder) {
        this.wsUrl = builder.wsUrl;
        this.iceServers = builder.iceServers;
        this.tokenId = builder.tokenId;
    }

    public static class TribeLiveOptionsBuilder {
        private final Context context;
        private String wsUrl;
        private List<PeerConnection.IceServer> iceServers;
        private String tokenId;

        public TribeLiveOptionsBuilder(Context context) {
            this.context = context;
        }

        public TribeLiveOptionsBuilder wsUrl(String url) {
            this.wsUrl = url;
            return this;
        }

        public TribeLiveOptionsBuilder iceServers(List<IceConfig> iceConfigList) {
            this.iceServers = new ArrayList<>();

            for (IceConfig iceConfig : iceConfigList) {
                iceServers.add(new PeerConnection.IceServer(iceConfig.getUrl(), iceConfig.getUsername(), iceConfig.getCredentials()));
            }

            return this;
        }

        public TribeLiveOptionsBuilder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public TribeLiveOptions build() {
            return new TribeLiveOptions(this);
        }
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public List<PeerConnection.IceServer> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<PeerConnection.IceServer> iceServers) {
        this.iceServers = iceServers;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
