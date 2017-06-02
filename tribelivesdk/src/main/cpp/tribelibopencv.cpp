#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define  LOG_TAG    "Tribe"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace std;
using namespace cv;

extern "C" {

jstring
Java_com_tribe_tribelivesdk_opencv_OpenCVWrapper_stringFromJNI(JNIEnv *env,
                                                               jobject) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_tribe_tribelivesdk_opencv_OpenCVWrapper_addPostIt(JNIEnv *env, jobject,
                                                           jbyteArray argbIn,
                                                           jint frameWidth,
                                                           jint frameHeight,
                                                           jintArray postIt,
                                                           jint postItWidth,
                                                           jint postItHeight,
                                                           jbyteArray argbOut) {
    jbyte *argbInData = env->GetByteArrayElements(argbIn, 0);
    jbyte *argbOutData = env->GetByteArrayElements(argbOut, 0);
    jint *postItData = env->GetIntArrayElements(postIt, 0);

    cv::Mat _rgbaIn(frameHeight, frameWidth, CV_8UC4, (uchar *) argbInData);
    cv::Mat _rgbaOut(frameHeight, frameWidth, CV_8UC4, (uchar *) argbOutData);
    cv::Mat _postIt(postItHeight, postItWidth, CV_8UC4, (uchar *) postItData);

    //LOGD("Channels _rgbaIn: %d", _rgbaIn.channels());
    //LOGD("Channels _rgbaOut: %d", _rgbaOut.channels());
    //LOGD("Channels _postIt: %d", _postIt.channels());

    //LOGD("Cols _rgbaIn: %d", _rgbaIn.cols);
    //LOGD("Rows _rgbaIn: %d", _rgbaOut.rows);
    //LOGD("Cols _postIt: %d", _postIt.cols);
    //LOGD("Rows _postIt: %d", _postIt.rows);

    cv::Point2i location(100, 100);
    _rgbaIn.copyTo(_rgbaOut);

    // start at the row indicated by location, or at row 0 if location.y is negative.
    for (int y = std::max(location.y, 0); y < _rgbaIn.rows; ++y) {
        int fY = y - location.y; // because of the translation

        // we are done of we have processed all rows of the foreground image.
        if (fY >= _postIt.rows)
            break;

        // start at the column indicated by location,

        // or at column 0 if location.x is negative.
        for (int x = std::max(location.x, 0); x < _rgbaIn.cols; ++x) {
            int fX = x - location.x; // because of the translation.

            // we are done with this row if the column is outside of the foreground image.
            if (fX >= _postIt.cols)
                break;

            // determine the opacity of the foregrond pixel, using its fourth (alpha) channel.
            double opacity =
                    ((double) _postIt.data[fY * _postIt.step + fX * _postIt.channels() +
                                           3])

                    / 255.;


            // and now combine the background and foreground pixel, using the opacity,

            // but only if opacity > 0.
            for (int c = 0; opacity > 0 && c < _rgbaOut.channels(); ++c) {
                unsigned char foregroundPx =
                        _postIt.data[fY * _postIt.step + fX * _postIt.channels() + c];
                unsigned char backgroundPx =
                        _rgbaIn.data[y * _rgbaIn.step + x * _rgbaIn.channels() + c];
                _rgbaOut.data[y * _rgbaOut.step + _rgbaOut.channels() * x + c] =
                        backgroundPx * (1. - opacity) + foregroundPx * opacity;
            }
        }
    }

    //cv::Mat roi = _rgbaIn(cv::Rect(0, 0, _postIt.cols, _postIt.rows));
    //LOGD("Channels _rgbaIn(roi): %d", _rgbaIn(roi).channels());
    //LOGD("Size _rgbaIn(roi): %s", _rgbaIn(roi).size);
    //cv::addWeighted(_rgbaIn(roi), 0, _postIt, 1, 0, _rgbaOut);

    env->ReleaseByteArrayElements(argbIn, argbInData, 0);
    env->ReleaseIntArrayElements(postIt, postItData, 0);
    env->ReleaseByteArrayElements(argbOut, argbOutData, 0);

    return true;
}

} // end of extern "C" (global C/C++ functions that aren't part of a C++ Class)