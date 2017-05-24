package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v8.renderscript.RenderScript;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.rs.RSCompute;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilterWrapper;
import com.tribe.tribelivesdk.util.BitmapUtils;
import com.tribe.tribelivesdk.util.ByteBuffers;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeVideoRenderer;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import org.webrtc.VideoCapturer;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

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
  private Bitmap bitmapLocalPostIt, bitmapRemotePostIt, tempBitmap, canvasBitmap;
  private Canvas canvas;
  private int[] argb;
  private byte[] yuvOut;
  private byte[] yuvOutLocal;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private boolean firstFrame = true;
  private int previousWidth, previousHeight = 0;
  private boolean processing = false;
  private VideoCapturer.CapturerObserver capturerObserver;
  private TribeVideoRenderer localRenderer;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onLocalFrame = PublishSubject.create();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();

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

          if (firstFrame
              || previousWidth != frame.getWidth()
              || previousHeight != frame.getHeight()) {
            Timber.d("New type of frame with width : "
                + frame.getWidth()
                + " / height : "
                + frame.getHeight());
            rsCompute = new RSCompute(context, renderScript, frame.getWidth(), frame.getHeight());
            tempBitmap =
                Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
            argb = new int[frame.getWidth() * frame.getHeight()];
            yuvOut = new byte[frame.getData().length];
            yuvOutLocal = new byte[frame.getData().length];
            yuvStrides = new int[3];
            yuvStrides[0] = frame.getWidth();
            yuvStrides[1] = (frame.getWidth() + 1) / 2;
            yuvStrides[2] = (frame.getWidth() + 1) / 2;

            int chroma_height = (frame.getHeight() + 1) / 2;
            if (byteBufferYuv != null) byteBufferYuv.clear();
            byteBufferYuv = ByteBuffer.allocateDirect(yuvOut.length);
            yuvPlanes = ByteBuffers.slice(byteBufferYuv, yuvStrides[0] * frame.getHeight(),
                yuvStrides[1] * chroma_height, yuvStrides[2] * chroma_height);
            firstFrame = false;
            previousHeight = frame.getHeight();
            previousWidth = frame.getWidth();

            Bitmap postIt = BitmapUtils.generateNewPostIt(context, "?",
                context.getResources().getDimensionPixelSize(R.dimen.textsize_post_it), Color.WHITE,
                R.drawable.bg_post_it);
            Matrix localMatrix = new Matrix();
            localMatrix.postScale(-1, 1);
            localMatrix.postRotate(-frame.getRotation());
            bitmapLocalPostIt =
                Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(),
                    localMatrix, true);

            Matrix remoteMatrix = new Matrix();
            remoteMatrix.postRotate(-frame.getRotation());
            bitmapRemotePostIt =
                Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(),
                    localMatrix, true);

            canvasBitmap =
                Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(canvasBitmap);
          }

          LUT3DFilter filter = lut3DFilterWrapper.getFilter();
          if (!filter.getId().equals(LUT3DFilter.LUT3D_NONE)) {
            long stepStart = System.nanoTime();
            libYuvConverter.YUVToARGB(frame.getData(), frame.getWidth(), frame.getHeight(), argb);
            long stepYuvToARGB = System.nanoTime();
            Timber.d("stepYuvToARGB : " + (stepYuvToARGB - stepStart) / 1000000.0f + " ms");
            //rsCompute.computeLUT3D(filter, argb, frame.getWidth(), frame.getHeight(), argb);
            //Bitmap bmp = tempBitmap;
            //bmp.setPixels(argb, 0, frame.getWidth(), 0, 0, frame.getWidth(), frame.getHeight());
            //BitmapUtils.addPostItToFrame(canvas, bmp, bitmapPostIt);
            //canvasBitmap.getPixels(argb, 0, frame.getWidth(), 0, 0, frame.getWidth(),
            //    frame.getHeight());
            //long stepLUT3D = System.nanoTime();
            //Timber.d("stepLUT3D : " + (stepLUT3D - stepYuvToARGB) / 1000000.0f + " ms");

            //long stepARGBToYUV = System.nanoTime();
            //Timber.d(
            //    "stepARGBToYUV : " + (stepARGBToYUV - stepLUT3D) / 1000000.0f + " ms");
            //Timber.d(
            //    "Total : " + (stepARGBToYUV - stepStart) / 1000000.0f + " ms");

            //if (localRenderer != null && argb != null && yuvOutLocal != null) {
            //  libYuvConverter.ARGBToI420(argb, frame.getWidth(), frame.getHeight(), yuvOutLocal);
            //  byteBufferYuv.put(yuvOutLocal);
            //  byteBufferYuv.flip();
            //  localRenderer.renderFrame(
            //      new TribeI420Frame(frame.getWidth(), frame.getHeight(), frame.getRotation(),
            //          yuvStrides, yuvPlanes));
            //}

            libYuvConverter.ARGBToYUV(argb, frame.getWidth(), frame.getHeight(), yuvOut);
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
    firstFrame = true;
    subscriptions.clear();
  }
}
