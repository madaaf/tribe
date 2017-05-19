package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.rs.RSCompute;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilterWrapper;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.concurrent.Executors;
import org.webrtc.VideoCapturer;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2017.
 */

public class FrameManager {

  // VARIABLES
  private Context context;
  private LibYuvConverter libYuvConverter;
  private RenderScript renderScript;
  private RSCompute rsCompute;
  private LUT3DFilterWrapper lut3DFilterWrapper;
  private byte[] argb;
  private byte[] yuvOut;
  private boolean firstFrame = true;
  private boolean processing = false;
  private VideoCapturer.CapturerObserver capturerObserver;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public FrameManager(Context context, VideoCapturer.CapturerObserver capturerObserver) {
    this.capturerObserver = capturerObserver;
    this.context = context;
    renderScript = RenderScript.create(context);
    libYuvConverter = new LibYuvConverter();
    lut3DFilterWrapper = new LUT3DFilterWrapper(context, renderScript);
  }

  public void initFrameSubscription(Observable<Frame> onFrame) {
    subscriptions.add(onFrame.subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .filter(frame -> !processing)
        .doOnNext(frame -> {
          processing = true;

          if (firstFrame) {
            rsCompute = new RSCompute(context, renderScript, frame.getWidth(), frame.getHeight());
            argb = new byte[frame.getWidth() * frame.getHeight() * 4];
            yuvOut = new byte[frame.getData().length];
            firstFrame = false;
          }

          LUT3DFilter filter = lut3DFilterWrapper.getFilter();
          if (!filter.getId().equals(LUT3DFilter.LUT3D_NONE)) {
            //long stepStart = System.nanoTime();
            libYuvConverter.YUVToARGB(frame.getData(), frame.getWidth(), frame.getHeight(), argb);
            //long stepYuvToARGB = System.nanoTime();
            //Timber.d("stepYuvToARGB : " + (stepYuvToARGB - stepStart) / 1000000.0f + " ms");
            rsCompute.computeLUT3D(filter, argb, frame.getWidth(), frame.getHeight(), argb);
            //long stepLUT3D = System.nanoTime();
            //Timber.d("stepLUT3D : " + (stepLUT3D - stepYuvToARGB) / 1000000.0f + " ms");
            libYuvConverter.ARGBToYUV(argb, frame.getWidth(), frame.getHeight(), yuvOut);
            //long stepARGBToYUV = System.nanoTime();
            //Timber.d(
            //    "stepARGBToYUV : " + (stepARGBToYUV - stepLUT3D) / 1000000.0f + " ms");
            //Timber.d(
            //    "Total : " + (stepARGBToYUV - stepStart) / 1000000.0f + " ms");

            capturerObserver.onByteBufferFrameCaptured(yuvOut, frame.getWidth(), frame.getHeight(),
                frame.getRotation(), frame.getTimestamp());
          } else {
            capturerObserver.onByteBufferFrameCaptured(frame.getData(), frame.getWidth(),
                frame.getHeight(), frame.getRotation(), frame.getTimestamp());
          }
        })
        .subscribe(frame -> processing = false));
  }

  public void switchFilter() {
    lut3DFilterWrapper.switchFilter();
  }

  public void dispose() {
    subscriptions.clear();
  }
}
