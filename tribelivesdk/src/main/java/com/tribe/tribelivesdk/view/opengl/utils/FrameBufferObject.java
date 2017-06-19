package com.tribe.tribelivesdk.view.opengl.utils;

import android.opengl.GLES20;
import android.util.Log;

public class FrameBufferObject {

  private int frameBufferID;

  public FrameBufferObject() {
    int[] buf = new int[1];
    GLES20.glGenFramebuffers(1, buf, 0);
    frameBufferID = buf[0];
  }

  public void release() {
    GLES20.glDeleteBuffers(1, new int[] { frameBufferID }, 0);
  }

  public void bind() {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferID);
  }

  public void bindTexture(int texID) {
    bind();
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
        GLES20.GL_TEXTURE_2D, texID, 0);
    if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
      Log.e(Common.LOG_TAG, "CGE::FrameBuffer::bindTexture2D - Frame buffer is not valid!");
    }
  }
}
