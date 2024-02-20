cmake_minimum_required(VERSION 3.16)

project(FunASRWebscoket) 

set(CMAKE_CXX_STANDARD 14 CACHE STRING "The C++ version to be used.")
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
# set 设置 文件路径啥的 
set(ONNXRUNTIME_DIR /home/sjy/cpp_ws/onnxruntime-android-1.14.0)
 # 设置 ONNXRUNTIME_DIR 的路径是以下的文件路径：/home/sunyujia/cpp_ws/onnxruntime-android-1.14.0
link_directories(${ONNXRUNTIME_DIR}/lib) # 以上路径下的lib文件夹
include_directories(${ONNXRUNTIME_DIR}/include) # 以上路径下的include文件夹

set(FFMPEG_DIR /home/sjy/ffmpeg-master-latest-armv8-gpl-shared)
#  设置 FFMPEG_DIR 的路径是以下的文件路径：/home/sunyujia/cpp_ws/onnxruntime-android-1.14.0
link_directories(${FFMPEG_DIR}/lib) # 以上路径下的lib文件夹
include_directories(${FFMPEG_DIR}/include) # 以上路径下的include文件夹



include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/include/) # 关联文件夹下的.h文件 也就是头文件
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/yaml-cpp/include/)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/kaldi-native-fbank)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/openfst/src/include/)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/src)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/gflags/src)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/kaldi)
include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/jieba/include)
# include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/jieba/include/limonp/)
# include_directories(/home/sjy/ffmpeg-master-latest-armv8-gpl-shared/include)



add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/yaml-cpp yaml-cpp) # 在当前的文件夹下 完成.a文件的生成
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/kaldi-native-fbank/kaldi-native-fbank/csrc csrc)
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/src src)
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/kaldi kaldi)
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/gflags gflags)
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/openfst/src openfst)

include_directories(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/glog)
set(BUILD_TESTING OFF) # 设置不开启
add_subdirectory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/glog glog) # 在当前的文件夹下 完成.a文件的生成

aux_source_directory(${PROJECT_SOURCE_DIR}/../onnxruntime/src src_files) # 查找路径下的所有源文件 保存在src_files这个变量中去！
# aux_source_directory(${PROJECT_SOURCE_DIR}/../onnxruntime/third_party/kaldi kaldi_files) # 查找路径下的所有源文件 保存在src_files这个变量中去！
add_library(fawasr2pass-jni SHARED fawasr2pass.cc ${src_files}  ) 
# add_library生成静态或者动态库 参数解析（生成的库名字， 决定是动态还是静态（默认是静态的），参数指定生成库的源文件） 
target_link_libraries(fawasr2pass-jni PUBLIC avcodec avformat avutil avfilter swresample swscale  )
# avcodec avformat avutil avfilter swresample swscale
target_link_libraries(fawasr2pass-jni PUBLIC onnxruntime  -pthread gflags fst  kaldi-util kaldi-decoder yaml-cpp dl  csrc glog log)
# target_link_libraries(fawasroff-jni PUBLIC -pthread)
# target_link_libraries 添加头文件目录. -pthread