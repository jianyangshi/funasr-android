package com.fawai.asr;

public class OnnxInter {
    static {
        System.loadLibrary("fawasr2pass-jni");
    }

    public static native void ASRInitOffline(String modelDir);
    public static native String ASRInferOffline(byte[] waveform, boolean input_finished);
//    public static native void VADInitOnline(String modelDir);
//    public static native String VADInferOnline(float[] waveform, boolean input_finished);
}
