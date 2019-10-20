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

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;

public class BackGroundService extends Service {

    ////////静态变量 public
    public static Context context=null;
    //public static Socket socket;

    ///////静态变量 private
    private static Properties prop = null;
    //private static String url="http://robot6.funnyai.com:8000";

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


//    public synchronized Socket start() {
//
//        Log.i ("socket","WebSocket: starting...");
//        if (socket.connected()==false) {
//            socket.connect();
//        }
//        return socket;
//    }

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
    //读取
    public static String getValue(String key){
        String value  = prop.getProperty(key);
        return value;
    }

    SocketConnectThread pClient;
    public void tcp_connect(String IP,int iPort){
        pClient=new SocketConnectThread(this.pMain,IP,iPort);
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

    /**
     *
     * @param url
     *
    public void set_socket_io_server(String url){
        this.url=url;
        try {
            socket = IO.socket(this.url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Message msg = pMain.myHandler.obtainMessage();
                msg.what = 2;
                msg.obj="0";   //用来存放Object类型的任意对象
                pMain.myHandler.sendMessage(msg); //发送消息
            }

        });


        socket.on("chat_event", new Emitter.Listener() {
            @Override
            public void call(Object... args1) {
                for (int i = 0; i < args1.length; i++) {
                    JSONObject obj = (JSONObject) args1[i];
                    try {
                        int ID=0;
                        if (obj.has("id")){
                            ID=obj.getInt("id");
                        }
                        String strType="";
                        if (obj.has("type")){
                            strType=obj.getString("type");
                        }

                        String strMsg = obj.getString("message");
                        String strFrom = obj.getString("from");
                        String strTo = obj.getString("to");

                        strMsg = StringEscapeUtils.unescapeHtml4(strMsg);


                        String strLine="{\"from\":\""+userName+"\",\"type\":\"chat_return\"," +
                                "\"to\":\""+strFrom+"\",\"message\":\""+ID+"\"}";
                        JSONObject obj_msg = new JSONObject(strLine);
                        socket.emit("sys_event",obj_msg); //消息返回

                        if (strFrom.equals("system") == false) {
                            if ("*".equals(strTo)){
                                //no sound
                                BackGroundService.this.showNotification(
                                        0,"n",
                                        ":" + strFrom + ">" + strTo,
                                        strMsg);
                            }else {
                                BackGroundService.this.showNotification(
                                        0,"s",
                                        ":" + strFrom + ">" + strTo,
                                        strMsg);
                            }
                        }
                        if (strFrom.equals("server_hadoop")){
                            Download_JS pDownload_JS = new Download_JS(BackGroundService.this);
                            pDownload_JS.execute(strMsg);
                        }

                        String strTime=BackGroundService.time_now();
                        String strSQL="insert into chat_log " +
                                " (Time,Msg_ID,Event,Type,SFrom,STo,Message)" +
                                " Values ('"+strTime+"',"+ID+",'chat_event','"+strType+"','"+strFrom+"','"+strTo+"','"+strMsg+"')";
                        Tools.SQL_Run(BackGroundService.context,strSQL);


                        Message msg = pMain.myHandler.obtainMessage();
                        msg.what = 1; //call_chat_event
                        msg.obj=obj;   //用来存放Object类型的任意对象
                        pMain.myHandler.sendMessage(msg); //发送消息
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        socket.on("sys_event", new Emitter.Listener() {
            @Override
            public void call(Object... args1) {
                userName=BackGroundService.getValue("sys.user_name");
                if (userName==null){
                    userName="Android.Guest";
                }
                for (int i = 0; i < args1.length; i++) {
                    JSONObject obj = (JSONObject) args1[i];
                    try {
                        String strMsg = obj.getString("message");
                        String strFrom = obj.getString("from");
                        String strType = obj.getString("type");
                        String strTo = obj.getString("to");

                        strMsg = StringEscapeUtils.unescapeHtml4(strMsg);


                        switch(strType){
                            case "chat_return":
                                //sys.Show_Text("txt_info","chat_return:"+obj.message);
                                //delete myMap[obj.message];
                                break;
                            case "30s:session"://服务器需要知道用户名
                                if (strFrom.equals("system")){
                                    //服务器会记录用户名
                                    Send_Msg(0,"sys_event","session",userName,".",strMsg);


                                    Message msg = pMain.myHandler.obtainMessage();
                                    msg.what = 2;
                                    msg.obj="1";   //用来存放Object类型的任意对象
                                    pMain.myHandler.sendMessage(msg); //发送消息

                                }
                                break;
                            case "list.all":
                                Message msg = pMain.myHandler.obtainMessage();
                                msg.what = 0;
                                msg.obj=obj;   //用来存放Object类型的任意对象
                                pMain.myHandler.sendMessage(msg); //发送消息

                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //*
        mHandler = new Handler();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                Log.i("check","check connected");
//                if (socket.connected() == false) {
//                    Log.e("socket","reconnected");
//
//                    mHandler.removeCallbacks(reconnectCallback);
//                    mHandler.postDelayed(reconnectCallback, TimeUnit.SECONDS.toMillis(5));
//                }

                Calendar ncalendar = Calendar.getInstance();
                //小时
                int hour=ncalendar.get(Calendar.HOUR_OF_DAY);
                //分
                int minute=ncalendar.get(Calendar.MINUTE);

                ArrayList<C_Remind> pList=Tools.Remind_Read(BackGroundService.context,hour,minute);

                for (int i=0;i<pList.size();i++) {
                    String strURL=pList.get(i).URL;
                    if (strURL!=null && strURL.equals("")==false){
                        Download_JS pDownload_JS = new Download_JS(BackGroundService.this);
                        pDownload_JS.execute(strURL);
                    }
                }

                //每隔30s循环执行run方法
                mHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(30));
            }
        };
        r.run();
    }
    */

//    public void Send_Msg(
//            int id,
//            String Event,
//            String Type,
//            String From,
//            String To,
//            String Msg){
//        JSONObject obj = null;
//        try {
//            obj = new JSONObject("{id:\""+id+"\","
//                    +"from:\""+From+"\", "
//                    +"to:\""+To+"\", "
//                    +"type: \""+Type+"\","
//                    +"message: \""+StringEscapeUtils.escapeHtml4(Msg) +"\"}");
//            if (socket!=null) {
//                //this.showNotification(0,"debug",Msg);
//                socket.emit(Event, obj);
//            }else{
//                this.showNotification(0,
//                        "n",
//                        "错误","socket==null");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

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

        //set_socket_io_server(this.url);
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


            //Intent mainIntent = new Intent(this, ACT_Main.class);
            Uri uri = Uri.parse(content);//"http://www.baidu.com");
            Intent mainIntent = new Intent();
            mainIntent.setAction("android.intent.action.VIEW");
            mainIntent.setData(uri);
            //startActivity(mainIntent);
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



