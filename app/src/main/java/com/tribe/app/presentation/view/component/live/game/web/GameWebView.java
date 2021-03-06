package com.tribe.app.presentation.view.component.live.game.web;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.LoadingGameView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameWebView extends GameViewWithEngine {

  @BindView(R.id.webView) WebView webView;
  @BindView(R.id.layoutProgress) FrameLayout layoutProgress;
  @BindView(R.id.loader) LoadingGameView loader;

  // VARIABLES
  private boolean didRestartWhenReady = false, isResetingScores = false;
  private MediaPlayer mediaPlayer;
  private Handler mainHandler;

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

    mainHandler = new Handler(context.getMainLooper());

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

  @Override protected void initGameMasterManagerSubscriptions() {

  }

  @Override protected int getSoundtrack() {
    return -1;
  }

  @Override protected String getStyleFont() {
    return FontUtils.GULKAVE_REGULAR;
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    super.gameOver(winnerId, isLocal);
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();

    didRestartWhenReady = false;

    subscriptions.add(gameEngine.onPlayerReady().subscribe(s -> {
      if (!didRestartWhenReady) {
        Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
        if (mapPlayerStatus.size() == 2) {
          didRestartWhenReady = true;

          subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
              .onBackpressureDrop()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> {
                resetScores(false);
                Set<String> playerIds = mapPlayerStatus.keySet();
                newGame(playerIds);
              }));
        }
      }
    }));
  }

  private void playSound() {
    Timber.d("Play sound");
  }

  private void gameLoaded() {
    AnimationUtils.fadeOut(layoutProgress, 250, new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        removeView(layoutProgress);
      }
    });

    imReady();

    refactorReady(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Timber.d("Trying getSoundtrack()");
      webView.evaluateJavascript("Tribe.getSoundtrack()", value -> {
        Timber.d("evaluate getSoundtrack() : " + value);
        mediaPlayer = new MediaPlayer();
        try {
          mediaPlayer.setDataSource(value);
          mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
          mediaPlayer.prepareAsync();
          mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override public void onPrepared(MediaPlayer mp) {
              mediaPlayer.start();
            }
          });
        } catch (IOException e) {
          e.printStackTrace();
          Timber.e("Soundtrack prepare() failed", e);
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
    Timber.d("evaluateJavascript : " + code);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      webView.evaluateJavascript(code, value -> {
        Timber.d("value : " + value);
      });
    }
  }

  private String escapeJavascript(String code) {
    return code.replaceAll("\n", "<br>");
  }

  @Override protected void showTitle(LabelListener listener, String font) {
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
    executeJavascript("Tribe.displayMessage(\"" + escapeJavascript(text) + "\", 1.5);");

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
    Timber.d("changeMessageStatus");
    if (view == txtRestart && isVisible) {
      String restartText = ((TextView) view).getText().toString();
      if (!StringUtils.isEmpty(restartText)) {
        executeJavascript("Tribe.displayRestart(\"" + escapeJavascript(restartText) + "\");");
      }
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
    isResetingScores = false;
    executeJavascript("Tribe.startGame()");
  }

  @Override public void resetScores(boolean shouldSendGameOver) {
    super.resetScores(shouldSendGameOver);
    Timber.d("resetScores");
    isResetingScores = true;
  }

  private class WebViewGameInterface {

    @JavascriptInterface public void gameEnded() {
      if (isResetingScores) return;
      mainHandler.post(() -> GameWebView.this.iLost());
    }

    @JavascriptInterface public void scoreIncremented(int points) {
      if (isResetingScores) return;
      mainHandler.post(() -> GameWebView.this.addPoints(points, currentUser.getId(), true));
    }

    @JavascriptInterface public void gameLoadingProgress(float progress) {
      mainHandler.post(() -> loader.setAnim(0f, 90f));
    }

    @JavascriptInterface public void gameLoaded() {
      mainHandler.post(() -> GameWebView.this.gameLoaded());
    }

    @JavascriptInterface public void playSound() {
      mainHandler.post(() -> GameWebView.this.playSound());
    }
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void start(Game game,
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs,
      Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_webv1_";
    loader.start();
    layoutProgress.setBackgroundColor(Color.parseColor("#" + game.getPrimary_color()));
    super.start(game, masterMapObs, mapObservable, mapInvitedObservable, liveViewsObservable,
        userId);
    if (!StringUtils.isEmpty(game.getUrl())) {
      webView.loadUrl(game.getUrl());
      //webView.onPause();
      webView.setWebViewClient(new WebViewClient() {
        @Override public void onPageFinished(WebView view, String url) {
          loader.setNotifEventListener(() -> {
            super.onPageFinished(view, url);
            //   webView.setVisibility(VISIBLE);
            //  webView.onResume();
          });
          loader.finish();
        }
      });
      //   webView.setVisibility(GONE);
    }
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
    webView.loadUrl("about:blank");
    webView.setWebViewClient(null);
    webView.setWebChromeClient(null);
    webView.removeJavascriptInterface("androidInterface");
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */
}
