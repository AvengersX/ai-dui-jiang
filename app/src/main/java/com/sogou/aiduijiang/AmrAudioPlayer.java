package com.sogou.aiduijiang;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sogou.aiduijiang.im.IMCallBack;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by caohe on 15-5-29.
 */
public class AmrAudioPlayer {

    private static AmrAudioPlayer mPlayer = new AmrAudioPlayer();

    private Handler mHandler;

    private IMCallBack mCallBack;

    public AmrAudioPlayer() {
        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    IMCallBack callBack = (IMCallBack)msg.obj;
                    Bundle data = msg.getData();
                    String uid = data.getString("uid");
                    if (callBack != null) {
                        callBack.onUserEndTalk(uid);
                    }
                }
            }
        };
        cacheFile = new File("/sdcard/cache.amr");
    }

    public static AmrAudioPlayer getInstance() {
        return mPlayer;
    }

    private boolean isPlaying = false;

    private File cacheFile;

    private boolean isFirstBlock = true;

    public boolean isPlaying() {
        return isPlaying;
    }

    private int mCurrent = 0;

    public void play(String uid, IMCallBack callBack) {
        Log.v("hccc", "===play==");
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        if (callBack != null) {
            callBack.onUserStartTalk(uid);
        }
        mHandler.removeMessages(100);
        playFrom(0, uid, callBack);
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                while(isPlaying) {
//                    playFrom(mCurrent);
//                    try {
//                        wait(1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    mCurrent += 1000;
//                }
//            }
//        }).start();
    }

    private MediaPlayer mCurrentP;

    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    private void playFrom(final int start, final String uid, final IMCallBack callBack) {
        final MediaPlayer mMediaPlayer = new MediaPlayer();
        mCurrentP = mMediaPlayer;
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    FileInputStream fis = new FileInputStream(cacheFile);
                    mMediaPlayer.setDataSource(fis.getFD());
                    mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Log.v("hccc", "=====on error=" + what + " " + extra);
                            return true;
                        }
                    });
                    mMediaPlayer.prepare();
                    Log.v("hccc", "====seek==" + start + " " + mMediaPlayer.getDuration());
//                    mMediaPlayer.reset();
                    mMediaPlayer.seekTo(start);
                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.v("hccc", "======on compeltion=" + mp.getCurrentPosition() + " " + start);
                            if (mp.getCurrentPosition() != start) {
                                playFrom(mp.getCurrentPosition(), uid, callBack);
//                                mp.release();
                            } else {
                                Log.v("hccc", "======on compeltion play finish=");
                                synchronized (mPlayer) {

                                    if (callBack != null) {
                                        Message msg = Message.obtain();
                                        msg.what = 100;
                                        msg.obj = callBack;
                                        Bundle data = new Bundle();
                                        data.putString("uid", uid);
                                        msg.setData(data);
                                        mHandler.sendMessageDelayed(msg, 1000);
//                                        callBack.onUserEndTalk(uid);
                                    }
                                    isPlaying = false;
                                    cacheFile.delete();
                                }
                            }
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            Log.v("hccc", "====on prepared =");
                        }
                    });
                    mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mp) {
                            Log.v("hccc", "===on seek complete==");
                        }
                    });

//                    mMediaPlayer.stop();
                } catch (Exception e) {

                    synchronized (mPlayer) {
                        isPlaying = false;
                        cacheFile.delete();
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stop() {
//        cacheFile.delete();
//        isPlaying = false;
//        mCurrent = 0;
    }

    public void addData(Uri uri, String uid, IMCallBack callBack) {
        synchronized (mPlayer) {
            try {
                FileOutputStream fileOS = new FileOutputStream(cacheFile, true);

                FileInputStream is = new FileInputStream(uri.getPath());
                DataInputStream dis = new DataInputStream(is);

                skipAmrHead(dis);
                if (!cacheFile.exists() || cacheFile.length() == 0) {
                    Log.v("hccc", "====write first block==");
                    final byte[] AMR_HEAD = new byte[] { 0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
                    fileOS.write(AMR_HEAD);
//                isFirstBlock = false;
                } else {
                    play(uid, callBack);
                }

                byte[] sendBuffer = new byte[1024];
                int a = dis.available();
                while (a > 0) {
                    Log.v("hccc", "======" + a);
                    dis.read(sendBuffer, 0, a > 1024 ? 1024 : a);
                    byte[] writeBuffer = new byte[a > 1024 ? 1024 : a];
                    System.arraycopy(sendBuffer, 0, writeBuffer, 0, a > 1024 ? 1024 : a);
                    fileOS.write(writeBuffer);
                    a = dis.available();
                }
                fileOS.flush();
                fileOS.close();
                is.close();
                dis.close();

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void skipAmrHead(DataInputStream dataInput) {
        final byte[] AMR_HEAD = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};
        int result = -1;
        int state = 0;
        try {
            while (-1 != (result = dataInput.readByte())) {
                if (AMR_HEAD[0] == result) {
                    state = (0 == state) ? 1 : 0;
                } else if (AMR_HEAD[1] == result) {
                    state = (1 == state) ? 2 : 0;
                } else if (AMR_HEAD[2] == result) {
                    state = (2 == state) ? 3 : 0;
                } else if (AMR_HEAD[3] == result) {
                    state = (3 == state) ? 4 : 0;
                } else if (AMR_HEAD[4] == result) {
                    state = (4 == state) ? 5 : 0;
                } else if (AMR_HEAD[5] == result) {
                    state = (5 == state) ? 6 : 0;
                }

                if (6 == state) {
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

}
