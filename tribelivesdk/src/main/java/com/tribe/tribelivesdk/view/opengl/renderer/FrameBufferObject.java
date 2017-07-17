package com.tribe.tribelivesdk.view.opengl.renderer;

import android.opengl.GLES20;
import timber.log.Timber;

public class FrameBufferObject {
  private int framebufferID;

  public FrameBufferObject() {
    int[] buf = new int[1];
    GLES20.glGenFramebuffers (1, buf, 0);
    framebufferID = buf[0];
  }

  public void release() {
    GLES20.glDeleteFramebuffers(1, new int[] { framebufferID }, 0);
  }

  public void bind() {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferID);
  }

  public void bindTexture(int texID) {
    bind();
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
        GLES20.GL_TEXTURE_2D, texID, 0);
    int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
    if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
      Timber.e("CGE::FrameBuffer::bindTexture2D - Frame buffer is not valid! : " + status);
    }
  }
}
