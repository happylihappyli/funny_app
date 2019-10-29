package com.funnyai.tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class M_Net {

    public static String http_get(
            String url) {
        String strEncode="utf-8";
        String reference_url="";
        int TimeOut=10;
        String result = "";
        try {
            URL httpurl = new URL(url);
            HttpURLConnection httpConn;

            boolean bProxy=true;

            httpConn = (HttpURLConnection) httpurl.openConnection();

            httpConn.setConnectTimeout(TimeOut*1000);
            httpConn.setReadTimeout(TimeOut*1000);
            httpConn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1");
            httpConn.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
            httpConn.setRequestProperty("Referer", reference_url);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream(), strEncode));
            String line;
            while ((line = in.readLine()) != null) {
                result += line + "\r\n";
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            //S_Net.SI_Send("sys_event","error","http_get","*",e.toString());
            Log.i("error","Encode="+strEncode);
            Log.i("error","url="+url);
            e.printStackTrace();
        }
        return result;
    }

}
