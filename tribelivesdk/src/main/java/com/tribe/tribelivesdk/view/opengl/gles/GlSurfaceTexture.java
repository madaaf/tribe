package com.tribe.tribelivesdk.view.opengl.gles;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.support.annotation.Nullable;
import rx.Observable;
import rx.subjects.PublishSubject;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

public final class GlSurfaceTexture
    implements PreviewTextureInterface, SurfaceTexture.OnFrameAvailableListener {

  @Nullable private SurfaceTexture surfaceTexture;
  private OnFrameAvailableListener onFrameAvailableListener;

  private int texName;
  final int[] textures = new int[1];

  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();

  public GlSurfaceTexture() {

  }

  @Override public void setOnFrameAvailableListener(final OnFrameAvailableListener l) {
    onFrameAvailableListener = l;
  }

  @Override public int getTextureId() {
    return texName;
  }

  public void init() {

    glGenTextures(textures.length, textures, 0);
    this.texName = textures[0];

    surfaceTexture = new SurfaceTexture(texName);
    surfaceTexture.setOnFrameAvailableListener(this);
  }

  @Override public void release() {
    if (surfaceTexture != null) {
      surfaceTexture.release();
      surfaceTexture = null;

      glDeleteTextures(textures.length, textures, 0);
    }
  }

  @Override public int getTextureTarget() {
    return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
  }

  @Override public long getTimestamp() {
    return surfaceTexture.getTimestamp();
  }

  @Override public void setup() {
    release();
    init();

    glBindTexture(getTextureTarget(), texName);

    glTexParameterf(getTextureTarget(), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(getTextureTarget(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glBindTexture(GL_TEXTURE_2D, 0);

    onSurfaceTextureReady.onNext(surfaceTexture);
  }

  @Override public void updateTexImage() {
    surfaceTexture.updateTexImage();
  }

  @Override public void getTransformMatrix(final float[] mtx) {
    surfaceTexture.getTransformMatrix(mtx);
  }

  @Override public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
    if (onFrameAvailableListener != null) {
      onFrameAvailableListener.onFrameAvailable(this);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  @Override public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return onSurfaceTextureReady;
  }
}
