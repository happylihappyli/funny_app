package com.funnyai.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class Tools {
    public static String file="test.db";

    public static int Read_Version(Context mContext){

        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int iVersion=db.getVersion();

        db.close();

        return iVersion;
    }


    public static String Chat_IDs(Context mContext){

        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String strReturn="";
        Cursor c = db.rawQuery("SELECT ID FROM chat_log" +
                " WHERE ID > ?  Order By ID desc Limit 20", new String[]{"0"});
        while (c.moveToNext()) {
            int ID = c.getInt(c.getColumnIndex("ID"));
            strReturn+=ID+",";
        }
        c.close();

        //关闭当前数据库
        db.close();
        if (strReturn.endsWith(",")){
            strReturn=strReturn.substring(0,strReturn.length()-1);
        }
        return strReturn;
    }


    public static String Chat_Read(Context mContext,int ID){

        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String strReturn="";
        Cursor c = db.rawQuery("SELECT * FROM chat_log WHERE ID = ? Limit 1",
                new String[]{ID+""});
        while (c.moveToNext()) {
            String From = c.getString(c.getColumnIndex("SFrom"));
            String To = c.getString(c.getColumnIndex("STo"));
            String Msg = c.getString(c.getColumnIndex("Message"));
            String Time = c.getString(c.getColumnIndex("Time"));
            strReturn+="<id>"+ID+"</id>" +
                    "<from>"+From+"</from>" +
                    "<time>"+Time+"</time>" +
                    "<to>"+To+"</to>" +
                    "<msg>"+ StringEscapeUtils.escapeHtml4(Msg) +"</msg>";
        }
        c.close();

        //关闭当前数据库
        db.close();
        return strReturn;
    }


    public static String Remind_Read(Context mContext){

        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String strReturn="";
        Cursor c = db.rawQuery(
                "SELECT * FROM remind WHERE Deleted= ? Limit 100",
                new String[]{"0"});
        while (c.moveToNext()) {
            int ID = c.getInt(c.getColumnIndex("ID"));
            int Hour = c.getInt(c.getColumnIndex("Hour"));
            int Minute = c.getInt(c.getColumnIndex("Minute"));
            String URL = c.getString(c.getColumnIndex("URL"));
            strReturn+="<item><id>"+ID+"</id>" +
                    "<hour>"+Hour+"</hour>" +
                    "<minute>"+Minute+"</minute>" +
                    "<url>"+URL+"</url></item>" ;
        }
        c.close();

        //关闭当前数据库
        db.close();
        return strReturn;
    }


    public static ArrayList<C_Remind> Remind_Read(
            Context mContext, Integer iHour,Integer iMinute){

        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<C_Remind> pList=new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT * FROM remind WHERE Hour = ? AND Minute=? AND Deleted=0 Limit 100",
                new String[]{iHour+"",iMinute+""});
        if (c.moveToNext()) {
            int Hour = c.getInt(c.getColumnIndex("Hour"));
            int Minute = c.getInt(c.getColumnIndex("Minute"));
            String URL = c.getString(c.getColumnIndex("URL"));
            pList.add(new C_Remind(Hour,Minute,URL));
        }
        c.close();

        //关闭当前数据库
        db.close();
        return pList;
    }

    public static void Table_Drop(Context mContext){
        //打开或创建test.db数据库
        SQLiteDatabase db = mContext.openOrCreateDatabase(
                file, Context.MODE_PRIVATE, null);

        //创建person表
        db.execSQL("Drop TABLE IF EXISTS chat_log;");

        //关闭当前数据库
        db.close();
    }

    public static void SQL_Run(Context mContext,String strSQL){
        SQLiteOpenHelper dbHelper = new SQLiteHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //插入数据
        db.execSQL(strSQL);//, new Object[]{person.name, person.age});

        //关闭当前数据库
        db.close();
    }
}
