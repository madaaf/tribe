package com.tribe.tribelivesdk.back;

import android.content.Context;
import android.support.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.webrtc.PeerConnection;

/**
 * Created by tiago on 13/01/2017.
 */

public class TribeLiveOptions {

  @StringDef({ P2P, ROUTED }) @Retention(RetentionPolicy.SOURCE) public @interface RoutingMode {
  }

  public static final String P2P = "p2p";
  public static final String ROUTED = "routed";

  private String wsUrl;
  private List<PeerConnection.IceServer> iceServers;
  private String tokenId;
  private String room_id;
  private @TribeLiveOptions.RoutingMode String routingMode;
  private Map<String, String> headers;
  private int orientation;
  private boolean frontCamera;
  private boolean shadowCall;

  private TribeLiveOptions(TribeLiveOptionsBuilder builder) {
    this.wsUrl = builder.wsUrl;
    //this.wsUrl = "wss://r.tribedev.pm/beta/39321";
    this.iceServers = builder.iceServers;
    this.tokenId = builder.tokenId;
    this.room_id = builder.roomId;
    this.routingMode = builder.routingMode;
    this.headers = builder.headers;
    this.orientation = builder.orientation;
    this.frontCamera = builder.frontCamera;
    this.shadowCall = builder.shadowCall;
  }

  public static class TribeLiveOptionsBuilder {
    private final Context context;
    private String wsUrl;
    private List<PeerConnection.IceServer> iceServers;
    private String tokenId;
    private String roomId;
    private @RoutingMode String routingMode;
    private Map<String, String> headers;
    private int orientation;
    private boolean frontCamera;
    private boolean shadowCall;

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
        if (iceConfig.getUrls() != null) {
          for (String url : iceConfig.getUrls()) {
            iceServers.add(new PeerConnection.IceServer(url, iceConfig.getUsername(),
                iceConfig.getCredential()));
          }
        }
      }

      return this;
    }

    public TribeLiveOptionsBuilder tokenId(String tokenId) {
      this.tokenId = tokenId;
      return this;
    }

    public TribeLiveOptionsBuilder roomId(String roomId) {
      this.roomId = roomId;
      return this;
    }

    public TribeLiveOptionsBuilder routingMode(@RoutingMode String routingMode) {
      this.routingMode = routingMode;
      return this;
    }

    public TribeLiveOptionsBuilder headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public TribeLiveOptionsBuilder orientation(int orientation) {
      this.orientation = orientation;
      return this;
    }

    public TribeLiveOptionsBuilder frontCamera(boolean front) {
      this.frontCamera = front;
      return this;
    }

    public TribeLiveOptionsBuilder shadowCall(boolean shadowCall) {
      this.shadowCall = shadowCall;
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

  public String getRoomId() {
    return room_id;
  }

  public void setRoomId(String roomId) {
    this.room_id = roomId;
  }

  public String getRoutingMode() {
    return routingMode;
  }

  public void setRoutingMode(String routingMode) {
    this.routingMode = routingMode;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public int getOrientation() {
    return orientation;
  }

  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }

  public boolean isFrontCamera() {
    return frontCamera;
  }

  public void setFrontCamera(boolean frontCamera) {
    this.frontCamera = frontCamera;
  }

  public boolean isShadowCall() {
    return shadowCall;
  }
}
