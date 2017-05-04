#include <jni.h>
#include <libyuv.h>
#include <android/log.h>
#include <stdio.h>

#ifdef __ANDROID__

#include "android.h"

using namespace libyuv;

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "tribelibyuv::", __VA_ARGS__))

bool CheckException(JNIEnv *jni) {
    if (jni->ExceptionCheck()) {
        jni->ExceptionDescribe();
        jni->ExceptionClear();
        return true;
    }
    return false;
}

extern "C" {

FILE *outfile;

//JNIEXPORT void JNICALL Java_com_jerikc_android_demo_camera_LibyuvCore_init
//        (JNIEnv *env, jobject jThis, jint src_w, jint src_h, jint dst_w, jint dst_h, jstring url) {
//    const char *output_file_path = env->GetStringUTFChars(url, (jboolean) 0);
//
//    pYuvCoreContext = (YuvCoreContext *) malloc(sizeof(YuvCoreContext));
//    pYuvCoreContext->src_width = src_w;
//    pYuvCoreContext->src_height = src_h;
//    pYuvCoreContext->dst_width = dst_w;
//    pYuvCoreContext->dst_height = dst_h;
//    pYuvCoreContext->src_frame = NULL;
//    pYuvCoreContext->src_size = 0;
//    pYuvCoreContext->i420 = NULL;
//    pYuvCoreContext->i420_size = 0;
//
//    pYuvCoreContext->outfile = fopen(output_file_path, "wb");
//    if (!pYuvCoreContext->outfile) {
//        free(pYuvCoreContext);
//        throwJavaException(env, "java/io/IOException",
//                           "Could not open the output file");
//        return;
//    }
//    (*env)->ReleaseStringUTFChars(env, url, output_file_path);
//
//}

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_yuvToRgb(JNIEnv *env, jobject,
                                                            jbyteArray yuvArray,
                                                            jint width,
                                                            jint height,
                                                            jbyteArray argbArray) {

//    jbyte *yuv = env->GetByteArrayElements(yuvArray, NULL);
//
//    if (CheckException(env)) return -1;
//
//    jbyte *rgb = env->GetByteArrayElements(argbArray, NULL);
//
//    if (CheckException(env)) return -1;
//
//    const uint8_t *src_y = (uint8_t *) yuv;
//    const uint8_t *src_vu = (uint8_t *) yuv + (width * height);
//    int src_stride_y = width;
//    int src_stride_vu = (width + 1) >> 1;
//
//    return libyuv::NV12ToARGB(src_y, src_stride_y,
//                              src_vu, src_stride_vu,
//                              (uint8_t *) rgb, width << 2,
//                              width, height);

    ll2p
    jbyte *yuv = env->GetByteArrayElements(yuvArray, NULL);
    jbyte *rgb = env->GetByteArrayElements(argbArray, NULL);

    LOGI("HEYAAAAA");

    int src_stride_y = width;
    uint8 *src_vu = (uint8 *) yuv + width * height;
    uint8 *src_y = (uint8 *) yuv;
    uint8 *dst_rgb = (uint8 *) rgb;
    int src_stride_vu = width;
    int dst_stride_argb = width * sizeof(int);

    LOGI("OYEE SAPAPAYAAAA %s", rgb);

    int r = libyuv::NV21ToARGB(src_y, src_stride_y, src_vu, src_stride_vu, dst_rgb,
                               dst_stride_argb, width, height);
    LOGI("Return code : %d", +r);

    LOGI("OYEE SAPAPAYAAAA 22222 %s", rgb);

    env->ReleaseByteArrayElements(yuvArray, yuv, 0);
    env->ReleaseByteArrayElements(argbArray, rgb, 0);

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

