package com.funnyai.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class S_File {

    public static String Read(String url){
        URL web = null;//"http://www.yahoo.com/");
        try {
            web = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(web.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder pStr=new StringBuilder();
        String inputLine="";
        while (inputLine!=null){
            try {
                inputLine = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputLine!=null) {
                pStr.append(inputLine + "\n");
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pStr.toString();
    }
}
