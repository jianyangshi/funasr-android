#include "jni.h" 
#include "precomp.h"
#ifdef __cplusplus 

namespace fawasr {
    FUNASR_HANDLE tpass_handle = nullptr;
    FUNASR_HANDLE tpass_online_handle = nullptr;

    FUNASR_RESULT result = nullptr;

    std::string vad_sub_dir = "/vad_res";
    std::string asr_offline_sub_dir = "/asr_offline_res";
    std::string asr_online_sub_dir = "/asr_online_res";
    std::string punc_sub_dir = "/punc_res";
    std::string online_res = "";
    std::string tpass_res = "";

    std::vector<std::vector<std::string>> punc_cache;

    int thread_num = 1;

    extern "C"
    JNIEXPORT void JNICALL Java_com_fawai_asr_OnnxInter_ASRInitOnline(
        JNIEnv *env, jobject /*obj*/, jstring jModelDir)
    {
        const char* pModelDir = env->GetStringUTFChars(jModelDir, nullptr);
        std::cout << "pModelDir" << pModelDir << std::endl;
        std::map<std::string, std::string> model_path;
        model_path.insert({VAD_DIR, std::string(pModelDir) + vad_sub_dir});
        model_path.insert({VAD_QUANT, "true"});

        model_path.insert({OFFLINE_MODEL_DIR, std::string(pModelDir) + asr_offline_sub_dir});
        model_path.insert({ONLINE_MODEL_DIR, std::string(pModelDir) + asr_online_sub_dir});
        model_path.insert({QUANTIZE, "true"});

        model_path.insert({PUNC_DIR, std::string(pModelDir) + punc_sub_dir});
        model_path.insert({PUNC_QUANT, "true"});

        try {
            tpass_handle = FunTpassInit(model_path, thread_num);
            if (!tpass_handle) {
                LOG(ERROR) << "FunTpassInit init failed";
            }
            std::vector<int> chunk_size_vec = {5, 10, 5};
            tpass_online_handle = FunTpassOnlineInit(tpass_handle, chunk_size_vec);

        } catch (const std::exception& e) {
            LOG(INFO) << e.what();
        }
    }

    extern "C"
    JNIEXPORT jstring JNICALL Java_com_fawai_asr_OnnxInter_ASRInferOnline(
        JNIEnv *env, jobject /*obj*/, jbyteArray jWaveform, jboolean input_finished)
    {
        int size = env->GetArrayLength(jWaveform);
        jbyte* waveform = env->GetByteArrayElements(jWaveform, 0);

        std::vector<char> subvector(waveform, waveform + size);

        try{
            result = FunTpassInferBuffer(tpass_handle, tpass_online_handle,
                                        subvector.data(), subvector.size(), punc_cache,
                                        input_finished==JNI_TRUE, 16000, "pcm", (ASR_TYPE)2);

            if (result) {
                std::string tmp_online_msg = FunASRGetResult(result, 0);
                online_res += tmp_online_msg;

                if (tmp_online_msg != "") {
                    LOG(INFO) << "online_res :" << tmp_online_msg;
                }
                std::string tmp_tpass_msg = FunASRGetTpassResult(result, 0);
                tpass_res += tmp_tpass_msg;
                if (tmp_tpass_msg != "") {
                    LOG(INFO) << "offline results : " << tmp_tpass_msg;
                    online_res = tpass_res;
                }

                FunASRFreeResult(result);
            }
        } catch (std::exception const &e)
        {
            LOG(ERROR) << e.what();
        } 

        return env->NewStringUTF(online_res.c_str());
    }
}


#endif