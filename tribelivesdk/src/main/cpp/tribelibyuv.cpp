#include <libyuv.h>
#include <stdio.h>

#ifdef __ANDROID__
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "tribelibyuv::", __VA_ARGS__))

#include <jni.h>
#include <android/log.h>

#endif

using namespace libyuv;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_yuvToRgb(JNIEnv *env, jobject,
                                                            jbyteArray yuvArray,
                                                            jint width,
                                                            jint height,
                                                            jintArray argbArray,
                                                            jbyteArray yuvOutArray) {

    jbyte *yuv = (jbyte *) env->GetPrimitiveArrayCritical(yuvArray, 0);
    uint8_t *rgbData = (uint8_t *) env->GetPrimitiveArrayCritical(argbArray, 0);
    jbyte *yuvOut = (jbyte *) env->GetPrimitiveArrayCritical(yuvOutArray, 0);

    const uint8 *src_y = (uint8 *) yuv;
    int src_stride_y = width;
    const uint8 *src_vu = src_y + width * height;
    int src_stride_vu = width;
    int dst_stride_argb = width << 2;

    int r = NV21ToARGB(src_y, src_stride_y,
                       src_vu, src_stride_vu,
                       rgbData, dst_stride_argb,
                       width, height);


    int src_stride_argb = width << 2;
    uint8 *dst_y = (uint8 *) yuvOut;
    int dst_stride_y = width;
    uint8 *dst_vu = dst_y + width * height;
    int dst_stride_vu = width;

    r = ARGBToNV21(rgbData, src_stride_argb,
                   dst_y, dst_stride_y,
                   dst_vu, dst_stride_vu,
                   width, height);

    env->ReleasePrimitiveArrayCritical(yuvArray, yuv, 0);
    env->ReleasePrimitiveArrayCritical(argbArray, rgbData, 0);
    env->ReleasePrimitiveArrayCritical(yuvOutArray, yuvOut, 0);

    return r;
}

int *rgbData;
int rgbDataSize = 0;

JNIEXPORT
void
JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_YUVtoRBG(JNIEnv *env, jobject, jintArray rgb,
                                                            jbyteArray yuv420sp, jint width,
                                                            jint height) {
    int sz;
    int i;
    int j;
    int Y;
    int Cr = 0;
    int Cb = 0;
    int pixPtr = 0;
    int jDiv2 = 0;
    int R = 0;
    int G = 0;
    int B = 0;
    int cOff;
    int w = width;
    int h = height;
    sz = w * h;

    LOGI("HEY");
    jbyte *yuv = env->GetByteArrayElements(yuv420sp, NULL);

    if (rgbDataSize < sz) {
        int tmp[sz];
        rgbData = &tmp[0];
        rgbDataSize = sz;
    }

    LOGI("yuv : %s", yuv);
    LOGI("rgbDataSize : %d", rgbDataSize);
    LOGI("rgbData : %s", rgbData);

    LOGI("YO");
    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }
            R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);//1.406*~1.403
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) +
                (Cr >> 5);//
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);//1.765~1.770
            if (B < 0) B = 0; else if (B > 255) B = 255;
            rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
        }
    }

    LOGI("WAZZZA");

    env->SetIntArrayRegion(rgb, 0, sz, &rgbData[0]);

    env->ReleaseByteArrayElements(yuv420sp, yuv, JNI_ABORT);

    LOGI("DONE");
}

} // extern "C"

