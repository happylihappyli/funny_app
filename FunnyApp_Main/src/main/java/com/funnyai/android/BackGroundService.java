package com.funnyai.android;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.funnyai.android.R;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;
import static java.net.Proxy.Type.HTTP;

public class BackGroundService extends Service {

    public static TreeMap pMap=new TreeMap();

    ////////静态变量 public
    public static Context context=null;

    ///////静态变量 private
    private static Properties prop = null;
    private static TextToSpeech tts;


    ////////////非静态变量 public
    public String bAlert = "0";//="1"就提醒
    public Handler mHandler = null;

    public ACT_Main pMain=null;

    public String userName;

    ///////////非静态变量 private
    private MsgBinder mMsgBinder=new MsgBinder();
    private Notification notification;
    private Context mContext;
//    private Runnable reconnectCallback=this::start;;


    public static void read_init(){

        prop = new Properties();

        try {
            //String config_file = "config.properties";
            File file = new File(context.getFilesDir(), "userinfo.txt");//"
            InputStream in =  new FileInputStream(file);//context.getAssets().open(context.getFilesDir()+"/userinfo.txt");  //打开assets目录下的config.properties文件
            prop.load(new InputStreamReader(in, "utf-8"));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }


    public static String http_post(String strURL,String data){
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");

            //数据准备
            //至少要设置的两个请求头
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", data.length()+"");

            //post的方式提交实际上是留的方式提交给服务器
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());

            //获得结果码
            int responseCode = connection.getResponseCode();
            if(responseCode ==200){
                //请求成功
                InputStream inputStream = connection.getInputStream();

                BufferedReader r = new BufferedReader(
                        new InputStreamReader(inputStream,"utf-8"));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }
                return total.toString();
            }else {
                //请求失败
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String sys_get_token(){
        String url="http://www.funnyai.com/login_get_token_json.php";

        String name=BackGroundService.getValue("sys.user_name");
        String md5=BackGroundService.getValue("sys.user_password");
        if (name==null || md5==null) return "";

        String data= null;
        try {
            data = "email="+ URLEncoder.encode(name, "UTF-8")
                    +"&password="+URLEncoder.encode(md5, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result=http_post(url,data);
        String token="";
        if (result.indexOf("登录成功")>-1){
            String[] strSplit=result.split("=");
            token=strSplit[2];
        }
        return token;
    }

    //读取
    public static String getValue(String key){
        String value  = prop.getProperty(key);
        return value;
    }

    public static SocketConnectThread pClient;
    public void tcp_connect(String IP,int iPort){
        pClient=new SocketConnectThread(this,this.pMain,IP,iPort);
        pClient.start();
    }
    //修改
    public static String setValue(String key,String value) {
        try {
            prop.setProperty (key,value);
            File file = new File(context.getFilesDir(), "userinfo.txt");//"file:///android_asset/config.properties");
            OutputStream fos = new FileOutputStream(file);
            prop.store(fos, "");
            fos.flush();
            return value;
        } catch (Exception e1) {
            return null;
        } finally {

        }
    }

    public void open_url(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }


    public static String time_now(){
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        String strTime= sdf.format(date); // 输出已经格式化的现在时间（24小时制）
        return strTime;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        BackGroundService.context=getApplicationContext();
        BackGroundService.read_init();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == tts.SUCCESS) {
                    int result = tts.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE){
                        Toast.makeText(BackGroundService.context,
                                "TTS暂时不支持这种语音的朗读！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "notification_forground";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "forground",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("111")
                    .setContentText("222").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mContext = this;
        Log.d(TAG, "onStartCommand()");

        Log.i("KEVIN", "Service is started" + "-----");
        //data = intent.getStringExtra("data");
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("=====onBind=====");
        return mMsgBinder;
    }


    public static void tts_speak(String strLine){
        BackGroundService.tts.speak(strLine, TextToSpeech.QUEUE_FLUSH,null,null);
    }
    //*
    public static int Msg_ID=10;

    public void show_url_notification(
            int Msg_ID2,
            String channel_type,
            String title, String content) {

        if (Msg_ID2==0) Msg_ID2=Msg_ID;
        String channel_Id = "notification_simple";
        String channel_Name = "notification_simple";
        switch(channel_type){
            case "s":
                channel_Id = "notification_simple";
                channel_Name= "simple";
                break;
            case "n":
                channel_Id = "notification_no_sound";
                channel_Name= "no_sound";
                break;
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            Log.i("aaa","aaa");
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channel_Id, channel_Name, NotificationManager.IMPORTANCE_HIGH);// .IMPORTANCE_DEFAULT);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{
                    100, 200, 300
            });
            manager.createNotificationChannel(channel);


            Uri uri = Uri.parse(content);//"http://www.baidu.com");
            Intent mainIntent = new Intent();
            mainIntent.setAction("android.intent.action.VIEW");
            mainIntent.setData(uri);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, channel_Id)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(mainPendingIntent)
                    .build();


            manager.notify(Msg_ID2, notification);
        }
        else{
            Log.i("bbb","bbb");
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(this, channel_Id)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            manager.notify(Msg_ID2, notification);
            Toast.makeText(this,"lest 26",Toast.LENGTH_LONG).show();
        }

        Log.i("test", "showNotification: "+content);

        Msg_ID+=1;
    }


    // 默认显示的的Notification
    public void showNotification(
            int Msg_ID2,
            String channel_type,
            String title, String content) {
 
        if (Msg_ID2==0) Msg_ID2=Msg_ID;
        String channel_Id = "notification_simple";
        String channel_Name = "notification_simple";
        switch(channel_type){
            case "s":
                channel_Id = "notification_simple";
                channel_Name= "simple";
                break;
            case "n":
                channel_Id = "notification_no_sound";
                channel_Name= "no_sound";
                break;
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            Log.i("aaa","aaa");
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channel_Id, channel_Name, NotificationManager.IMPORTANCE_HIGH);// .IMPORTANCE_DEFAULT);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{
                    100, 200, 300
            });
            manager.createNotificationChannel(channel);


            Intent mainIntent = new Intent(this, ACT_Main.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, channel_Id)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(mainPendingIntent)
                    .build();


            manager.notify(Msg_ID2, notification);
        }
        else{
            Log.i("bbb","bbb");
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(this, channel_Id)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
                    //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            manager.notify(Msg_ID2, notification);
            Toast.makeText(this,"lest 26",Toast.LENGTH_LONG).show();
        }

        Log.i("test", "showNotification: "+content);

        Msg_ID+=1;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();

        if (tts != null)
            tts.shutdown();

    }



    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public BackGroundService getService(){
            return BackGroundService.this;
        }
    }



    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        }

    }
}



