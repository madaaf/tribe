package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.util.Pair;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.game.GamePostIt;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.opencv.OpenCVWrapper;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.mask.HeadsUpMaskFilter;
import com.tribe.tribelivesdk.webrtc.Frame;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2017.
 */

public class FrameManager {

  private static final float SCALE = 0.3f;

  // VARIABLES
  private Context context;
  private GameManager gameManager;
  private UlseeManager ulseeManager;
  private LibYuvConverter libYuvConverter;
  private OpenCVWrapper openCVWrapper;
  private FilterManager filterManager;
  private byte[] argb, yuvOut;
  private boolean firstFrame, processing = false;
  private int previousWidth = 0, previousHeight = 0, previousRotation = 0;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription subscriptionGame;
  private Observable<Frame> onNewPreviewFrame;
  private PublishSubject<Frame> onNewFrame = PublishSubject.create();
  private PublishSubject<Frame> onFrameSizeChange = PublishSubject.create();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();

  public FrameManager(Context context) {
    this.context = context;
    libYuvConverter = LibYuvConverter.getInstance();
    openCVWrapper = OpenCVWrapper.getInstance();
    initGameManager();
    initFilterManager();
    initUlseeManager();
  }

  private void initGameManager() {
    gameManager = GameManager.getInstance(context);
    gameManager.initSubscriptions();
    gameManager.initFrameSizeChangeObs(onFrameSizeChange);
    gameManager.initOnNewFrameObs(onNewFrame);
  }

  private void initSubscriptions() {
    subscriptions.add(filterManager.onFilterChange().subscribe(filterMask -> switchFilter()));
    subscriptions.add(gameManager.onRemoteFrame().onBackpressureDrop().subscribe(onRemoteFrame));
    subscriptions.add(gameManager.onGameChange().subscribe(game -> {
      if (game != null && gameManager.isFacialRecognitionNeeded() && subscriptionGame == null) {
        if (game instanceof GamePostIt) {
          filterManager.setCurrentFilter(
              new HeadsUpMaskFilter(context, FaceMaskFilter.FACE_MASK_HEADS_UP, "", -1));
        }

        subscriptionGame = onNewPreviewFrame.onBackpressureDrop()
            .filter(frame -> !processing && gameManager.isLocalFrameDifferent())
            .observeOn(Schedulers.computation())
            .map(frame1 -> {
              processing = true;

              processFrame(frame1);

              libYuvConverter.YUVToARGB(frame1.getData(), frame1.getWidth(), frame1.getHeight(),
                  argb);
              frame1.setData(argb);
              frame1.setDataOut(yuvOut);
              onNewFrame.onNext(frame1);

              return frame1;
            })
            .subscribe(frame -> processing = false);
      } else if (game == null || !gameManager.isFacialRecognitionNeeded()) {
        if (filterManager.getFilter() instanceof HeadsUpMaskFilter) filterManager.setToPrevious();
        clearSubscriptionGame();
      }
    }));
  }

  private void initFilterManager() {
    filterManager = filterManager.getInstance(context);
  }

  private void initUlseeManager() {
    ulseeManager = UlseeManager.getInstance(context);
  }

  public void initFrameSubscription(Observable<Frame> onFrame) {
    subscriptions.add(onFrame.onBackpressureDrop()
        .filter(frame -> !processing && !gameManager.isLocalFrameDifferent())
        .observeOn(Schedulers.computation())
        .map(frame1 -> {
          processing = true;

          processFrame(frame1);

          boolean shouldSendRemoteFrame = true;

          long timeStart = System.nanoTime();
          openCVWrapper.flipBeforeSending(frame1.getData(), argb, frame1.getWidth(),
              frame1.getHeight(), SCALE);
          long timeEndFlip = System.nanoTime();
          //Timber.d("Total time of flipping frame " +
          //    " / " +
          //    (timeEndFlip - timeStart) / 1000000.0f +
          //    " ms");
          libYuvConverter.ABGRToYUV(argb, frame1.getWidth(), frame1.getHeight(), yuvOut);
          frame1.setDataOut(yuvOut);
          long timeEnd = System.nanoTime();
          //Timber.d("Total time of converting frame " +
          //    " / " +
          //    (timeEnd - timeEndFlip) / 1000000.0f +
          //    " ms");

          if (shouldSendRemoteFrame) onRemoteFrame.onNext(frame1);

          return frame1;
        })
        .subscribe(frame -> processing = false));
  }

  public void initPreviewFrameSubscription(Observable<Frame> onPreviewFrame) {
    this.onNewPreviewFrame = onPreviewFrame;
  }

  public void initNewFacesSubscriptions(Observable<Pair<RectF[], int[]>> observable) {
    ulseeManager.initNewFacesSubscription(observable);
  }

  public void switchFilter() {
    if (!(filterManager.getFilter() instanceof FaceMaskFilter)) {
      ulseeManager.dispose();
    } else if (onNewPreviewFrame != null) {
      ulseeManager.initFrameSubscription(onNewPreviewFrame);
    }
  }

  public void dispose() {
    onNewPreviewFrame = null;
    filterManager.dispose();
    ulseeManager.dispose();
    gameManager.dispose();
    clearSubscriptionGame();
  }

  public void startCapture() {
    initSubscriptions();
  }

  public void stopCapture() {
    firstFrame = true;
    subscriptions.clear();
    ulseeManager.stopCapture();
  }

  public void switchCamera() {
    firstFrame = true;
  }

  /////////////
  // PRIVATE //
  /////////////

  private void processFrame(Frame frame) {
    boolean shouldForce = false;

    if (gameManager.getCurrentGame() instanceof GamePostIt) {
      GamePostIt gamePostIt = (GamePostIt) gameManager.getCurrentGame();
      shouldForce = gamePostIt.shouldGenerateNewName();
    }

    if (firstFrame ||
        previousWidth != frame.getWidth() ||
        previousHeight != frame.getHeight() ||
        previousRotation != frame.getRotation() ||
        shouldForce) {
      argb = new byte[frame.getWidth() * frame.getHeight() * 4];
      yuvOut = new byte[frame.getData().length];

      frame.setDataOut(yuvOut);

      firstFrame = false;
      previousHeight = frame.getHeight();
      previousWidth = frame.getWidth();
      previousRotation = frame.getRotation();

      onFrameSizeChange.onNext(frame);
    }
  }

  private void clearSubscriptionGame() {
    if (subscriptionGame != null) {
      subscriptionGame.unsubscribe();
      subscriptionGame = null;
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }
}
