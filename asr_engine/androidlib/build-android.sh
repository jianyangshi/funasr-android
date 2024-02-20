dir=$PWD/build

mkdir -p $dir
cd $dir
    # -DFFMPEG_DIR=/home/sjy/ffmpeg-master-latest-armv8-gpl-shared \ 
cmake -DCMAKE_BUILD_TYPE=release \
    -DBUILD_SHARED_LIBS=ON  \
    -DCMAKE_TOOLCHAIN_FILE="/home/sjy/Android/Sdk/ndk/25.1.8937393/build/cmake/android.toolchain.cmake" \
    -DANDROID_ABI="arm64-v8a" \
    -DANDROID_PLATFORM="android-26" \
    -DCMAKE_INSTALL_PREFIX=./install ..

make -j4
