package com.tribe.tribelivesdk.view.opengl.objloader;

import com.uls.gl.Mesh;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

public class TDModelPart extends Mesh {

  Vector<Short> faces;
  Vector<Short> vtPointer;
  Vector<Short> vnPointer;
  Material material;
  private FloatBuffer normalBuffer;
  ShortBuffer faceBuffer;

  private float[] verticsArrayOrg;
  private float[] verticsArrayTmp;

  public TDModelPart(Vector<Short> faces, Vector<Short> vtPointer, Vector<Short> vnPointer,
      Material material, Vector<Float> vn) {
    super();
    this.faces = faces;
    this.vtPointer = vtPointer;
    this.vnPointer = vnPointer;
    this.material = material;

    ByteBuffer byteBuf = ByteBuffer.allocateDirect(vnPointer.size() * 4 * 3);
    byteBuf.order(ByteOrder.nativeOrder());
    normalBuffer = byteBuf.asFloatBuffer();
    for (int i = 0; i < vnPointer.size(); i++) {
      float x = vn.get(vnPointer.get(i) * 3);
      float y = vn.get(vnPointer.get(i) * 3 + 1);
      float z = vn.get(vnPointer.get(i) * 3 + 2);
      normalBuffer.put(x);
      normalBuffer.put(y);
      normalBuffer.put(z);
    }
    normalBuffer.position(0);

    ByteBuffer fBuf = ByteBuffer.allocateDirect(faces.size() * 2);
    fBuf.order(ByteOrder.nativeOrder());
    faceBuffer = fBuf.asShortBuffer();
    faceBuffer.put(toPrimitiveArrayS(faces));
    faceBuffer.position(0);
  }

  public void setIVT(Vector<Short> faces, Vector<Float> v, Vector<Float> vt) {
    float[] vta = new float[vt.size() * 2 / 3];
    for (int i = 0; i < vt.size() / 3; i++) {
      vta[i * 2 + 0] = vt.get(i * 3 + 0);
      vta[i * 2 + 1] = vt.get(i * 3 + 1);
    }
    this.setCurveTextureCoordinates(vta);

    short[] fa = new short[faces.size()];
    for (int i = 0; i < faces.size(); i++)
      fa[i] = faces.get(i);
    this.setIndices(fa);

    verticsArrayOrg = new float[v.size()];
    verticsArrayTmp = new float[v.size()];
    for (int i = 0; i < v.size(); i += 3) {
      verticsArrayOrg[i + 0] = v.get(i + 0);//x
      // y z flip
      verticsArrayOrg[i + 1] = v.get(i + 2);//y
      verticsArrayOrg[i + 2] = 0;//z
    }
    this.setVertices(verticsArrayOrg);
  }

  public void setCurveVerticesWHScale(float w, float h) {
    mOrgScaleParam_W = w / ImageHeight;
    //mOrgScaleParam_H = orgRatio * h/w;
    mOrgScaleParam_H = h / ImageWidth;
    for (int i = 0; i < verticsArrayOrg.length / 3; i++) {
      verticsArrayTmp[i * 3 + 0] = verticsArrayOrg[i * 3 + 0] * mOrgScaleParam_W;
      verticsArrayTmp[i * 3 + 1] = verticsArrayOrg[i * 3 + 1] * mOrgScaleParam_H * 2.0f;
    }

    this.setVertices(verticsArrayTmp);
  }

  public void setCurveTrackScale(float trackScale) {
    mTrackScale = trackScale / 11.0f;// for jason version
    for (int i = 0; i < verticsArrayOrg.length / 3; i++) {
      verticsArrayTmp[i * 3 + 0] = verticsArrayOrg[i * 3 + 0] * mOrgScaleParam_W * mTrackScale;
      verticsArrayTmp[i * 3 + 2] = verticsArrayOrg[i * 3 + 2] * mTrackScale;
      verticsArrayTmp[i * 3 + 1] =
          verticsArrayOrg[i * 3 + 1] * mOrgScaleParam_H * mTrackScale * 2.0f;
    }

    this.setVertices(verticsArrayTmp);
  }

  public String toString() {
    String str = new String();
    if (material != null) {
      str += "Material name:" + material.getName();
    } else {
      str += "Material not defined!";
    }
    str += "\nNumber of faces:" + faces.size();
    str += "\nNumber of vnPointers:" + vnPointer.size();
    str += "\nNumber of vtPointers:" + vtPointer.size();
    return str;
  }

  public ShortBuffer getFaceBuffer() {
    return faceBuffer;
  }

  public FloatBuffer getNormalBuffer() {
    return normalBuffer;
  }

  private static short[] toPrimitiveArrayS(Vector<Short> vector) {
    short[] s;
    s = new short[vector.size()];
    for (int i = 0; i < vector.size(); i++) {
      s[i] = vector.get(i);
    }
    return s;
  }

  public int getFacesCount() {
    return faces.size();
  }

  public Material getMaterial() {
    return material;
  }
}
