package com.funnyai.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class M_String {
    public String urlencode(String strLine){
        if (strLine==null) return "";
        try {
            return URLEncoder.encode(strLine, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(M_String.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(strLine);
            ex.printStackTrace();
        }
        return "error";
    }
}
