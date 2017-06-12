cmake_minimum_required(VERSION 3.4.1)

# configure import libs
set(jniLibs_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)

add_library(lib_libyuv SHARED IMPORTED)
set_target_properties(lib_libyuv PROPERTIES IMPORTED_LOCATION
    ${jniLibs_DIR}/${ANDROID_ABI}/libyuv.so)

set(pathToOpenCV ../../../../opencvnative)
include_directories(${pathToOpenCV}/jni/include)

add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${jniLibs_DIR}/${ANDROID_ABI}/libopencv_java3.so)

# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library(tribelibyuv SHARED
            tribelibyuv.cpp)

target_include_directories(tribelibyuv PRIVATE
                           ./include
                           ./include)

target_link_libraries(tribelibyuv
                      android
                      lib_libyuv
                      log)

add_library(tribelibopencv
            SHARED
            tribelibopencv.cpp)

find_library(log-lib
             log)

target_link_libraries(tribelibopencv
                      ${log-lib} lib_opencv)