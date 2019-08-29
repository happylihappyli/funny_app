package com.funnyai.android;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

@SuppressLint("OverrideAbstract")
public class ListenerService extends NotificationListenerService {

    //Properties prop = null;

    private BufferedWriter bw;
    private SimpleDateFormat sdf;


    private MsgBinder mMsgBinder=new MsgBinder();


    private MyHandler handler = new MyHandler();
    private String nMessage;


    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String msgString = (String) msg.obj;
            Toast.makeText(getApplicationContext(), msgString, Toast.LENGTH_LONG).show();
        }
    };


    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public ListenerService getService(){
            return ListenerService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("=====onBind=====");
        return mMsgBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        prop = new Properties();
//
//        try {
//            String config_file = "config.properties";
//            InputStream in = getApplicationContext().getAssets().open(config_file);  //打开assets目录下的config.properties文件
//            prop.load(new InputStreamReader(in, "utf-8"));
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
    }

//    //读取
//    public String getValue(String key){
//        String value  = prop.getProperty(key);
//        return value;
//    }
//
//    //修改
//    public String setValue(String key,String value) {
//        try {
//            prop.setProperty (key,value);
//            File file = new File("file:///android_asset/config.properties");
//            OutputStream fos = new FileOutputStream(file);
//            prop.store(fos, "");
//            fos.flush();
//            return value;
//        } catch (Exception e1) {
//            return null;
//        } finally {
//
//        }
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("KEVIN", "Service is started" + "-----");
        //data = intent.getStringExtra("data");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //        super.onNotificationPosted(sbn);
        try {
            //有些通知不能解析出TEXT内容，这里做个信息能判断
            if (sbn.getNotification().tickerText != null) {
                nMessage = sbn.getNotification().tickerText.toString();

                Log.i("message",nMessage);

                String sound_file = "";
                for (int i=1;i<=10;i++){
                    String key=BackGroundService.getValue("key"+i);
                    Log.i("key",key);
                    if (nMessage.contains(key)) {
                        sound_file = BackGroundService.getValue("sound"+i);
                    }
                }

                if (!"".equals(sound_file)){
                    if (sound_file.startsWith("file:///")){
                        MyPlayer.playOrPause(sound_file);
                    }else {
                        try {
                            AssetFileDescriptor fd = getAssets().openFd(sound_file);
                            MediaPlayer mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (Exception e) {
            Toast.makeText(ListenerService.this, "不可解析的通知", Toast.LENGTH_SHORT).show();
        }

    }




    private File newFile() {
        File fileDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ANotification");
        fileDir.mkdir();
        String basePath = Environment.getExternalStorageDirectory() + File.separator + "ANotification" + File.separator + "record.txt";
        return new File(basePath);

    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
//                    Toast.makeText(MyService.this,"Bingo",Toast.LENGTH_SHORT).show();
            }
        }

    }

}
