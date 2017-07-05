package com.tribe.tribelivesdk.view.opengl.gles;

import com.uls.gl.Mesh;

public class SimplePlane extends Mesh {

  float textureCoordinatesForSticker[] = {
      1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
  };

  short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };

  private float[] OrgVertices = new float[] {
      1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, // x ,y, z
      1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
  };

  private float[] TrackVertices = new float[] {
      -1.0f, -1.0f, 0.0f, // x ,y, z
      1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f
  };

  public SimplePlane(float width, float height) {

    if (width != height) {
      textureCoordinatesForSticker[1] *= (width / height);

      textureCoordinatesForSticker[3] *= (width / height);
    }
    // Mapping coordinates for the vertices
    this.setTextureCoordinates(textureCoordinatesForSticker);
    this.setIndices(indices);
    this.setVertices(OrgVertices);
  }

  public void setVerticesWHScale(float w, float h) {
    mOrgScaleParam_W = w / (ImageHeight / 2);
    mOrgScaleParam_H = h / (ImageHeight / 2);
    for (int i = 0; i < 4; i++) {
      TrackVertices[i * 3 + 0] = OrgVertices[i * 3 + 0] * mOrgScaleParam_W;
      TrackVertices[i * 3 + 1] = OrgVertices[i * 3 + 1] * mOrgScaleParam_H;
    }

    this.setVertices(TrackVertices);
  }

  public void setVerticesWHScaleInZ(float w, float h) {
    mOrgScaleParam_W = w / ImageHeight;
    mOrgScaleParam_H = h / ImageHeight;
    for (int i = 0; i < 4; i++) {
      TrackVertices[i * 3 + 2] = OrgVertices[i * 3 + 2] * mOrgScaleParam_W;
      TrackVertices[i * 3 + 1] = OrgVertices[i * 3 + 1] * mOrgScaleParam_H;
    }
    this.setVertices(TrackVertices);
  }

  public void setUlsTrackScale(float trackScale) {
    mTrackScale = (trackScale / 11.0f);
    for (int i = 0; i < 4; i++) {
      TrackVertices[i * 3 + 0] = OrgVertices[i * 3 + 0] * mOrgScaleParam_W * mTrackScale;
      TrackVertices[i * 3 + 1] = OrgVertices[i * 3 + 1] * mOrgScaleParam_H * mTrackScale;
      TrackVertices[i * 3 + 2] = OrgVertices[i * 3 + 2] * mTrackScale;
    }

    this.setVertices(TrackVertices);
  }
}
