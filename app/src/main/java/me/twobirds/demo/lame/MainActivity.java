package me.twobirds.demo.lame;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView tv_file_path;
    private Button btn_transform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_file_path = (TextView) findViewById(R.id.tv_file_path);
        btn_transform = (Button) findViewById(R.id.btn_transform);

        initFFmpeg();

        requestPermissions();

        tv_file_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 1);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_transform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedFilePath = selectedFile.getAbsolutePath();
                String convertedFilePath = selectedFilePath.substring(0, selectedFilePath.lastIndexOf('.')) + "_" + System.currentTimeMillis() + ".mp3";
                FFmpeg ffmpeg = FFmpeg.getInstance(MainActivity.this);
                try {
                    // to execute "ffmpeg -version" command you just need to pass "-version"
                    ffmpeg.execute(new String[]{"-y", "-i", selectedFilePath, convertedFilePath}, new ExecuteBinaryResponseHandler() {

                        @Override
                        public void onStart() {
                            Log.i("cth", "onStart");
                        }

                        @Override
                        public void onProgress(String message) {
                            Log.i("cth", "onProgress : message = " + message);
                        }

                        @Override
                        public void onFailure(String message) {
                            Log.i("cth", "onFailure : message = " + message);
                        }

                        @Override
                        public void onSuccess(String message) {
                            Log.i("cth", "onSuccess : message = " + message);
                        }

                        @Override
                        public void onFinish() {
                            Log.i("cth", "onSuccess");
                        }
                    });
                } catch (FFmpegCommandAlreadyRunningException e) {
                    // Handle if FFmpeg is already running
                }
            }
        });
    }

    private void initFFmpeg() {
//        AndroidAudioConverter.load(this, new ILoadCallback() {
//            @Override
//            public void onSuccess() {
//                // Great!
//                Log.d("cth","加载成功");
//            }
//            @Override
//            public void onFailure(Exception error) {
//                // FFmpeg is not supported by device
//            }
//        });

        FFmpeg ffmpeg = FFmpeg.getInstance(this);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.i("cth", "开始加载");
                }

                @Override
                public void onFailure() {
                    Log.i("cth", "加载失败");
                }

                @Override
                public void onSuccess() {
                    Log.i("cth", "加载成功");
                }

                @Override
                public void onFinish() {
                    Log.i("cth", "加载工作结束");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    private File selectedFile;

    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率

    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get the Uri of the selected file
            Uri uri = data.getData();
            Log.i("cth", "uri = " + uri);
            String filePath = UriToPathUtil.getRealFilePath(this, uri);
            Log.i("cth", "filePath is " + filePath);

            if (!TextUtils.isEmpty(filePath)) {
                selectedFile = new File(filePath);
                if (selectedFile.exists()) {

                    tv_file_path.setText(filePath);

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            try {
//                                FileInputStream fileInputStream = new FileInputStream(selectedFile);
//                                File parentFile = selectedFile.getParentFile();
//                                int lastDotIndex = selectedFile.getName().lastIndexOf('.');
//                                String mp3FileName = selectedFile.getName().substring(0, lastDotIndex) + "_" + System.currentTimeMillis() + ".mp3";
//                                File mp3File = new File(parentFile, mp3FileName);
//                                Log.i("cth", "mp3File = " + mp3File);
//                                FileOutputStream fileOutputStream = new FileOutputStream(mp3File);
//
//                                byte[] buffer = new byte[1024];
//                                LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
//
//                                int len = 0;
//                                byte[] mp3buf = new byte[1024 * 10];
//                                while (len >= 0) {
//                                    len = fileInputStream.read(buffer);
//                                    Log.i("cth", "len = " + len);
//                                    if (len > 0) {
//                                        short[] tmp = toShortArray(buffer);
//                                        int encodeLen = LameUtil.encode(tmp, tmp, len * 2, mp3buf);
//                                        Log.i("cth", "encodeLen = " + encodeLen);
//                                        if (encodeLen > 0) {
//                                            fileOutputStream.write(mp3buf, 0, encodeLen);
//                                        }
//                                    }
//                                }
//
//                                fileInputStream.close();
//                                fileInputStream = null;
//
//                                fileOutputStream.flush();
//                                fileOutputStream.close();
//                                fileOutputStream = null;
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();


                }
            }


        }


    }


    public static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public static byte[] toByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }
}
