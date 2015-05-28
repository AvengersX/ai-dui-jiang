package com.sogou.aiduijiang;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by caohe on 15-5-28.
 */
public class RecordUtil {

    private static MediaRecorder mMediaRecorder01;
    private static File mRecordFile;

    public static File startRecord() {
        File  myRecAudioFile = null;
        try {
            Log.v("hccc", "=====" + Environment.getExternalStorageDirectory() + " " + myRecAudioFile);
            File cacheDir = new File(Environment.getExternalStorageDirectory() + "/adj_temp");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            myRecAudioFile = File.createTempFile("Audio" + System.currentTimeMillis(), ".amr",
                    cacheDir);
            Log.v("hccc", "=====" + cacheDir + " " + myRecAudioFile);
            mMediaRecorder01 = new MediaRecorder();
          /* 设定录音来源为麦克风 */
            mMediaRecorder01
                    .setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder01
                    .setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder01
                    .setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            mMediaRecorder01.setOutputFile(myRecAudioFile
                    .getAbsolutePath());

            mMediaRecorder01.prepare();
            mMediaRecorder01.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecordFile = myRecAudioFile;
        return myRecAudioFile;
    }

    public static File endRecord() {

        try {
            if (mMediaRecorder01 != null) {
                mMediaRecorder01.stop();
                mMediaRecorder01.release();
                mMediaRecorder01 = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mRecordFile;
    }

}
