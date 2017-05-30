package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 25/05/2017.
 */

public class ShadowCallActivity extends BaseActivity implements LiveMVPView {

  private static String CONTRY_CODE = "CONTRY_CODE";
  private static String SMS_CONTENT = "SMS_CONTENT";

  public static Intent getCallingIntent(Context context, String countryCode, String smsContent) {
    Intent intent = new Intent(context, ShadowCallActivity.class);
    intent.putExtra(CONTRY_CODE, countryCode);
    intent.putExtra(SMS_CONTENT, smsContent);
    return intent;
  }

  @Inject AccessToken accessToken;

  @Inject LivePresenter livePresenter;

  @Inject TribeLiveSDK tribeLiveSDK;

  @Inject Navigator navigator;

  private Room room;
  private Uri deepLink;
  private String countryCode = null;
  private String smsContent = null;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(null);

    ((AndroidApplication) getApplicationContext()).getApplicationComponent().inject(this);

    if (getIntent().getExtras() != null) {
      countryCode = getIntent().getExtras().getString(CONTRY_CODE);
      smsContent = getIntent().getExtras().getString(SMS_CONTENT);
    }

    manageDeepLink(getIntent());
  }

  @Override protected void onDestroy() {
    subscriptions.clear();
    super.onDestroy();
  }

  public void joinRoom(RoomConfiguration roomConfiguration) {
    Map<String, String> headers = new HashMap<>();

    headers.put(WebSocketConnection.ORIGIN, com.tribe.app.BuildConfig.TRIBE_ORIGIN);

    TribeLiveOptions options = new TribeLiveOptions.TribeLiveOptionsBuilder(this).wsUrl(
        roomConfiguration.getWebsocketUrl())
        .tokenId(accessToken.getAccessToken())
        .iceServers(roomConfiguration.getRtcPeerConfiguration().getIceServers())
        .roomId(roomConfiguration.getRoomId())
        .routingMode(roomConfiguration.getRoutingMode())
        .headers(headers)
        .shadowCall(true)
        .build();

    Timber.d("Initiating Room");
    room.connect(options);

    subscriptions.add(room.onJoined().subscribe(tribeJoinRoom -> {
      room.leaveRoom();
      navigator.navigateToHomeFromLogin(this, countryCode, null, smsContent);
      finish();
    }));
  }

  /////////////////
  //   PRIVATE   //
  /////////////////

  private void start(Uri uri) {
    room = tribeLiveSDK.newRoom();
    getRoomParameter(uri);
  }

  private void getRoomParameter(Uri uri) {
    String path = uri.getPath();
    String host = uri.getHost();
    String scheme = uri.getScheme();
    String roomId = uri.getQueryParameter("roomId");
    String linkId, url, deepLinkScheme = getString(R.string.deeplink_host);

    if (!StringUtils.isEmpty(roomId)) {
      linkId = roomId;
      url = uri.getScheme() + "://" + host + "/" + linkId;
    } else {
      linkId = path.substring(1, path.length());
      if (deepLinkScheme.equals(scheme)) {
        url = StringUtils.getUrlFromLinkId(this, linkId);
      } else {
        url = uri.toString();
      }
    }

    Live live = new Live.Builder(Live.WEB, Live.WEB).linkId(linkId)
        .url(url)
        .source(LiveActivity.SOURCE_DEEPLINK)
        .build();

    livePresenter.joinRoom(live);
  }

  private void manageDeepLink(Intent intent) {
    if (intent != null && intent.getData() != null) {
      deepLink = intent.getData();
      start(deepLink);
    }
  }

  @Override protected void onStop() {
    livePresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onStart() {
    super.onStart();
    livePresenter.onViewAttached(this);
  }

  @Override public Context context() {
    return this;
  }

  @Override public void onRecipientInfos(Recipient recipient) {

  }

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {

  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    joinRoom(roomConfiguration);
  }

  @Override public void onJoinRoomError(String message) {
    navigator.navigateToHomeFromLogin(this, countryCode, null, null);
  }

  @Override public void onRoomFull(String message) {

  }

  @Override public void onReceivedAnonymousMemberInRoom(List<User> users) {

  }

  @Override public void onRoomLink(String roomLink) {

  }

  @Override public void onAddError() {

  }

  @Override public void onAddSuccess(Friendship friendship) {

  }
}
