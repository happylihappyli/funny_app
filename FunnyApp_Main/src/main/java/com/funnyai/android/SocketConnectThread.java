package com.funnyai.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import static android.content.ContentValues.TAG;


public class SocketConnectThread extends Thread{

    class Connect_Handle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            event_connected();
        }
    }

    public Connect_Handle pConnected=new Connect_Handle();

    public Socket mSocket;
    String mIpAddress;
    int mClientPort;
    OutputStream mOutStream;
    InputStream mInStream;
    private ACT_Main pMain=null;
    public long keep_count=0;
    public String userName="";

    public SocketConnectThread(
            ACT_Main pMain,
            String mIpAddress,
            int mClientPort){
        this.pMain=pMain;
        this.mIpAddress=mIpAddress;
        this.mClientPort=mClientPort;
    }

    @Override
    public void run(){
        try {
            //指定ip地址和端口号
            mSocket = new Socket(mIpAddress,mClientPort);
            if(mSocket != null){
                //获取输出流、输入流
                mOutStream = mSocket.getOutputStream();
                mInStream = mSocket.getInputStream();
            }
            Message msg=new Message();
            pConnected.handleMessage(msg);
            receiveMsg();
        } catch (Exception e) {
            e.printStackTrace();
            //mHandler.sendEmptyMessage(MSG_SOCKET_CONNECTFAIL);
            return;
        }
        Log.i(TAG,"connect success");
    }


    public void event_connected(){
        userName=BackGroundService.getValue("sys.user_name");
        String password=BackGroundService.getValue("sys.user_password");
        send_msg("login","","","login");
    }




    Handler handler_send_msg = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String token = data.getString("token");
            String type = data.getString("type");
            String friend = data.getString("friend");
            String message = data.getString("message");
            String return_cmd = data.getString("return_cmd");



            Log.i("mylog","请求结果-->" + token);


            String strLine="";

            String strMsg2=message.replace("\"","\\\"");
            strMsg2=strMsg2.replace("\n","\\n");

            if ("".equals(token)==false){
                strLine="{\"id\":\""+msg_id+"\","
                        +"\"token\":\""+token+"\","
                        +"\"return_cmd\":\""+return_cmd+"\","
                        +"\"from\":\""+userName+"\",\"type\":\""+type+"\","
                        +"\"to\":\""+friend+"\",\"message\":\""+strMsg2+"\"}";

                switch(type){
                    case "login":
                    case "friend_list":
                        break;
                    default:
                        //myMap["K"+msg_id]=new C_Msg(msg_id,strLine);
                        break;
                }

                send_string("m:<s>:"+strLine+":</s>\r\n");
            }
        }
    };


    private int msg_id=1;
    public void send_msg(
            String type,
            String friend,
            String message,
            String return_cmd) {

        msg_id+=1;

        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                String token=BackGroundService.sys_get_token();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("type",type);
                data.putString("friend",friend);
                data.putString("message",message);
                data.putString("token",token);
                data.putString("return_cmd",return_cmd);
                msg.setData(data);
                handler_send_msg.sendMessage(msg);
            }
        };
        new Thread(runnable).start();


    }

    public String data_remain="";
    boolean bError=false;
    private void receiveMsg() {
        while(bError==false) {
            byte[] buffer = new byte[4096];
            int count = 0;
            while (count == 0) {
                try {
                    count = mInStream.read(buffer, 0, buffer.length);
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
            if (count>0){
                String data= null;
                try {
                    data = new String(buffer,0,count,"utf-8");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }

                data=data_remain+data;
                data_remain="";
                while(true){
                    int index1=data.indexOf(":<s>:");
                    int index2=data.indexOf(":</s>");
                    if (index2>index1 && index1>0) {
                        String json=data.substring(index1+5,index2);

                        json=json.replaceAll("\\\\","\\\\\\\\");
                        json=json.replaceAll("\\r","\\\\r");
                        json=json.replaceAll("\\n","\\\\n");
                        try{
                            JSONObject pObj=new JSONObject(json);
                            if (pObj.has("k")){
                                keep_count=0;
                            }else {
                                Message msg = pMain.myHandler.obtainMessage();
                                msg.what = 11;//接收到tcp json消息
                                msg.obj = json;
                                pMain.myHandler.sendMessage(msg); //发送事件
                            }
                        }catch(Exception ex){

                        }

                        data=data.substring(index2+5);
                    }else{
                        data_remain=data;
                        break;
                    }
                }
            }
        }
    }


    private void send_string(String msg){
        if(msg.length() == 0 || mOutStream == null)
            return;

        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                try {   //发送
                    mOutStream.write(msg.getBytes("utf-8"));// getBytes());
                    mOutStream.flush();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();


    }
}
