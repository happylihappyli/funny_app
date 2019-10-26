package com.funnyai.android;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.tuenti.smsradar.Sms;
import com.tuenti.smsradar.SmsListener;
import com.tuenti.smsradar.SmsRadar;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

public class webView_function {

    public static int msg_id=0;

    public static void register(BridgeWebView webView) {

        webView.registerHandler("play", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                MyPlayer.playOrPause(data);
                function.onCallBack(data);
            }
        });

        webView.registerHandler("ini_read", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String strValue=BackGroundService.getValue(data);
                function.onCallBack(strValue);
            }
        });

        webView.registerHandler("ini_save", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                JSONObject obj;
                try {
                    obj = new JSONObject(data);
                    String key=obj.getString("key");
                    String value=obj.getString("value");

                    BackGroundService.setValue(key,value);

                    function.onCallBack(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });




        webView.registerHandler("send_msg", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                String userName=BackGroundService.getValue("sys.user_name");
                JSONObject obj;
                try {
                    obj = new JSONObject(data);
                    String strTo=obj.getString("to");
                    String message=obj.getString("message");
                    String type=obj.getString("type");
                    String return_cmd=obj.getString("return_cmd");

                    if ("msg".equals(type)){
                        String strSQL="insert into chat_log " +
                                " (Time,Msg_ID,Event,Type,SFrom,STo,Message)" +
                                " Values ('"+BackGroundService.time_now()+"',"+msg_id+",'chat_event',''," +
                                "'"+userName+"','"+strTo+"','"+message+"')";
                        Tools.SQL_Run(BackGroundService.context,strSQL);
                    }

                    BackGroundService.pClient.send_msg(
                            type,strTo,message,return_cmd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    }
}
