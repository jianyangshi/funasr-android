#include "jni.h" 
#include "precomp.h"
#ifdef __cplusplus 

namespace funasroff {
    FUNASR_HANDLE asr_handle = nullptr;
    FUNASR_DEC_HANDLE decoder_handle = nullptr;
    FUNASR_RESULT result = nullptr;
    FUNASR_HANDLE tpass_handle = nullptr;
    FUNASR_HANDLE tpass_online_handle = nullptr;
    // std::string vad_sub_dir = "/vad_res";
    // std::string punc_sub_dir = "/punc_res";
    std::string asr_model_dir = "/asr_offline_model_res";
   
    // std::string lm_dir = "/lm";
    std::string hotword = "/horword";
    std::string wav_file = "/wav_file";
    std::vector<std::vector<float>> hotwords_embedding;
    std::string offline_res = "";
    std::vector<std::vector<std::string>> punc_cache;


    int thread_num = 1;

    extern "C"
    JNIEXPORT void JNICALL Java_com_fawai_asr_OnnxInter_ASRInitOffline(
        JNIEnv *env, jobject /*obj*/, jstring jModelDir)
    {
        const char* pModelDir = env->GetStringUTFChars(jModelDir, nullptr);
        std::cout << "pModelDir" << pModelDir << std::endl;
        std::map<std::string, std::string> model_path;
        // model_path.insert({VAD_DIR, std::string(pModelDir) + vad_sub_dir});
        // model_path.insert({VAD_QUANT, "true"});

        model_path.insert({MODEL_DIR, std::string(pModelDir) + asr_model_dir});
        model_path.insert({QUANTIZE, "true"});

        // model_path.insert({LM_DIR, std::string(pModelDir) + lm_dir});
        // model_path.insert({HOTWORD, std::string(pModelDir) + hotword});

        // model_path.insert({PUNC_DIR, std::string(pModelDir) + punc_sub_dir});
        // model_path.insert({PUNC_QUANT, "true"});

        try {
            asr_handle = FunOfflineInit(model_path, thread_num);
            if (!asr_handle) {
                LOG(ERROR) << "FunOfflineInit init failed";
            }
            decoder_handle = FunASRWfstDecoderInit(asr_handle, 0, 3.0, 3.0, 10.0);
            if (!asr_handle) {
                LOG(ERROR) << "FunASRWfstDecoderInit init failed";
            }
        } catch (const std::exception& e) {
            LOG(INFO) << e.what();
        }
    }


    extern "C"
    JNIEXPORT jstring JNICALL Java_com_fawai_asr_OnnxInter_ASRInferOffline(
        JNIEnv *env, jobject /*obj*/, jbyteArray jWaveform, jboolean input_finished)
    {
        int size = env->GetArrayLength(jWaveform);
        jbyte* waveform = env->GetByteArrayElements(jWaveform, 0);
        
        std::vector<char> subvector(waveform, waveform + size);
        
        try{
            result = FunOfflineInferBuffer(asr_handle, subvector.data(), subvector.size(), RASR_NONE, NULL, 
            hotwords_embedding,  16000, "pcm", true, decoder_handle);

            // result = FunTpassInferBuffer(tpass_handle, tpass_online_handle,
            //                             subvector.data(), subvector.size(), punc_cache,
            //                             input_finished==JNI_TRUE, 16000, "pcm", (ASR_TYPE)2);

            if (result) {
                std::string msg = FunASRGetResult(result, 0);
                offline_res += msg;

                if (msg != "") {
                    LOG(INFO) << "offline results :" << msg;
                }
                //  asr_handle,subvector.data(), subvector.size(), 
                //                                -1, null    , hotwords_embedding,
				// 							 16000,  "pcm", true, dec_handle
                // std::string  tmp_sent_msg = FunASRGetStampSents(result, 0);
                // tmp_sent += tmp_sent_msg;
                // if (tmp_sent_msg != "") {
                //     LOG(INFO) << "tmp_sent results : " << tmp_sent_msg;
                //     offline_res = tmp_sent;
                // }

                FunASRFreeResult(result);
            }
        } catch (std::exception const &e)
        {
            LOG(ERROR) << e.what();
        } 

        return env->NewStringUTF(offline_res.c_str());

    }

}

#endif