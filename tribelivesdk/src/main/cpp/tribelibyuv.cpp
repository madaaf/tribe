#include <libyuv.h>
#include <string.h>
#include <sys/mman.h>

#define valloc(size, prot) mmap(NULL, (size), (prot), MAP_PRIVATE | MAP_ANON, -1, 0);
#define vfree(mem, size)  munmap(mem, size)
#define vlock(mem, size) mlock((mem), (size))

#define BUFSIZE (4*1024)

#ifdef __ANDROID__
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "tribelibyuv::", __VA_ARGS__))

#include <jni.h>
#include <android/log.h>

#endif

#define GL_GLEXT_PROTOTYPES 1
#define GL3_PROTOTYPES 1

#include <GLES3/gl3.h>
#include <Timer.h>

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

    const uint8 *srcDataRGBA = (uint8 *) rgbData;
    int src_stride_rgba = width << 2;
    uint8 *dstDataARGB = (uint8 *) rgbData;
    int dst_stride_argb = width << 2;

    int r1 = ABGRToARGB(srcDataRGBA, src_stride_rgba, dstDataARGB, dst_stride_argb, width,
                        height);

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

JNIEXPORT void JNICALL
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

// Consts
const int PBO_COUNT = 3;

// Global variables
GLuint pboIds[PBO_COUNT];
int vram2sys;         // index of PBO used to copy from vram to sysmem
int gpu2vram;         // index of PBO used to copy framebuffer to vram
jint nbBytes;
unsigned char *ptr;
Timer t1;
float readTime, processTime;


JNIEXPORT void JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_initPBO(JNIEnv *jni, jclass, jint width,
                                                           jint height) {
    nbBytes = width * height * 4;
    glGenBuffers(PBO_COUNT, pboIds);

    for (int i = 0; i < PBO_COUNT; ++i) {
        //LOGI("pbodownloader.pbos[%d] = %d, nbytes: %d", i, pboIds[i], nbBytes);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[i]);
        glBufferData(GL_PIXEL_PACK_BUFFER, nbBytes, NULL, GL_STREAM_READ);
    }

    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    vram2sys = 0;
    gpu2vram = PBO_COUNT - 1;
}

JNIEXPORT void JNICALL
Java_com_tribe_tribelivesdk_libyuv_LibYuvConverter_readFromPBO(JNIEnv *jni, jclass, jobject buffer,
                                                               jint width,
                                                               jint height) {
    uint8_t *dst =
            reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(buffer));

    t1.start();

    glBindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[gpu2vram]);
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);

    glBindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[vram2sys]);

    ptr = (unsigned char *) glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, nbBytes,
                                             GL_MAP_READ_BIT);
    if (ptr != NULL) {
        memcpy(dst, ptr, nbBytes);
    }

    glUnmapBuffer(GL_PIXEL_PACK_BUFFER);

    t1.stop();
    processTime = t1.getElapsedTimeInMilliSec();

    LOGI("processTime %f ms", processTime);

    // shift names
    GLuint temp = pboIds[0];
    for (int i = 1; i < PBO_COUNT; i++)
        pboIds[i - 1] = pboIds[i];
    pboIds[PBO_COUNT - 1] = temp;

    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

} // extern "C"

