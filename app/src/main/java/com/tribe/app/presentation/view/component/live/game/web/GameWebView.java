package com.tribe.app.presentation.view.component.live.game.web;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameWebView extends GameViewWithEngine {

  @BindView(R.id.webView) WebView webView;

  // VARIABLES
  private boolean didRestartWhenReady = false;
  private MediaPlayer mediaPlayer;

  public GameWebView(@NonNull Context context) {
    super(context);
  }

  public GameWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_web, this, true);
    unbinder = ButterKnife.bind(this);

    webView.setFocusable(true);
    webView.setFocusableInTouchMode(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setDatabaseEnabled(true);
    webView.getSettings().setAppCacheEnabled(true);
    webView.setWebChromeClient(webChromeClient);
    webView.setWebViewClient(webViewClient);
    webView.addJavascriptInterface(new WebViewGameInterface(), "androidInterface");
  }

  @Override protected int getSoundtrack() {
    return -1;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) {
    super.setupGameLocally(userId, players, timestamp);
  }

  @Override protected void gameOver(String winnerId) {
    super.gameOver(winnerId);
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();

    subscriptions.add(gameEngine.onPlayerReady().subscribe(s -> {
      if (!didRestartWhenReady) {
        Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
        if (mapPlayerStatus.size() == 2) {
          didRestartWhenReady = true;
        }

        subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              resetScores();
              Set<String> playerIds = mapPlayerStatus.keySet();
              newGame(playerIds);
            }));
      }
    }));
  }

  private void gameLoaded() {
    // remove loading

    imReady();

    refactorReady(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      webView.evaluateJavascript("Tribe.getSoundtrack()", value -> {
        mediaPlayer = new MediaPlayer();
        try {
          mediaPlayer.setDataSource(value);
          mediaPlayer.prepare();
          mediaPlayer.start();
        } catch (IOException e) {
          Timber.e("Soundtrack prepare() failed");
        }
      });
    }
  }

  @Override protected long startGameTimestamp() {
    return System.currentTimeMillis() + (int) (3.5f * 1000);
  }

  protected WebChromeClient webChromeClient = new WebChromeClient() {
    @Override public void onProgressChanged(WebView view, int newProgress) {
      super.onProgressChanged(view, newProgress);
      Timber.d("New Progress : " + newProgress);
    }

    @Override public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      Timber.d("Console message : " + consoleMessage.message());
      return super.onConsoleMessage(consoleMessage);
    }
  };

  protected WebViewClient webViewClient = new WebViewClient() {
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
      Timber.d("Error");
    }
  };

  private void executeJavascript(String code) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      webView.evaluateJavascript(code, value -> {
        Timber.d("value : " + value);
      });
    }
  }

  private String escapeJavascript(String code) {
    return code.replaceAll("'", "\'").replaceAll("\n", "<br>");
  }

  @Override protected void showTitle(LabelListener listener) {
    executeJavascript("Tribe.displayTitle(3.5)");

    subscriptions.add(Observable.timer(3500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (listener != null) listener.call();
        }));
  }

  @Override
  protected void showMessage(String text, int duration, LabelListener willDisappearListener,
      LabelListener completionListener) {
    executeJavascript("Tribe.displayMessage('" + escapeJavascript(text) + "', 1.5);");

    subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (completionListener != null) completionListener.call();
        }));

    subscriptions.add(Observable.timer(1500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (willDisappearListener != null) willDisappearListener.call();
        }));
  }

  @Override
  protected void changeMessageStatus(View view, boolean isVisible, boolean isAnimated, int duration,
      int delay, LabelListener blockToCall, LabelListener completionListener) {
    if (view == txtRestart && isVisible) {
      executeJavascript("Tribe.displayRestart('" +
          escapeJavascript(getResources().getString(
              StringUtils.stringWithPrefix(getContext(), wordingPrefix, "pending_instructions"))) +
          "');");
    }

    if (blockToCall != null) blockToCall.call();

    if (isAnimated) {
      subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            if (completionListener != null) completionListener.call();
          }));
    } else {
      if (completionListener != null) completionListener.call();
    }
  }

  @Override protected void playGame() {
    super.playGame();

    executeJavascript("Tribe.startGame()");
  }

  private class WebViewGameInterface {

    @JavascriptInterface public void gameEnded() {
      Timber.d("Game ended");
    }

    @JavascriptInterface public void scoreUpdated(int score) {
      Timber.d("Score updated");
    }

    @JavascriptInterface public void gameLoadingProgress(float progress) {
      Timber.d("Progress : " + (int) (progress * 100));
    }

    @JavascriptInterface public void gameLoaded() {
      GameWebView.this.gameLoaded();
    }
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_webv1_";
    super.start(game, mapObservable, liveViewsObservable, userId);
    webView.loadUrl(game.getUrl());
  }

  @Override public void stop() {
    super.stop();

    if (mediaPlayer != null) {
      mediaPlayer.reset();
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

  @Override public void dispose() {
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
