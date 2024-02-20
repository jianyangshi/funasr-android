package com.fawai.asr;

import static java.lang.System.arraycopy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainActivity extends AppCompatActivity {

  private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
  private static final String LOG_TAG = "FAWAI_ASR";
  private static final int SAMPLE_RATE = 16000;
  private static final int MAX_QUEUE_SIZE = 2500;
  private static final int ASR_MINI_BUFFER_SIZE = 9600;
  private static final int VAD_MINI_BUFFER_SIZE = 800;

  private static final List<String> resource = Arrays.asList(
    "asr_offline_res", "horword", "asr_online_res","punc_res","vad_res"
  );

  private boolean startRecord = false;
  private AudioRecord record = null;
  private final BlockingQueue<byte[]> audioBufferQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
  private final BlockingQueue<float[]> asrBufferQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

  private String  asrResPartial = "";
  private Boolean vadFrontEnd = false;
  private Boolean vadTailEnd = false;

  public static void assetsInit(Context context) throws IOException {
    // 模型资源文件转存 assets -> app/files  静态方法声明，参数为 Context 对象，抛出 IOException 异常。
    AssetManager assetMgr = context.getAssets();
    //获取应用的 AssetManager 对象，用于访问应用的 assets 目录。
    for (String file_dir : assetMgr.list("")) {
      //遍历 assets 目录下的所有文件和子目录。
      if (resource.contains(file_dir)) {
        // 检查当前目录是否在指定的资源列表 resource 中。
        for (String file : assetMgr.list(file_dir)) {
          //遍历当前目录下的所有文件。
          file = file_dir + "/" + file;
          //构建文件的相对路径。
          File dst = new File(context.getFilesDir(), file);
          //创建目标文件对象，指定目标目录为 app/files。
          if (!dst.exists() || dst.length() == 0) {
            //检查目标文件是否存在或为空。
            File dst_dir = new File(context.getFilesDir(), file_dir);
            //创建目标目录对象。
            if (!dst_dir.exists()) {
              //如果目标目录不存在，尝试创建目标目录，如果创建失败，记录错误日志。
              if (!dst_dir.mkdirs()) {
                Log.e(LOG_TAG, "make des dir failed");
              }
            }

            Log.i(LOG_TAG, "Unzipping " + file + " to " + dst.getAbsolutePath());
            //记录信息日志，表示正在解压文件到目标目录。
            InputStream is = assetMgr.open(file);
            //打开 assets 目录下文件的输入流。
            OutputStream os = new FileOutputStream(dst);
            //创建目标文件的输出流。创建缓冲区，大小为 4KB。
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
              os.write(buffer, 0, read );
            }
            os.flush();
          }
        }
      }
    }
  }

  @Override
  //注解，表示该方法覆盖了父类的同名方法
  public void onRequestPermissionsResult(int requestCode,
  //方法声明，用于处理权限请求结果。
      String[] permissions, int[] grantResults) {
        //检查权限请求的请求码是否为 MY_PERMISSIONS_RECORD_AUDIO
    if (requestCode == MY_PERMISSIONS_RECORD_AUDIO) {
      //检查授权结果数组的长度是否大于 0，且第一个权限是否被授予。
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //记录信息日志，表示录音权限已被授予。
        Log.i(LOG_TAG, "record permission is granted");
        //调用 initRecorder 方法，用于初始化录音器。
        initRecorder();
      } else {
        //显示一个长时间的 Toast 消息，提示用户录音权限已被拒绝。
        Toast.makeText(this, "Permissions denied to record audio", Toast.LENGTH_LONG).show();
        Button button = findViewById(R.id.button);
        button.setEnabled(false);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //方法声明，用于在 Activity 创建时执行初始化操作。
    super.onCreate(savedInstanceState);
    //调用父类的 onCreate 方法，确保父类的初始化操作得以执行。
    setContentView(R.layout.activity_main);
    //设置当前活动的布局，使用 activity_main.xml 文件定义的布局。
    requestAudioPermissions();
    //调用 requestAudioPermissions 方法，请求录音权限。
    try {
      //尝试调用 assetsInit 方法，用于将模型资源文件从 assets 目录拷贝到应用程序文件目录中。
      //如果发生异常（如 IO 异常），则记录错误日志。
      assetsInit(this);
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error process asset files to file path");
    }
    // 获取布局中的 EditText 对象，并将其文本内容设为空字符串
    EditText deviceResView = findViewById(R.id.deviceResView);
    deviceResView.setText("");
    // 端侧ASR引擎初始化
    OnnxInter.ASRInitOffline(getFilesDir().getPath());
    //调用 OnnxInter.ASRInitOnline 方法，用于在端侧初始化在线 ASR 引擎。
    //getFilesDir().getPath() 提供了应用程序文件目录的路径。
    Button button = findViewById(R.id.button);
    button.setText("Start Record");
    // 获取布局中的按钮对象，并将其文本内容设置为 "Start Record"。
    //为按钮设置点击事件监听器，当按钮被点击时执行的操作。
    //如果 startRecord 为 false，表示当前未开始录音，则设置 startRecord 为 true，清空部分 ASR 结果，
    //启动录音线程和 ASR 线程，将按钮文本设置为 "Stop Record"。如果 startRecord 为 true，表示当前正在录音，
    //则设置 startRecord 为 false，将按钮文本设置为 "Start Record"，并禁用按钮。
    button.setOnClickListener(view -> {
      if (!startRecord) {
        startRecord = true;
        asrResPartial = "";
        startRecordThread();
        startAsrThread();
        button.setText("Stop Record");
      } else {
        startRecord = false;
        button.setText("Start Record");
      }
      button.setEnabled(false);
    });
  }

  private void requestAudioPermissions() {
    //方法声明，用于请求录音权限。
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.RECORD_AUDIO},
          MY_PERMISSIONS_RECORD_AUDIO);
          //使用 ActivityCompat.requestPermissions 方法请求录音权限。
          //参数包括当前 Activity，权限数组（这里只包括录音权限），以及权限请求码 MY_PERMISSIONS_RECORD_AUDIO。
    } else {
      initRecorder();
      //初始化录音操作.
    }
  }

  private void initRecorder() {
    //初始化录音的具体操作
    record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            VAD_MINI_BUFFER_SIZE); // 最小数据长度
            //创建 AudioRecord 实例，指定音频源为 VOICE_RECOGNITION，采样率为 SAMPLE_RATE，
            //声道配置为单声道 (CHANNEL_IN_MONO)，
            //数据格式为 16 位 PCM 编码 (ENCODING_PCM_16BIT)，缓冲区大小为 VAD_MINI_BUFFER_SIZE。
    if (record.getState() != AudioRecord.STATE_INITIALIZED) {
      //使用 getState 方法检查 AudioRecord 实例的初始化状态。
      //如果初始化状态不是 STATE_INITIALIZED，表示初始化失败，则执行以下代码块。
      Log.e(LOG_TAG, "Audio Record can't initialize!");
      //在错误日志中记录初始化失败的消息。
      return;
    }
    Log.i(LOG_TAG, "Record init okay");
  }

  private void startRecordThread() {
    //方法声明，用于启动音频录制线程。
    new Thread(() -> {
      //创建一个新线程，并使用 Lambda 表达式定义线程的执行内容。
      record.startRecording();
      //调用 startRecording 方法开始录制音频。
      Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
      //设置线程优先级为音频优先级。
      while (startRecord) {
        //循环条件，只要 startRecord 为 true，就执行循环体内的操作
        byte[] buffer = new byte[VAD_MINI_BUFFER_SIZE * 2];
        //创建一个字节数组 buffer，其大小为 VAD_MINI_BUFFER_SIZE * 2。
        int read = record.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);
        //使用 AudioRecord 的 read 方法读取录制的音频数据，并将读取的数据存储到 buffer 中。
        //AudioRecord.READ_BLOCKING 表示以阻塞方式读取音频数据。
        try {
          if (AudioRecord.ERROR_INVALID_OPERATION != read) {
            audioBufferQueue.put(buffer);
          }
        } catch (InterruptedException e) {
          Log.e(LOG_TAG, e.getMessage());
        }
        //尝试将读取到的音频数据放入 audioBufferQueue 阻塞队列中。如果成功放入队列，表示数据成功读取。
        //如果队列操作被中断，捕获 InterruptedException 异常并记录错误日志。
        Button button = findViewById(R.id.button);
        if (!button.isEnabled() && startRecord) {
          runOnUiThread(() -> button.setEnabled(true));
        }
        //获取布局中的按钮控件，并检查按钮是否处于不可用状态，如果是，则在 UI 线程中将按钮设为可用状态。
      }//循环结束
      record.stop();//停止音频录制
    }).start();//线程启动
  }//方法结束

  private void startAsrThread() {
    new Thread(() -> {
      // 端侧ASR推理
//      int asrDataCount = 0;
//      float[] asrData = new float[ASR_MINI_BUFFER_SIZE];
      while (startRecord || audioBufferQueue.size() > 1) {
        try {
          byte[] data = audioBufferQueue.take();// 从队列中取出音频数据
          String asrResTmp = OnnxInter.ASRInferOffline(data, false);// 进行端侧ASR推理
          runOnUiThread(() -> {
              TextView deviceResView = findViewById(R.id.deviceResView);
              deviceResView.setText(asrResTmp);// 在UI线程更新UI组件上的ASR结果显示
          });
// 下面是一些被注释掉的逻辑，处理了VAD（语音活动检测）的前端、后端和ASR推理的逻辑，可以根据需要进行调整
//          if (!vadFrontEnd) {
//            vadFrontEnd = (Objects.equals(vadResTmp, "frontEnd"));
//          }
//          if (!vadTailEnd) {
//            vadTailEnd = (Objects.equals(vadResTmp, "tailEnd"));
//          }
//
//          if (vadFrontEnd && asrDataCount < 12) {
//            arraycopy(data, 0, asrData, asrDataCount * VAD_MINI_BUFFER_SIZE, VAD_MINI_BUFFER_SIZE);
//            asrDataCount += 1;
//          } else if (vadFrontEnd) {
//            Log.i(LOG_TAG, vadResTmp);
//            String asrResTmp = OnnxInter.ASRInferOnline(asrData, false);
//            asrResPartial += asrResTmp;
//            Log.i(LOG_TAG, asrResPartial);
//
//            runOnUiThread(() -> {
//              TextView deviceResView = findViewById(R.id.deviceResView);
//              deviceResView.setText(asrResPartial);
//            });
//
//            asrDataCount = 0;
//            asrData = new float[ASR_MINI_BUFFER_SIZE];
//          }
//          if (vadTailEnd) {
//            String asrResTmp = OnnxInter.ASRInferOnline(asrData, true);
//            asrResPartial += asrResTmp;
//            Log.i(LOG_TAG, asrResPartial);
//
//            runOnUiThread(() -> {
//              TextView deviceResView = findViewById(R.id.deviceResView);
//              deviceResView.setText(asrResPartial);
//            });
//
//            asrDataCount = 0;
//            asrData = new float[ASR_MINI_BUFFER_SIZE];
//
//            vadTailEnd = false;
//            vadFrontEnd = false;
//          }

        } catch (InterruptedException e) {
          Log.e(LOG_TAG, e.getMessage());
        }
      }

//      while (audioBufferQueue.size() > 0) {
//        try {
//          float[] data = audioBufferQueue.take();
//          Log.i(LOG_TAG, String.valueOf(data.length));
//          String asrResTmp = OnnxInter.ASRInferOnline(data, true);
//          asrResPartial += asrResTmp;
//          Log.i(LOG_TAG, asrResPartial);
//          runOnUiThread(() -> {
//            TextView deviceResView = findViewById(R.id.deviceResView);
//            deviceResView.setText(asrResPartial);
//          });
//        } catch (InterruptedException e) {
//          throw new RuntimeException(e);
//        }
//      }
      runOnUiThread(() -> {
        Button button = findViewById(R.id.button);
        button.setEnabled(true);
      });
    }).start();
  }
}