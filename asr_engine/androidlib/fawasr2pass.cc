#include "jni.h" 
#include "precomp.h"
#ifdef __cplusplus 

namespace funasroff {
    FUNASR_HANDLE tpass_handle = nullptr;
    FUNASR_HANDLE tpass_online_handle = nullptr;
    // FUNASR_RESULT FunTpassInferBuffer = nullptr;
   

    FUNASR_RESULT result = nullptr;
    FUNASR_DEC_HANDLE decoder_handle = nullptr;

    std::string vad_sub_dir = "/vad_res";
    std::string asr_offline_sub_dir = "/asr_offline_res";
    std::string asr_online_sub_dir = "/asr_online_res";
    std::string punc_sub_dir = "/punc_res";
    std::string online_res = "";
    std::string tpass_res = "";
    std::string nn_hotwords_ = "";
    std::string hotword = "/hotword";

    std::vector<std::vector<float>> hotwords_embedding = CompileHotwordEmbedding(tpass_handle, nn_hotwords_, ASR_TWO_PASS);
    std::string offline_res = "";
    std::vector<std::vector<std::string>> punc_cache(2);


    int thread_num = 1;

    extern "C"
    JNIEXPORT void JNICALL Java_com_fawai_asr_OnnxInter_ASRInitOffline(
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
            // std::vector<int> chunk_size_vec = {5, 10, 5};
            std::vector<int> chunk_size = {5,10,5};
            float glob_beam = 3.0f;
            float lat_beam = 3.0f;
            float am_sc = 10.0f;
            tpass_online_handle = FunTpassOnlineInit(tpass_handle, chunk_size);
            decoder_handle = FunASRWfstDecoderInit(tpass_handle, ASR_TWO_PASS, glob_beam, lat_beam, am_sc);
            if (!tpass_handle) {
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
            // result = FunOfflineInferBuffer(asr_handle, subvector.data(), subvector.size(), RASR_NONE, NULL, 
            // hotwords_embedding,  16000, "pcm", true, decoder_handle);

            result = FunTpassInferBuffer(tpass_handle, tpass_online_handle,
                                        subvector.data(), subvector.size(), punc_cache,
                                        input_finished==JNI_TRUE, 16000, "pcm", (ASR_TYPE)2, hotwords_embedding, true, decoder_handle
                                        );

            if (result) {
                // std::string msg = FunASRGetResult(result, 0);
                // offline_res += msg;

                // if (msg != "") {
                //     LOG(INFO) << "offline results :" << msg;
                // }
                //  asr_handle,subvector.data(), subvector.size(), 
                //                                -1, null    , hotwords_embedding,
				// 							 16000,  "pcm", true, dec_handle
                // std::string  tmp_sent_msg = FunASRGetStampSents(result, 0);
                // tmp_sent += tmp_sent_msg;
                // if (tmp_sent_msg != "") {
                //     LOG(INFO) << "tmp_sent results : " << tmp_sent_msg;
                //     offline_res = tmp_sent;
                // }
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