1、部署的offline的模式。

2、/home/sjy/fawasr-sjy/asr_android_app/app/src/main 文件夹下的是 
安卓APP的基本配置文件：assets是模型文件，asr_offline_res 非流式模型（一句话说完显示结果） asr_online_are 流式模型（一直在显示说的内容） hotword 热词模型 punch_res 标点模型 vad_res 端点断句？
 				
		      JAVA文件夹下的是基础的简单的调用JAVA文件创建安卓app。

		      jniLibs/arm64-v8a 放置的是安卓的ARM64版本的.so文件供上述的JAVA文件夹调用！

		      

3、在JAVA文件夹下的/java/com/fawai/asr/MainActivity.java 是主要文件，OnnxInter.java就是调用.so中的相关参数函数。

4、需要自己去构建安卓下的ffmpeg的.so文件，我当时试过linux下的.so，但是尝试失败！下载github上的ffmpeg-master，构建build-android（armv8）和（armv7）.sh，执行文件创建Android文件夹生成ffmpeg的.so文件供安卓app调用！ONNX的.so我是直接找的网上的，可以直接调用。

5、model文件夹的地址是/home/sjy/苏州亿城科技/2024/2024.2.5/fawasr-sjy/asr_android_app/app/src/main/assets下的
