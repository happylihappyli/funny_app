package com.funnyai.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.faendir.rhino_android.RhinoAndroidHelper;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.funnyai.android.R;
import com.google.gson.Gson;
import com.tuenti.smsradar.Sms;
import com.tuenti.smsradar.SmsListener;
import com.tuenti.smsradar.SmsRadar;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Calendar;
import java.util.Locale;

import io.socket.client.Socket;

import static androidx.core.content.ContextCompat.getSystemService;

public class ACT_Main extends Activity {

    private static final int Ringtone = 0;
    private static ACT_Main inst=null;
    ///////////////
    public Handler myHandler;
    private BackGroundService mService1;
    private ListenerService mService2;
    private Socket socket;

	private final String TAG = "ACT_Main";

    private BridgeWebView webView;
    private PendingIntent pendingIntent;

	int RESULT_CODE = 0;

	ValueCallback<Uri> mUploadMessage;

	ValueCallback<Uri[]> mUploadMessageArray;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

    static class Data {
        String name;
        String value;
    }

    Download_JS pDownload_JS=null;
    ServiceConnection conn1 = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name){

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            mService1 = ((BackGroundService.MsgBinder)service).getService();

            mService1.pMain=ACT_Main.this;
            socket = BackGroundService.socket;// mService1.getSocket();
            pDownload_JS=new Download_JS(mService1);

            Log.i("test", "socket: "+socket);
        }
    };

    public static ACT_Main instance() {
        return inst;
    }

    public boolean Get_WIFI_Connected(){
        WifiManager wifiMgr = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            wifiInfo.getSSID();
            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }


    public String Get_WIFI_Name(){
        WifiManager wifiMgr = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            return wifiInfo.getSSID();
        }else {
            return "isWifiEnabled=false";
        }
    }

    ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            mService2 = ((ListenerService.MsgBinder)service).getService();

            Log.i("test", "socket: "+socket);
        }
    };

    /**
     * 设置铃声之后的回调函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        } else {
            // 得到我们选择的铃声,如果选择的是"静音"，那么将会返回null
            Uri uri = data
                    .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Log.e("onActivityResult====", "" + uri);
            //Toast.makeText(mContext, uri + "", 500).show();
            if (uri != null) {
                switch (requestCode) {
                    case Ringtone:
                        //this.mService1.showNotification(0,"url",uri.toString());

                        Data pData = new Data();
                        pData.value = uri.toString();
                        pData.name = "";
                        //
                        webView.callHandler("ringtone_selected",
                                new Gson().toJson(pData), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                                //ACT_Main.this.mService1.showNotification(0,"back",data);
                            }
                        });
                        break;
                }
            }
        }
    }



    public void callback_connected(String data){
        webView.callHandler("callback_connected",
                data, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        //ACT_Main.this.mService1.showNotification(0,"back",data);
                    }
                });
    }


    public void callback_chat_event(String data){
        webView.callHandler("callback_chat_event",
            data, new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    //ACT_Main.this.mService1.showNotification(0,"back",data);
                }
            });
    }



    public void callback_sys_event(String data){
        webView.callHandler("callback_sys_event",
                data, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i("",data);
                        //ACT_Main.this.mService1.showNotification(0,"back",data);
                    }
                });
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        inst=this;

        ///////////////////////////////////////
		setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);

//        EditText pText = (EditText) findViewById(R.id.editText);
//        Button pButton = (Button) findViewById(R.id.button_go);
//        pButton.setOnClickListener( new OnClickListener( ) {
//            @Override
//            public void onClick(View v) {
//                //mService1.showNotification(0,"test",pDownload_JS.getStatus().toString());
//
//                pDownload_JS.execute(pText.getText().toString());
//            }
//        });

        //绑定Service1
        Intent bindIntent1 = new Intent(this, BackGroundService.class);
        bindService(bindIntent1, conn1, BIND_AUTO_CREATE);


        //绑定Service1
        Intent bindIntent2 = new Intent(this, ListenerService.class);
        bindService(bindIntent2, conn2, BIND_AUTO_CREATE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BackGroundService.class));
        } else {
            startService(new Intent(this, BackGroundService.class));
        }



        //默认的处理方法
		webView.setDefaultHandler(new DefaultHandler(){

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = default, data from ... = " + data);
				function.onCallBack("default ...");
			}
		});

        webView.setWebViewClient(new MyWebViewClient(webView));
		webView.setWebChromeClient(new WebChromeClient() {

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				this.openFileChooser(uploadMsg);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
				this.openFileChooser(uploadMsg);
			}

			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				pickFile();
			}

			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				mUploadMessageArray = filePathCallback;
				pickFile();
				return true;
			}
		});

        Bundle bundle = this.getIntent().getExtras();
        String url = "";
        if (bundle!=null) {
            url = bundle.getString("url");
        }
        if (url!=null && "".equals(url)==false){
            webView.loadUrl(url);
        }else {
            webView.loadUrl("file:///android_asset/main.html");
        }

        webView_function.register(webView);

        webView.registerHandler("set_socket_io_server", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                mService1.set_socket_io_server(data);
                socket = BackGroundService.socket;//mService1.getSocket();
                function.onCallBack(data);
            }
        });

        webView.registerHandler("init_sms", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                SmsRadar.initializeSmsRadarService(
                        ACT_Main.this.getApplicationContext(),
                        new SmsListener() {
                            @Override
                            public void onSmsSent(Sms sms) {
                                //showSmsToast(sms);
                            }

                            @Override
                            public void onSmsReceived(Sms sms) {
                                JSONObject obj= new JSONObject();
                                try{
                                    obj.put("user",sms.getAddress());
                                    obj.put("msg",sms.getMsg());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                function.onCallBack(obj.toString());
                            }
                        });
            }
        });


        webView.registerHandler("tts", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                BackGroundService.tts_speak(data);
            }
        });

        webView.registerHandler("show_notification", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                JSONObject obj;
                try {
                    obj = new JSONObject(data);
                    String strTitle=obj.getString("title");
                    String message=obj.getString("message");

                    mService1.showNotification(
                            0,
                            strTitle,
                            message);
                    function.onCallBack(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        webView.registerHandler("set_alarm", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String[] strSplit=data.split(":");
                int hour = Integer.parseInt(strSplit[0]) ;
                int minute = Integer.parseInt(strSplit[1]);
//                int iVersion=Tools.Read_Version(ACT_Main.this);
//                function.onCallBack(iVersion+"");

                    Log.d("MyActivity", "Alarm On");
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY,hour);
                    calendar.set(Calendar.MINUTE,minute);
                    Intent myIntent = new Intent(ACT_Main.this, ACT_Main.class);
                    pendingIntent = PendingIntent.getBroadcast(ACT_Main.this, 0, myIntent, 0);
                    AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
//                } else {
//                    alarmManager.cancel(pendingIntent);
//                    setAlarmText("");
//                    Log.d("MyActivity", "Alarm Off");
//                }
            }
        });


        webView.registerHandler("chat_ids", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String strLine=Tools.Chat_IDs(ACT_Main.this);
                function.onCallBack(strLine);
            }
        });


        webView.registerHandler("chat_read", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                int id=Integer.parseInt(data);
                String strLine=Tools.Chat_Read(ACT_Main.this,id);
                function.onCallBack(strLine);
            }
        });


        webView.registerHandler("new_win", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                Intent intent = new Intent(ACT_Main.this, ACT_Main.class);
                Bundle bundle = new Bundle();
                bundle.putString("url",data);
                intent.putExtras(bundle);
                ACT_Main.this.startActivity(intent);

                function.onCallBack(data);
            }
        });

        webView.registerHandler("sql_run", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

//                String strTime=BackGroundService.time_now();
//                String strSQL="insert into chat_log " +
//                        " (Time,Msg_ID,Event,Type,SFrom,STo,Message)" +
//                        " Values ('"+strTime+"',"+ID+",'chat_event','"+strType+"','"+strFrom+"','"+strTo+"','"+strMsg+"')";

                Tools.SQL_Run(BackGroundService.context,data);
            }
        });


        webView.registerHandler("remind_read", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String strLine=Tools.Remind_Read(BackGroundService.context);
                function.onCallBack(strLine);
            }
        });


        webView.registerHandler("setting", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                //打开监听引用消息Notification access
                Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent_s);
            }
        });

        webView.registerHandler("rebind", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                ACT_Main.this.toggleNotificationListenerService();
            }
        });

        webView.registerHandler("wifi_name", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                data=ACT_Main.this.Get_WIFI_Name();
                function.onCallBack(data);
            }
        });


        webView.registerHandler("save_alert", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                ACT_Main.this.mService1.bAlert=data;
                BackGroundService.setValue("alert",data);
            }
        });



        webView.registerHandler("select_ringtone", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                Intent intent = new Intent(
                        RingtoneManager.ACTION_RINGTONE_PICKER);

                // 列表中不显示"默认铃声"选项，默认是显示的
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                        false);

                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM,
                        true);

                // 设置列表对话框的标题，不设置，默认显示"铃声"
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置来电铃声");
                startActivityForResult(intent, Ringtone);
            }
        });

        webView.send("hello");



        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 0:
                        ACT_Main.this.callback_sys_event(msg.obj.toString());
                        break;
                    case 1:
                        ACT_Main.this.callback_chat_event(msg.obj.toString());
                        break;
                    case 2:
                        ACT_Main.this.callback_connected(msg.obj.toString());
                    default:
                        break;
                }
            }
        };
    }


    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, ListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, ListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


	public void pickFile() {
		Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooserIntent.setType("image/*");
		startActivityForResult(chooserIntent, RESULT_CODE);
	}



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn1);
        unbindService(conn2);
    }


    class MyWebViewClient extends BridgeWebViewClient {

        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }

        //网页加载完成

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);

            Log.e(TAG,"onPageFinished");

            webView.callHandler("sys_on_load",
                    "bbb",new CallBackFunction(){
                @Override
                public void onCallBack(String data) {
                    Log.e(TAG, "来自web的回传数据：" + data);
                }
            });

        }

    }
}

