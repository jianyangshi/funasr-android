
download_model_dir="/home/sunyujia/.cache/modelscope/hub"
model_dir="damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-onnx"
online_model_dir="damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx"
vad_dir="damo/speech_fsmn_vad_zh-cn-16k-common-onnx"
punc_dir="damo/punc_ct-transformer_zh-cn-common-vocab272727-onnx"
decoder_thread_num=32
io_thread_num=8
listen_ip="10.78.2.83"
port=10096

. ../../egs/aishell/transformer/utils/parse_options.sh || exit 1;

cd /home/sunyujia/python_ws/FunASR/funasr/runtime/websocket/build/bin
./funasr-wss-server-2pass  \
  --download-model-dir ${download_model_dir} \
  --model-dir ${model_dir} \
  --online-model-dir ${online_model_dir} \
  --vad-dir ${vad_dir} \
  --punc-dir ${punc_dir} \
  --punc-revision "v1.1.7" \
  --decoder-thread-num ${decoder_thread_num} \
  --io-thread-num ${io_thread_num} \
  --listen-ip ${listen_ip} \
  --port ${port} \
  --certfile "" \
  --keyfile "" \

