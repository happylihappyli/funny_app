package com.funnyai.android;

import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import static android.content.ContentValues.TAG;

public class SocketConnectThread extends Thread{


    public Socket mSocket;
    String mIpAddress;
    int mClientPort;
    OutputStream mOutStream;
    InputStream mInStream;
    private ACT_Main pMain=null;

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
            receiveMsg();
        } catch (Exception e) {
            e.printStackTrace();
            //mHandler.sendEmptyMessage(MSG_SOCKET_CONNECTFAIL);
            return;
        }
        Log.i(TAG,"connect success");
    }


    boolean bError=false;
    private void receiveMsg() {
        while(bError==false) {
            byte[] buffer = new byte[4096];
            int count = 0;
            while (count == 0) {
                try {
                    count = mInStream.read(buffer, 0, buffer.length);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (count>0){
                String data= null;
                try {
                    data = new String(buffer,0,count,"utf-8");
                    data=data.replaceAll("\\\\","\\\\\\\\");
                    data=data.replaceAll("\\r","\\\\r");
                    data=data.replaceAll("\\n","\\\\n");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                /*
                if (data.endsWith("\r\n")){
                    data=data.substring(0,data.length()-2);
                }
                //*/
                Message msg = pMain.myHandler.obtainMessage();
                msg.what = 10;//接收到tcp消息
                msg.obj=data;
                pMain.myHandler.sendMessage(msg); //发送消息
            }
        }
    }


    private void send(String msg){
        if(msg.length() == 0 || mOutStream == null)
            return;
        try {   //发送
            mOutStream.write(msg.getBytes("utf-8"));// getBytes());
            mOutStream.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
