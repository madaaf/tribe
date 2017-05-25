#include <libyuv.h>

#ifdef __ANDROID__
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "tribelibyuv::", __VA_ARGS__))

#include <jni.h>

#endif

using namespace libyuv;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_YUVToARGB(JNIEnv *env, jobject,
                                                             jbyteArray yuvArray,
                                                             jint width,
                                                             jint height,
                                                             jbyteArray argbArray) {

    jbyte *yuv = (jbyte *) env->GetPrimitiveArrayCritical(yuvArray, 0);
    jbyte *rgbData = (jbyte *) env->GetPrimitiveArrayCritical(argbArray, 0);

    const uint8 *src_y = (uint8 *) yuv;
    int src_stride_y = width;
    const uint8 *src_vu = src_y + width * height;
    int src_stride_vu = width;
    int dst_stride_argb = width << 2;
    uint8 *dstData = (uint8 *) rgbData;

    int r = NV21ToARGB(src_y, src_stride_y,
                       src_vu, src_stride_vu,
                       dstData, dst_stride_argb,
                       width, height);

    env->ReleasePrimitiveArrayCritical(yuvArray, yuv, 0);
    env->ReleasePrimitiveArrayCritical(argbArray, rgbData, 0);

    return r;
}

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_ARGBToYUV(JNIEnv *env, jobject,
                                                             jbyteArray argbArray,
                                                             jint width,
                                                             jint height,
                                                             jbyteArray yuvOutArray) {

    jbyte *rgbData = (jbyte *) env->GetPrimitiveArrayCritical(argbArray, 0);
    jbyte *yuvOut = (jbyte *) env->GetPrimitiveArrayCritical(yuvOutArray, 0);

    const uint8 *srcData = (uint8 *) rgbData;
    int src_stride_argb = width << 2;
    uint8 *dst_y = (uint8 *) yuvOut;
    int dst_stride_y = width;
    uint8 *dst_vu = dst_y + width * height;
    int dst_stride_vu = width;

    int r = ARGBToNV21(srcData, src_stride_argb,
                       dst_y, dst_stride_y,
                       dst_vu, dst_stride_vu,
                       width, height);

    env->ReleasePrimitiveArrayCritical(argbArray, rgbData, 0);
    env->ReleasePrimitiveArrayCritical(yuvOutArray, yuvOut, 0);

    return r;
}

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_ARGBToI420(JNIEnv *env, jobject,
                                                              jbyteArray argbArray,
                                                              jint width,
                                                              jint height,
                                                              jbyteArray yuvOutArray) {

    jbyte *rgbData = (jbyte *) env->GetPrimitiveArrayCritical(argbArray, 0);
    jbyte *yuvData = (jbyte *) env->GetPrimitiveArrayCritical(yuvOutArray, 0);

    const uint8 *srcData = (uint8 *) rgbData;
    int src_stride_argb = width * 4;
    uint8 *dst_y = (uint8 *) yuvData;
    int dst_stride_y = width;
    uint8 *dst_u = dst_y + dst_stride_y * height;
    int dst_stride_u = (width + 1) / 2;
    uint8 *dst_v = dst_y + dst_stride_y * height + dst_stride_u * ((height + 1) / 2);
    int dst_stride_v = (width + 1) / 2;

    int r = ARGBToI420(srcData, src_stride_argb,
                       dst_y, dst_stride_y,
                       dst_u, dst_stride_u,
                       dst_v, dst_stride_v,
                       width, height);

    env->ReleasePrimitiveArrayCritical(argbArray, rgbData, 0);
    env->ReleasePrimitiveArrayCritical(yuvOutArray, yuvData, 0);

    return r;
}

JNIEXPORT jint JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_nativeCopyPlane(JNIEnv *jni, jclass,
                                                                   jobject j_src_buffer, jint width,
                                                                   jint height,
                                                                   jint src_stride,
                                                                   jobject j_dst_buffer,
                                                                   jint dst_stride) {
    uint8_t *src =
            reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(j_src_buffer));
    uint8_t *dst =
            reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(j_dst_buffer));
    if (src_stride == dst_stride) {
        memcpy(dst, src, src_stride * height);
    } else {
        for (int i = 0; i < height; i++) {
            memcpy(dst, src, width);
            src += src_stride;
            dst += dst_stride;
        }
    }
}

} // extern "C"

